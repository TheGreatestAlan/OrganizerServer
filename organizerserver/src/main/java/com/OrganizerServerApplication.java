package com;

import com.evernote.EvernoteApi;
import com.resources.InventoryResource;
import com.services.Inventory;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class OrganizerServerApplication extends Application<OrganizerServerConfiguration> {

  public static void main(final String[] args) throws Exception {
    new OrganizerServerApplication().run(args);
  }

  @Override
  public void run(final OrganizerServerConfiguration configuration,
      final Environment environment) {
    EvernoteApi evernoteApi = new EvernoteApi(configuration);
    Inventory inventory = new Inventory(evernoteApi);
    final InventoryResource inventoryResource = new InventoryResource(inventory);
    environment.jersey().register(inventoryResource);
  }

}
