package com.nguyen.server;

import com.nguyen.server.evernote.EvernoteApi;
import com.nguyen.server.obsidian.ObsidianRepository;
import com.nguyen.server.resources.InventoryResource;
import com.nguyen.server.services.Inventory;
import com.nguyen.server.interfaces.OrganizerRepository;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class OrganizerServerApplication extends Application<OrganizerServerConfiguration> {

  public static void main(final String[] args) throws Exception {
    new OrganizerServerApplication().run(args);
  }

  @Override
  public void run(final OrganizerServerConfiguration configuration,
      final Environment environment) {
    OrganizerRepository organizerRepository = new ObsidianRepository(configuration.getObsidianOrganizerVaultLocation());

    Inventory inventory = new Inventory(organizerRepository);
    final InventoryResource inventoryResource = new InventoryResource(inventory);
    environment.jersey().register(inventoryResource);
  }

}
