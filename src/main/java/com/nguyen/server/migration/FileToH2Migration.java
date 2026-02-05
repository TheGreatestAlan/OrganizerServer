package com.nguyen.server.migration;

import com.nguyen.server.repository.H2InventoryRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileToH2Migration {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: FileToH2Migration <itemLocation.md path> <jdbc url>");
            System.out.println("Example: FileToH2Migration ./data/ItemLocation.md jdbc:h2:file:./inventory.db");
            System.exit(1);
        }

        String filePath = args[0];
        String jdbcUrl = args[1];

        H2InventoryRepository repo = new H2InventoryRepository(jdbcUrl);

        List<String> lines = Files.readAllLines(Path.of(filePath));
        int migratedCount = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || !line.contains(":")) {
                continue;
            }

            String[] parts = line.split(":", 2);
            String containerId = parts[0].trim();
            String[] items = parts[1].split(",");

            for (String item : items) {
                item = item.trim();
                if (!item.isEmpty()) {
                    // Parse quantity if present (e.g., "Item (x5)")
                    int quantity = 1;
                    String cleanName = item;

                    if (item.contains("(x")) {
                        int start = item.lastIndexOf("(x");
                        int end = item.lastIndexOf(")");
                        if (start > 0 && end > start) {
                            try {
                                quantity = Integer.parseInt(item.substring(start + 2, end));
                                cleanName = item.substring(0, start).trim();
                            } catch (NumberFormatException e) {
                                // Keep original name and quantity 1
                            }
                        }
                    }

                    repo.addOrganizerInventory(cleanName, containerId, quantity);
                    System.out.println("Migrated: " + cleanName + " (x" + quantity + ") -> " + containerId);
                    migratedCount++;
                }
            }
        }

        System.out.println("\nMigration complete! Migrated " + migratedCount + " items.");
    }
}
