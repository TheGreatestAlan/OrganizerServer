package com.nguyen.server.interfaces;

import java.util.List;

public interface OrganizerRepository {

  List<String> getTodoList();

  List<String> getOrganizerInventory();

  List<String> getContainerLocation();

  void saveOrganizerInventory(List<String> inventory);

  void saveContainerLocation(List<String> containerLocation);
}