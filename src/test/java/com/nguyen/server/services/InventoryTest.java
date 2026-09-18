package com.nguyen.server.services;

import com.nguyen.server.repository.H2InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        String jdbcUrl = "jdbc:h2:file:" + tempDir.resolve("test.db").toString() + ";DB_CLOSE_DELAY=-1";
        inventory = new Inventory(new H2InventoryRepository(jdbcUrl));
    }

    @Test
    void testContainerExists() {
        assertFalse(inventory.containerExists("garage"));
        inventory.createContainer("garage", "detached garage");
        assertTrue(inventory.containerExists("garage"));
    }

    @Test
    void testCreateContainerReturnsFalseWhenAlreadyExists() {
        assertTrue(inventory.createContainer("garage", null));
        assertFalse(inventory.createContainer("garage", null));
    }

    @Test
    void testCreateContainerVisibleInInventory() {
        inventory.createContainer("garage", null);
        assertTrue(inventory.getOrganizerInventory().containsKey("garage"));
    }

    @Test
    void testRenameContainerMovesItemsAndRefreshesCache() {
        inventory.createItemsInContainer("old-box", List.of("hammer"));
        assertTrue(inventory.getContainerById("old-box").contains("hammer"));

        boolean result = inventory.renameContainer("old-box", "new-box", null);

        assertTrue(result);
        assertTrue(inventory.getContainerById("old-box").isEmpty());
        assertTrue(inventory.getContainerById("new-box").contains("hammer"));
    }

    @Test
    void testRenameContainerNonexistentReturnsFalse() {
        assertFalse(inventory.renameContainer("missing", "new-name", null));
    }

    @Test
    void testRenameContainerDescriptionOnly() {
        inventory.createContainer("shed", "back yard");
        assertTrue(inventory.renameContainer("shed", null, "side yard"));
        assertTrue(inventory.getContainerLocation().contains("shed: side yard"));
    }
}
