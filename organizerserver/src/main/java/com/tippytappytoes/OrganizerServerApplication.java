package com.tippytappytoes;

import com.tippytappytoes.evernote.EvernoteApi;
import com.tippytappytoes.interfaces.OrganizerRepository;
import com.tippytappytoes.resources.InventoryResource;
import com.tippytappytoes.services.Inventory;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class OrganizerServerApplication extends Application<OrganizerServerConfiguration> {

  public static void main(final String[] args) throws Exception {
    new OrganizerServerApplication().run(args);
  }

  @Override
  public void run(final OrganizerServerConfiguration configuration,
      final Environment environment) {
    OrganizerRepository organizerRepository = new EvernoteApi(configuration);

    Inventory inventory = new Inventory(organizerRepository);
    final InventoryResource inventoryResource = new InventoryResource(inventory);
    environment.jersey().register(inventoryResource);
  }

}
