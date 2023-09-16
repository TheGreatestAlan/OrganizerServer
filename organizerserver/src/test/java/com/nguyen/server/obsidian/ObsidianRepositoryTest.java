package com.nguyen.server.obsidian;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObsidianRepositoryTest {
    ObsidianRepository obsidianRepository;

    private static final String ORGANIZER_LOCATION = "C:\\workspace\\OrganizerServer\\organizerserver\\src\\test\\resources";
    @BeforeEach
    public void setup(){

        List<String> tempFiles = null;
        try {
            tempFiles = Files.readAllLines(Path.of(ObsidianRepositoryTest.class.getResource("/ContainerLocation.md").toURI()));
            tempFiles = Files.write(Path.of(ObsidianRepositoryTest.class.getResource("/ContainerLocation.md").toURI()., tempFiles);


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        for(String tempFile : tempFiles){
            System.out.println(tempFile);
        }
        obsidianRepository = new ObsidianRepository(ORGANIZER_LOCATION);
    }

    @Test
    @Disabled
    public void getOrganizerInventory() throws IOException {
        List<String> expectedItems = Files.readAllLines(Path.of(ORGANIZER_LOCATION + "\\ItemLocation.md"));
        List<String> actualItems = obsidianRepository.getOrganizerInventory();

        assertEquals(expectedItems, actualItems);
    }

    @Test
    @Disabled
    public void getContainerLocation() throws IOException {
        List<String> expectedItems = Files.readAllLines(Path.of(ORGANIZER_LOCATION + "\\ContainerLocation.md"));
        List<String> actualItems = obsidianRepository.getContainerLocation();

        assertEquals(expectedItems, actualItems);
    }

    @Test
    @Disabled
    public void getTodoList() throws IOException {
        List<String> expectedItems = Files.readAllLines(Path.of(ORGANIZER_LOCATION + "\\Todo.md"));
        List<String> actualItems = obsidianRepository.getTodoList();

        assertEquals(expectedItems, actualItems);
    }

    @Test
    public void addItem() {
        assertTrue(true);

    }
}
