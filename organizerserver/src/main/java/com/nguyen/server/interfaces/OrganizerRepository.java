package com.nguyen.server.interfaces;

import java.util.List;

public interface OrganizerRepository {

  List<String> getTodoList() throws Exception;

  List<String> getOrganizerInventory();

  List<String> getContainerLocation();
}
