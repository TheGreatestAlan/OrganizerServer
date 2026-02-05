package com.nguyen.server.services;

import com.nguyen.server.RateLimitException;
import com.nguyen.server.interfaces.OrganizerRepository;
import com.nguyen.server.repository.H2InventoryRepository;

import java.util.*;
import java.util.stream.Collectors;

public class Inventory {

    private final OrganizerRepository organizerRepository;
    private Map<String, List<String>> cachedOrganizerInventory;
    private List<String> cachedContainerLocation;

    public Inventory(OrganizerRepository organizerRepository) {
        this.organizerRepository = organizerRepository;
        refreshCache();
    }

    public void refreshCache() {
        this.cachedOrganizerInventory = sortOrganizerInventory(
                organizerRepository.getOrganizerInventory());
        this.cachedContainerLocation = organizerRepository.getContainerLocation();
    }

    private Map<String, List<String>> sortOrganizerInventory(Map<String, List<String>> inventory) {
        Map<String, List<String>> sortedInventory = new LinkedHashMap<>();

        inventory.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(new CustomKeyComparator()))
                .forEachOrdered(entry -> sortedInventory.put(entry.getKey(), entry.getValue()));

        return sortedInventory;
    }

    private static class CustomKeyComparator implements Comparator<String> {
        @Override
        public int compare(String key1, String key2) {
            try {
                int num1 = Integer.parseInt(key1);
                int num2 = Integer.parseInt(key2);
                return Integer.compare(num1, num2);
            } catch (NumberFormatException e) {
                if (key1.matches("\\d+") && !key2.matches("\\d+")) {
                    return -1;
                } else if (!key1.matches("\\d+") && key2.matches("\\d+")) {
                    return 1;
                } else {
                    return key1.compareTo(key2);
                }
            }
        }
    }

    public Map<String, List<String>> getOrganizerInventory() {
        refreshCache();
        return cachedOrganizerInventory;
    }

    public List<String> getContainerLocation() {
        try {
            cachedContainerLocation = organizerRepository.getContainerLocation();
        } catch (RateLimitException e) {
            // Return the cached version in case of a rate limit exception
        }
        return cachedContainerLocation;
    }

    public List<String> findItem(String itemName) {
        refreshCache();
        List<String> results = new ArrayList<>();
        String query = itemName.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, List<String>> entry : cachedOrganizerInventory.entrySet()) {
            String containerId = entry.getKey();
            List<String> items = entry.getValue();
            List<String> matchingItems = items.stream()
                    .map(String::trim)
                    .filter(item -> {
                        String cleanItem = item;
                        if (item.contains("(x")) {
                            int idx = item.lastIndexOf("(x");
                            cleanItem = item.substring(0, idx).trim();
                        }
                        return cleanItem.toLowerCase(Locale.ROOT).contains(query);
                    })
                    .collect(Collectors.toList());
            if (!matchingItems.isEmpty()) {
                results.add(containerId + ":" + String.join(",", matchingItems));
            }
        }

        return results;
    }

    public List<String> getContainerById(String containerId) {
        refreshCache();
        return cachedOrganizerInventory.getOrDefault(containerId.trim(), Collections.emptyList());
    }

    public List<String> findContainerLocation(String containerId) {
        refreshCache();
        String query = containerId.toLowerCase(Locale.ROOT);
        return cachedContainerLocation.stream()
                .filter(location -> location.toLowerCase(Locale.ROOT).contains(query))
                .collect(Collectors.toList());
    }

    public boolean createItemsInContainer(String containerId, List<String> items) {
        return createItemsInContainer(containerId, items, 1);
    }

    public boolean createItemsInContainer(String containerId, List<String> items, int quantity) {
        containerId = containerId.trim();
        items = items.stream().map(String::trim).collect(Collectors.toList());

        if (organizerRepository instanceof H2InventoryRepository) {
            H2InventoryRepository h2Repo = (H2InventoryRepository) organizerRepository;
            for (String item : items) {
                h2Repo.addOrganizerInventory(item, containerId, quantity);
            }
        } else {
            for (String item : items) {
                organizerRepository.addOrganizerInventory(item, containerId);
            }
        }

        refreshCache();
        return true;
    }

    public boolean deleteItemsFromContainer(String containerId, List<String> itemsToDelete) {
        return deleteItemsFromContainer(containerId, itemsToDelete, 1);
    }

    public boolean deleteItemsFromContainer(String containerId, List<String> itemsToDelete, int quantity) {
        containerId = containerId.trim();
        itemsToDelete = itemsToDelete.stream()
                .map(String::trim)
                .collect(Collectors.toList());

        boolean updated = false;

        if (organizerRepository instanceof H2InventoryRepository) {
            H2InventoryRepository h2Repo = (H2InventoryRepository) organizerRepository;
            for (String item : itemsToDelete) {
                h2Repo.deleteOrganizerInventory(item, containerId, quantity);
                updated = true;
            }
        } else {
            for (String item : itemsToDelete) {
                organizerRepository.deleteOrganizerInventory(item, containerId);
                updated = true;
            }
        }

        if (updated) {
            refreshCache();
        }

        return updated;
    }
}
