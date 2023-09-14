package com.tippytappytoes.file;


import org.junit.jupiter.api.Test;

public class FileOrganizerRepoTest {

  FileOrganizerRepo fileOrganizerRepo = new FileOrganizerRepo(
      "C:\\Users\\AlanH\\Documents\\SyncedVault\\Organizer");

  @Test
  public void todoList() throws Exception {
    for (String todo : fileOrganizerRepo.GetTodoList()) {
      System.out.println(todo);
    }

  }

}
