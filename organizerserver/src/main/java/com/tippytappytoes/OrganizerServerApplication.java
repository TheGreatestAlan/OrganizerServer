package com.tippytappytoes;

import com.tippytappytoes.file.FileOrganizerRepo;
import com.tippytappytoes.resources.InventoryResource;
import com.tippytappytoes.services.Inventory;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;

public class OrganizerServerApplication extends Application<OrganizerServerConfiguration> {

  public static void main(final String[] args) throws Exception {
    new OrganizerServerApplication().run(args);
  }

  @Override
  public void initialize(final Bootstrap<OrganizerServerConfiguration> bootstrap) {
    // Enable environment variable substitution with a prefix of 'dw'
    boolean strict = false; // set to true to throw exception if variable is not defined
    bootstrap.setConfigurationSourceProvider(
            new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                    new EnvironmentVariableSubstitutor(strict))
    );
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
