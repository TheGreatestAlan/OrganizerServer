package com.tippytappytoes.interfaces;

import java.util.List;

public interface OrganizerRepository {

  List<String> GetTodoList() throws Exception;

  List<String> getOrganizerInventory();

  List<String> getContainerLocation();
}
