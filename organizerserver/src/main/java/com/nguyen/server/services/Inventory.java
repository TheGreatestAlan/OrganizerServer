package com.nguyen.server.services;

import com.nguyen.server.RateLimitException;
import com.nguyen.server.interfaces.OrganizerRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Inventory {

  OrganizerRepository organizerRepository;
  List<String> cachedOrganizerInventory;
  List<String> cachedContainerLocation;

  public Inventory(OrganizerRepository organizerRepository) {
    cachedOrganizerInventory = organizerRepository.getOrganizerInventory();
    cachedContainerLocation = organizerRepository.getContainerLocation();
    this.organizerRepository = organizerRepository;
  }

  public List<String> getOrganizerInventory() {
    try {
      cachedOrganizerInventory = organizerRepository.getOrganizerInventory();
      return cachedOrganizerInventory;
    } catch (RateLimitException e) {
      return cachedOrganizerInventory;
    }
  }


  public List<String> getContainerLocation() {
    try {
      cachedContainerLocation = organizerRepository.getContainerLocation();
      return cachedContainerLocation;
    } catch (RateLimitException e) {
      return cachedContainerLocation;
    }
  }

  public List<String> findItem(String itemName) {
    return findFromRepoTable(itemName, getOrganizerInventory());
  }

  public List<String> findContainerLocation(String containerId) {
    return findFromRepoTable(containerId, getContainerLocation());
  }

  public List<String> findFromRepoTable(String query, List<String> repoTable) {
    List<String> results = new ArrayList<>();
    for (String rows : repoTable) {
      if (rows.startsWith("#") || rows.isBlank() || rows.isEmpty()) {
        continue;
      }
      String[] itemPair = rows.split(":");
      String foundItem = "";
      boolean itemFoundInRow = false;
      if(itemPair.length < 2){
        //empty container
        continue;
      }
      for (String item : itemPair[1].split(",")) {
        if (item.toLowerCase(Locale.ROOT)
            .contains(query.toLowerCase(Locale.ROOT))) {
          if (!itemFoundInRow) {
            foundItem = itemPair[0] + ":";
          }
          itemFoundInRow = true;
          foundItem += item + ",";
        }
      }
      if (itemFoundInRow) {
        results.add(foundItem.substring(0, foundItem.length() - 1));
      }
    }
    return results;
  }

  public boolean createItemsInContainer(String containerId, List<String> items) {
    List<String> updatedInventory = new ArrayList<>();
    boolean updated = false;
    for (String row : cachedOrganizerInventory) {
      String[] itemPair = row.split(":");
      if (itemPair[0].trim().equals(containerId.trim())) {
        String existingItems = itemPair[1];
        String newItemString = items.stream()
            .map(String::trim)
            .collect(Collectors.joining(","));
        updatedInventory.add(
            itemPair[0] + ":" + existingItems + (existingItems.isEmpty() ? "" : ",")
                + newItemString);
        updated = true;
      } else {
        updatedInventory.add(row);
      }
    }
    if (updated) {
      cachedOrganizerInventory = updatedInventory;
      organizerRepository.saveOrganizerInventory(cachedOrganizerInventory);
    }
    return updated;
  }


  public boolean deleteItemsFromContainer(String containerId, List<String> itemsToDelete) {
    List<String> updatedInventory = new ArrayList<>();
    itemsToDelete = itemsToDelete.stream().map(String::trim).collect(Collectors.toList());
    boolean updated = false;
    for (String row : cachedOrganizerInventory) {
      String[] itemPair = row.split(":");
      if (itemPair[0].trim().equals(containerId.trim())) {
        List<String> items = new ArrayList<>(List.of(itemPair[1].split(",")));
        List<String> updatedItems = new ArrayList<>();
        for (String item : items) {
          if (!itemsToDelete.contains(item.trim())) {
            updatedItems.add(item.trim());
            updated = true;
          }
        }
        if (updated) { // Only update if items were actually removed
          row = itemPair[0] + ":" + String.join(",", updatedItems);
        }
      }
      updatedInventory.add(row);
    }
    if (updated) {
      cachedOrganizerInventory = updatedInventory;
      organizerRepository.saveOrganizerInventory(cachedOrganizerInventory);
    }
    return updated; // return whether the operation modified the inventory
  }
}
