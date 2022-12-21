package com.tippytappytoes.services;

import com.tippytappytoes.RateLimitException;
import com.tippytappytoes.interfaces.OrganizerRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
      String[] itemPair = rows.split(":");
      String foundItem = "";
      boolean itemFoundInRow = false;
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
}
