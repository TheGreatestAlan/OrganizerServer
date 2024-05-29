package com.nguyen.server.services;

import com.nguyen.server.RateLimitException;
import com.nguyen.server.interfaces.OrganizerRepository;

import java.util.*;
import java.util.stream.Collectors;

public class Inventory {

  OrganizerRepository organizerRepository;
  Map<String, List<String>> cachedOrganizerInventory;
  List<String> cachedContainerLocation;

  public Inventory(OrganizerRepository organizerRepository) {
    this.organizerRepository = organizerRepository;
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
    return cachedOrganizerInventory;
  }

  private List<String> convertInventoryMapToList(Map<String, List<String>> inventoryMap) {
    List<String> inventoryList = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : inventoryMap.entrySet()) {
      String containerId = entry.getKey();
      String items = String.join(",", entry.getValue());
      inventoryList.add(containerId + ":" + items);
    }
    return inventoryList;
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
    List<String> results = new ArrayList<>();
    String query = itemName.toLowerCase(Locale.ROOT);

    for (Map.Entry<String, List<String>> entry : cachedOrganizerInventory.entrySet()) {
      String containerId = entry.getKey();
      List<String> items = entry.getValue();
      List<String> matchingItems = items.stream()
          .map(String::trim)
          .filter(item -> item.toLowerCase(Locale.ROOT).contains(query))
          .collect(Collectors.toList());
      if (!matchingItems.isEmpty()) {
        results.add(containerId + ":" + String.join(",", matchingItems));
      }
    }

    return results;
  }

  public List<String> getContainerById(String containerId) {
    return cachedOrganizerInventory.getOrDefault(containerId.trim(), Collections.emptyList());
  }

  public List<String> findContainerLocation(String containerId) {
    String query = containerId.toLowerCase(Locale.ROOT);
    return cachedContainerLocation.stream()
        .filter(location -> location.toLowerCase(Locale.ROOT).contains(query))
        .collect(Collectors.toList());
  }

  public boolean createItemsInContainer(String containerId, List<String> items) {
    containerId = containerId.trim();
    items = items.stream().map(String::trim).collect(Collectors.toList());
    boolean updated = false;

    if (cachedOrganizerInventory.containsKey(containerId)) {
      List<String> existingItems = cachedOrganizerInventory.get(containerId);
      existingItems.addAll(items);
      cachedOrganizerInventory.put(containerId, existingItems);
      updated = true;
    } else {
      cachedOrganizerInventory.put(containerId, new ArrayList<>(items));
      updated = true;
    }

    if (updated) {
      organizerRepository.saveOrganizerInventory(cachedOrganizerInventory);
    }

    return updated;
  }

  public boolean deleteItemsFromContainer(String containerId, List<String> itemsToDelete) {
    containerId = containerId.trim();
    itemsToDelete = itemsToDelete.stream()
        .map(String::trim)
        .map(String::toLowerCase)
        .collect(Collectors.toList());
    boolean updated = false;

    if (cachedOrganizerInventory.containsKey(containerId)) {
      List<String> items = cachedOrganizerInventory.get(containerId);
      List<String> finalItemsToDelete = itemsToDelete;
      List<String> updatedItems = items.stream()
          .filter(item -> !finalItemsToDelete.contains(item.trim().toLowerCase()))
          .collect(Collectors.toList());
      if (updatedItems.size() != items.size()) {
        cachedOrganizerInventory.put(containerId, updatedItems);
        updated = true;
      }
    }

    if (updated) {
      organizerRepository.saveOrganizerInventory(cachedOrganizerInventory);
    }

    return updated;
  }
}
