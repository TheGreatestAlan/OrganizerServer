package com.nguyen.server.repository;

import com.nguyen.server.model.Container;
import com.nguyen.server.model.Item;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface InventoryDao {

    @SqlUpdate("CREATE TABLE IF NOT EXISTS containers (" +
            "id VARCHAR(255) PRIMARY KEY, " +
            "location_description VARCHAR(1000))")
    void createContainersTable();

    @SqlUpdate("CREATE TABLE IF NOT EXISTS items (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(500) NOT NULL, " +
            "container_id VARCHAR(255) NOT NULL, " +
            "quantity INT DEFAULT 1, " +
            "FOREIGN KEY (container_id) REFERENCES containers(id) ON DELETE CASCADE, " +
            "UNIQUE KEY unique_item_in_container (name, container_id))")
    void createItemsTable();

    @SqlUpdate("CREATE INDEX IF NOT EXISTS idx_items_name ON items(name)")
    void createItemsNameIndex();

    @SqlUpdate("CREATE INDEX IF NOT EXISTS idx_items_container ON items(container_id)")
    void createItemsContainerIndex();

    @SqlQuery("SELECT id, location_description FROM containers WHERE id = :id")
    @RegisterBeanMapper(Container.class)
    Optional<Container> findContainerById(@Bind("id") String id);

    @SqlQuery("SELECT id, location_description FROM containers")
    @RegisterBeanMapper(Container.class)
    List<Container> listAllContainers();

    @SqlUpdate("INSERT INTO containers (id, location_description) VALUES (:id, :locationDescription)")
    void insertContainer(@BindBean Container container);

    @SqlUpdate("UPDATE containers SET location_description = :locationDescription WHERE id = :id")
    void updateContainer(@BindBean Container container);

    @SqlUpdate("DELETE FROM containers WHERE id = :id")
    void deleteContainer(@Bind("id") String id);

    @SqlQuery("SELECT id, name, container_id, quantity FROM items WHERE container_id = :containerId")
    @RegisterBeanMapper(Item.class)
    List<Item> findItemsByContainer(@Bind("containerId") String containerId);

    @SqlQuery("SELECT id, name, container_id, quantity FROM items WHERE LOWER(name) LIKE LOWER(:pattern)")
    @RegisterBeanMapper(Item.class)
    List<Item> searchItemsByName(@Bind("pattern") String pattern);

    @SqlQuery("SELECT id, name, container_id, quantity FROM items WHERE name = :name AND container_id = :containerId")
    @RegisterBeanMapper(Item.class)
    Optional<Item> findItemByNameAndContainer(@Bind("name") String name, @Bind("containerId") String containerId);

    @SqlUpdate("INSERT INTO items (name, container_id, quantity) VALUES (:name, :containerId, :quantity)")
    void insertItem(@BindBean Item item);

    @SqlUpdate("UPDATE items SET quantity = :quantity WHERE id = :id")
    void updateItemQuantity(@Bind("id") Long id, @Bind("quantity") int quantity);

    @SqlUpdate("DELETE FROM items WHERE id = :id")
    void deleteItem(@Bind("id") Long id);

    @SqlUpdate("DELETE FROM items WHERE container_id = :containerId AND name = :name")
    void deleteItemByNameAndContainer(@Bind("name") String name, @Bind("containerId") String containerId);
}
