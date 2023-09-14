package com.tippytappytoes;

import com.tippytappytoes.file.FileOrganizerRepo;
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
    FileOrganizerRepo fileOrganizerRepo = new FileOrganizerRepo(
        configuration.getObsidianVaultRepoLocation());
    fileOrganizerRepo.getOrganizerInventory();
    Inventory inventory = new Inventory(fileOrganizerRepo);
    final InventoryResource inventoryResource = new InventoryResource(inventory);
    environment.jersey().register(inventoryResource);
  }

}
