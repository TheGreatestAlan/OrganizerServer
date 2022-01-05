package com.services;

import com.evernote.EvernoteApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Inventory {

  EvernoteApi evernoteApi;

  public Inventory(EvernoteApi evernoteApi) {
    this.evernoteApi = evernoteApi;
  }

  public List<String> getOrganizerInventory() {
    return evernoteApi.getOrganizerInventory();
  }

  public List<String> findItem(String itemName) {
    List<String> results = new ArrayList<>();
    List<String> itemLocations = getOrganizerInventory();
    for (String itemLocation : itemLocations) {
      String[] itemPair = itemLocation.split(":");
      String foundItem = "";
      boolean itemFoundInRow = false;
      for (String item : itemPair[1].split(",")) {
        if (item.toLowerCase(Locale.ROOT)
            .contains(itemName.toLowerCase(Locale.ROOT))) {
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
