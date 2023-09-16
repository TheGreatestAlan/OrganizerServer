package com.nguyen.server.interfaces;

public interface OrganizerRepositoryWrite {
    void addOrganizerInventory(String item, String location);

    void deleteOrganizerInventory(String item, String location);


}
