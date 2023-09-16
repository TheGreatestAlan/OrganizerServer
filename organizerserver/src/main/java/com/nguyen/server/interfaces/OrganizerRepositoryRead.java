package com.nguyen.server.interfaces;

import java.util.List;

public interface OrganizerRepositoryRead {

  List<String> getTodoList();

  List<String> getOrganizerInventory();

  List<String> getContainerLocation();
}
