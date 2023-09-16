package com.nguyen.server.obsidian;

import com.nguyen.server.OrganizerRepositoryException;
import com.nguyen.server.interfaces.OrganizerRepositoryRead;
import com.nguyen.server.interfaces.OrganizerRepositoryWrite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ObsidianRepository implements OrganizerRepositoryRead, OrganizerRepositoryWrite {
    private String organizerLocation;
    private final static String ITEM_LOCATION_FILENAME = "ItemLocation.md";
    private final static String CONTAINER_LOCATION_FILENAME = "ContainerLocation.md";
    private final static String TODO_LIST_FILENAME = "Todo.md";
    public ObsidianRepository(String location){
        this.organizerLocation = location;
    }
    @Override
    public List<String> getTodoList() {
        return readFile(Path.of(organizerLocation).resolve(TODO_LIST_FILENAME));
    }

    @Override
    public List<String> getOrganizerInventory() {
        return readFile(Path.of(organizerLocation).resolve(ITEM_LOCATION_FILENAME));
    }

    @Override
    public List<String> getContainerLocation() {
        return readFile(Path.of(organizerLocation).resolve( CONTAINER_LOCATION_FILENAME));
    }

    private List<String> readFile(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new OrganizerRepositoryException(e);
        }
    }

    private void writeFile(Path path, List<String> lines){
        try {
            Files.write(path, lines);
        } catch (IOException e) {
            throw new OrganizerRepositoryException(e);
        }

    }

    @Override
    public synchronized void addOrganizerInventory(String item, String location) {
        List<String> lines = getOrganizerInventory();
        for( String line : lines){
            System.out.println(line);
        }

    }

    @Override
    public void deleteOrganizerInventory(String item, String location) {

    }
}
