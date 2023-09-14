package com.tippytappytoes.interfaces;

import java.util.List;

public interface OrganizerRepository {

  List<String> GetTodoList();

  List<String> getOrganizerInventory();

  List<String> getContainerLocation();
}
