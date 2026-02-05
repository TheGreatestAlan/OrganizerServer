package com.nguyen.server.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class H2InventoryRepositoryTest {

    private H2InventoryRepository repository;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        String jdbcUrl = "jdbc:h2:file:" + tempDir.resolve("test.db").toString() + ";DB_CLOSE_DELAY=-1";
        repository = new H2InventoryRepository(jdbcUrl);
    }

    @Test
    void testAddAndRetrieveItem() {
        repository.addOrganizerInventory("Cumin", "1", 3);

        List<String> items = repository.getContainerById("1");

        assertEquals(1, items.size());
        assertEquals("Cumin (x3)", items.get(0));
    }

    @Test
    void testIncrementQuantity() {
        repository.addOrganizerInventory("Salt", "2", 1);
        repository.addOrganizerInventory("Salt", "2", 2);

        List<String> items = repository.getContainerById("2");

        assertEquals(1, items.size());
        assertEquals("Salt (x3)", items.get(0));
    }

    @Test
    void testDeleteItemPartial() {
        repository.addOrganizerInventory("Pepper", "3", 5);
        repository.deleteOrganizerInventory("Pepper", "3", 2);

        List<String> items = repository.getContainerById("3");

        assertEquals(1, items.size());
        assertEquals("Pepper (x3)", items.get(0));
    }

    @Test
    void testDeleteItemCompletely() {
        repository.addOrganizerInventory("Sugar", "4", 2);
        repository.deleteOrganizerInventory("Sugar", "4", 2);

        List<String> items = repository.getContainerById("4");

        assertTrue(items.isEmpty());
    }

    @Test
    void testGetOrganizerInventory() {
        repository.addOrganizerInventory("Item1", "A", 1);
        repository.addOrganizerInventory("Item2", "A", 1);
        repository.addOrganizerInventory("Item3", "B", 1);

        Map<String, List<String>> inventory = repository.getOrganizerInventory();

        assertEquals(2, inventory.size());
        assertEquals(2, inventory.get("A").size());
        assertEquals(1, inventory.get("B").size());
    }

    @Test
    void testGetContainerLocation() {
        repository.addOrganizerInventory("Test", "Kitchen", 1);

        List<String> locations = repository.getContainerLocation();

        assertEquals(1, locations.size());
        assertEquals("Kitchen", locations.get(0));
    }

    @Test
    void testSaveOrganizerInventory() {
        Map<String, List<String>> inventory = Map.of(
            "Box1", List.of("Item1", "Item2 (x3)"),
            "Box2", List.of("Item3")
        );

        repository.saveOrganizerInventory(inventory);

        Map<String, List<String>> retrieved = repository.getOrganizerInventory();
        assertEquals(2, retrieved.size());
        assertTrue(retrieved.get("Box1").contains("Item1"));
        assertTrue(retrieved.get("Box1").contains("Item2 (x3)"));
    }

    @Test
    void testTodoListReturnsEmpty() {
        List<String> todos = repository.getTodoList();
        assertTrue(todos.isEmpty());
    }
}
