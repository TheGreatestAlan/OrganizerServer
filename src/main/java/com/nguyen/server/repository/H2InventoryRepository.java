package com.nguyen.server.repository;

import com.nguyen.server.model.Container;
import com.nguyen.server.model.Item;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.util.*;

public class H2InventoryRepository implements com.nguyen.server.interfaces.OrganizerRepository {

    private final Jdbi jdbi;

    public H2InventoryRepository(String jdbcUrl) {
        this.jdbi = Jdbi.create(jdbcUrl);
        this.jdbi.installPlugin(new SqlObjectPlugin());
        initializeSchema();
    }

    public H2InventoryRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
        initializeSchema();
    }

    private void initializeSchema() {
        jdbi.useExtension(InventoryDao.class, dao -> {
            dao.createContainersTable();
            dao.createItemsTable();
            dao.createItemsNameIndex();
            dao.createItemsContainerIndex();
        });
    }

    @Override
    public List<String> getTodoList() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<String>> getOrganizerInventory() {
        return jdbi.withExtension(InventoryDao.class, dao -> {
            Map<String, List<String>> inventory = new HashMap<>();
            List<Container> containers = dao.listAllContainers();

            for (Container container : containers) {
                List<Item> items = dao.findItemsByContainer(container.getId());
                List<String> itemNames = new ArrayList<>();
                for (Item item : items) {
                    String name = item.getName();
                    if (item.getQuantity() > 1) {
                        name = name + " (x" + item.getQuantity() + ")";
                    }
                    itemNames.add(name);
                }
                inventory.put(container.getId(), itemNames);
            }

            return inventory;
        });
    }

    @Override
    public List<String> getContainerLocation() {
        return jdbi.withExtension(InventoryDao.class, dao -> {
            List<Container> containers = dao.listAllContainers();
            List<String> locations = new ArrayList<>();
            for (Container container : containers) {
                String desc = container.getLocationDescription();
                if (desc != null && !desc.isEmpty()) {
                    locations.add(container.getId() + ": " + desc);
                } else {
                    locations.add(container.getId());
                }
            }
            return locations;
        });
    }

    @Override
    public List<String> getContainerById(String containerId) {
        return jdbi.withExtension(InventoryDao.class, dao -> {
            List<Item> items = dao.findItemsByContainer(containerId);
            List<String> itemNames = new ArrayList<>();
            for (Item item : items) {
                String name = item.getName();
                if (item.getQuantity() > 1) {
                    name = name + " (x" + item.getQuantity() + ")";
                }
                itemNames.add(name);
            }
            return itemNames;
        });
    }

    @Override
    public void saveOrganizerInventory(Map<String, List<String>> inventory) {
        jdbi.useTransaction(handle -> {
            InventoryDao dao = handle.attach(InventoryDao.class);

            for (Map.Entry<String, List<String>> entry : inventory.entrySet()) {
                String containerId = entry.getKey();

                Optional<Container> existing = dao.findContainerById(containerId);
                if (existing.isEmpty()) {
                    dao.insertContainer(new Container(containerId, null));
                }

                for (String itemName : entry.getValue()) {
                    int quantity = 1;
                    String cleanName = itemName;

                    if (itemName.contains("(x")) {
                        int start = itemName.lastIndexOf("(x");
                        int end = itemName.lastIndexOf(")");
                        if (start > 0 && end > start) {
                            try {
                                quantity = Integer.parseInt(itemName.substring(start + 2, end));
                                cleanName = itemName.substring(0, start).trim();
                            } catch (NumberFormatException e) {
                                // Keep original
                            }
                        }
                    }

                    Optional<Item> existingItem = dao.findItemByNameAndContainer(cleanName, containerId);
                    if (existingItem.isPresent()) {
                        dao.updateItemQuantity(existingItem.get().getId(), quantity);
                    } else {
                        dao.insertItem(new Item(cleanName, containerId, quantity));
                    }
                }
            }
        });
    }

    @Override
    public void addOrganizerInventory(String item, String containerId) {
        addOrganizerInventory(item, containerId, 1);
    }

    public void addOrganizerInventory(String item, String containerId, int quantity) {
        jdbi.useTransaction(handle -> {
            InventoryDao dao = handle.attach(InventoryDao.class);

            Optional<Container> container = dao.findContainerById(containerId);
            if (container.isEmpty()) {
                dao.insertContainer(new Container(containerId, null));
            }

            Optional<Item> existing = dao.findItemByNameAndContainer(item, containerId);
            if (existing.isPresent()) {
                Item existingItem = existing.get();
                dao.updateItemQuantity(existingItem.getId(), existingItem.getQuantity() + quantity);
            } else {
                dao.insertItem(new Item(item, containerId, quantity));
            }
        });
    }

    @Override
    public void deleteOrganizerInventory(String item, String containerId) {
        deleteOrganizerInventory(item, containerId, 1);
    }

    public void deleteOrganizerInventory(String item, String containerId, int quantity) {
        jdbi.useTransaction(handle -> {
            InventoryDao dao = handle.attach(InventoryDao.class);

            Optional<Item> existing = dao.findItemByNameAndContainer(item, containerId);
            if (existing.isPresent()) {
                Item existingItem = existing.get();
                int newQuantity = existingItem.getQuantity() - quantity;
                if (newQuantity <= 0) {
                    dao.deleteItem(existingItem.getId());
                } else {
                    dao.updateItemQuantity(existingItem.getId(), newQuantity);
                }
            }
        });
    }

    @Override
    public boolean containerExists(String containerId) {
        return jdbi.withExtension(InventoryDao.class,
                dao -> dao.findContainerById(containerId).isPresent());
    }

    @Override
    public void createContainer(String containerId, String locationDescription) {
        jdbi.useExtension(InventoryDao.class,
                dao -> dao.insertContainer(new Container(containerId, locationDescription)));
    }

    @Override
    public boolean renameContainer(String oldId, String newId, String locationDescription) {
        return jdbi.inTransaction(handle -> {
            InventoryDao dao = handle.attach(InventoryDao.class);
            Optional<Container> existing = dao.findContainerById(oldId);
            if (existing.isEmpty()) {
                return false;
            }
            String effectiveDescription = (locationDescription != null)
                    ? locationDescription
                    : existing.get().getLocationDescription();
            if (newId == null || newId.equals(oldId)) {
                dao.updateContainer(new Container(oldId, effectiveDescription));
                return true;
            }
            dao.insertContainer(new Container(newId, effectiveDescription));
            dao.moveItemsToContainer(oldId, newId);
            dao.deleteContainer(oldId);
            return true;
        });
    }
}
