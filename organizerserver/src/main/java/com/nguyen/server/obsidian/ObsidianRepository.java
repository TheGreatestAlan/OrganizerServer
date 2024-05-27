package com.nguyen.server.obsidian;

import com.nguyen.server.OrganizerRepositoryException;
import com.nguyen.server.interfaces.OrganizerRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

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
  public List<String> getOrganizerInventory() {
    return readFile(Path.of(organizerLocation, ITEM_LOCATION_FILENAME));
  }

  @Override
  public List<String> getContainerLocation() {
    return readFile(Path.of(organizerLocation, CONTAINER_LOCATION_FILENAME));
  }

  @Override
  public void saveOrganizerInventory(List<String> inventory) {
    writeFile(Path.of(organizerLocation, ITEM_LOCATION_FILENAME), inventory);
  }

  @Override
  public void saveContainerLocation(List<String> containerLocation) {
    writeFile(Path.of(organizerLocation, CONTAINER_LOCATION_FILENAME), containerLocation);
  }

  private List<String> readFile(Path path) {
    try {
      return Files.readAllLines(path);
    } catch (IOException e) {
      throw new OrganizerRepositoryException("Failed to read file: " + path, e);
    }
  }

  public void updateOrganizerInventory(List<String> inventory) {
    writeFile(Path.of(organizerLocation, ITEM_LOCATION_FILENAME), inventory);
  }

  public void updateContainerLocation(List<String> containerLocation) {
    writeFile(Path.of(organizerLocation, CONTAINER_LOCATION_FILENAME), containerLocation);
  }

  private void writeFile(Path path, List<String> lines) {
    try {
      Files.write(path, lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      throw new OrganizerRepositoryException("Failed to write to file: " + path, e);
    }
  }

  @Override
  public void addOrganizerInventory(String item, String location) {

  }

  @Override
  public void deleteOrganizerInventory(String item, String location) {

  }
}
