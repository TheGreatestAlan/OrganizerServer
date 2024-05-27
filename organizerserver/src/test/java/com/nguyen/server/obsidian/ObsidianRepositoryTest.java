package com.nguyen.server.obsidian;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObsidianRepositoryTest {

  ObsidianRepository obsidianRepository;
  private static final String ORGANIZER_LOCATION = "C:\\workspace\\OrganizerServer\\organizerserver\\src\\test\\resources";

  @BeforeEach
  public void setup() {
    obsidianRepository = new ObsidianRepository(ORGANIZER_LOCATION);
  }

  @Test
  public void getOrganizerInventory() throws IOException {
    List<String> expectedItems = Files.readAllLines(
        Path.of(ORGANIZER_LOCATION + "\\ItemLocation.md"));
    List<String> actualItems = obsidianRepository.getOrganizerInventory();

    assertEquals(expectedItems, actualItems);
  }

  @Test
  public void getContainerLocation() throws IOException {
    List<String> expectedItems = Files.readAllLines(
        Path.of(ORGANIZER_LOCATION + "\\ContainerLocation.md"));
    List<String> actualItems = obsidianRepository.getContainerLocation();

    assertEquals(expectedItems, actualItems);
  }

  @Test
  public void getTodoList() throws IOException {
    List<String> expectedItems = Files.readAllLines(Path.of(ORGANIZER_LOCATION + "\\Todo.md"));
    List<String> actualItems = obsidianRepository.getTodoList();

    assertEquals(expectedItems, actualItems);
  }
}
