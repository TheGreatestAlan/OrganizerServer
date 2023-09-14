package com.tippytappytoes.file;

import com.tippytappytoes.interfaces.OrganizerRepository;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileOrganizerRepo implements OrganizerRepository {

  private String todoList;
  private String inventory;
  private String containerLocation;

  public FileOrganizerRepo(String vaultLocation) {

    this.todoList = Path.of(vaultLocation, "Todo.md").toString();
    this.inventory = Path.of(vaultLocation, "ItemLocation.md").toString();
    this.containerLocation = Path.of(vaultLocation, "ContainerLocation.md").toString();
  }

  @Override
  public List<String> GetTodoList() {
    return readFile(todoList);
  }

  @Override
  public List<String> getOrganizerInventory() {
    return readFile(inventory);
  }

  @Override
  public List<String> getContainerLocation() {
    return readFile(containerLocation);
  }

  private List<String> readFile(String location) {
    try {
      return Files.readAllLines(Paths.get(location));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
