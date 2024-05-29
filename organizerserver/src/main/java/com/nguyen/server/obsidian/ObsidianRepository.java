package com.nguyen.server.obsidian;

import com.nguyen.server.OrganizerRepositoryException;
import com.nguyen.server.interfaces.OrganizerRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class ObsidianRepository implements OrganizerRepository {

  private final String organizerLocation;
  private final static String ITEM_LOCATION_FILENAME = "ItemLocation.md";
  private final static String CONTAINER_LOCATION_FILENAME = "ContainerLocation.md";
  private final static String TODO_LIST_FILENAME = "Todo.md";

  public ObsidianRepository(String location) {
    this.organizerLocation = location;
  }

  @Override
  public List<String> getTodoList() {
    return readFile(Path.of(organizerLocation, TODO_LIST_FILENAME));
  }

  @Override
  public Map<String, List<String>> getOrganizerInventory() {
    List<String> lines = readFile(Path.of(organizerLocation, ITEM_LOCATION_FILENAME));
    return lines.stream()
        .filter(line -> !line.startsWith("#") && line.contains(":"))
        .map(line -> line.split(":"))
        .filter(parts -> parts.length >= 2)  // Ensure the line has at least two parts
        .collect(Collectors.toMap(
            parts -> parts[0].trim(),
            parts -> Arrays.stream(parts[1].split(","))
                .map(String::trim)
                .collect(Collectors.toList())
        ));
  }

  @Override
  public List<String> getContainerById(String containerId) {
    Map<String, List<String>> inventory = getOrganizerInventory();
    return inventory.getOrDefault(containerId, Collections.emptyList());
  }

  @Override
  public List<String> getContainerLocation() {
    return readFile(Path.of(organizerLocation, CONTAINER_LOCATION_FILENAME));
  }

  @Override
  public void saveOrganizerInventory(Map<String, List<String>> inventory) {
    List<String> lines = inventory.entrySet().stream()
        .map(entry -> entry.getKey() + ":" + String.join(",", entry.getValue()))
        .collect(Collectors.toList());
    writeFile(Path.of(organizerLocation, ITEM_LOCATION_FILENAME), lines);
  }

  private List<String> readFile(Path path) {
    try {
      return Files.readAllLines(path);
    } catch (IOException e) {
      throw new OrganizerRepositoryException("Failed to read file: " + path, e);
    }
  }

  private void writeFile(Path path, List<String> lines) {
    try {
      Files.write(path, lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      throw new OrganizerRepositoryException("Failed to write to file: " + path, e);
    }
  }

  @Override
  public void addOrganizerInventory(String item, String containerId) {
    Map<String, List<String>> inventory = getOrganizerInventory();
    List<String> items = inventory.getOrDefault(containerId, new ArrayList<>());
    items.add(item);
    inventory.put(containerId, items);
    saveOrganizerInventory(inventory);
  }

  @Override
  public void deleteOrganizerInventory(String item, String containerId) {
    Map<String, List<String>> inventory = getOrganizerInventory();
    if (inventory.containsKey(containerId)) {
      List<String> items = inventory.get(containerId);
      items.removeIf(existingItem -> existingItem.equalsIgnoreCase(item));
      if (items.isEmpty()) {
        inventory.remove(containerId);
      } else {
        inventory.put(containerId, items);
      }
      saveOrganizerInventory(inventory);
    }
  }
}
