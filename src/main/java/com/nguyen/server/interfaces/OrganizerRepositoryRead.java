package com.nguyen.server.interfaces;

import java.util.List;
import java.util.Map;

public interface OrganizerRepositoryRead {

  List<String> getTodoList();

  Map<String, List<String>> getOrganizerInventory();

  List<String> getContainerLocation();

  List<String> getContainerById(String containerId);
}