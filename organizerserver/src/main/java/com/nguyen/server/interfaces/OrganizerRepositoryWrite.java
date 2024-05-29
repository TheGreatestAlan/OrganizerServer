package com.nguyen.server.interfaces;

import java.util.List;
import java.util.Map;

public interface OrganizerRepositoryWrite {

  void saveOrganizerInventory(Map<String, List<String>> inventory);

  void addOrganizerInventory(String item, String location);

  void deleteOrganizerInventory(String item, String location);


}
