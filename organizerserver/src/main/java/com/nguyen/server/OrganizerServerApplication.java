package com.nguyen.server;

import com.nguyen.server.interfaces.OrganizerRepositoryWrite;
import com.nguyen.server.obsidian.ObsidianRepository;
import com.nguyen.server.resources.InventoryResource;
import com.nguyen.server.services.Inventory;
import com.nguyen.server.interfaces.OrganizerRepositoryRead;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class OrganizerServerApplication extends Application<OrganizerServerConfiguration> {

  public static void main(final String[] args) throws Exception {
    new OrganizerServerApplication().run(args);
  }

  @Override
  public void initialize(Bootstrap<OrganizerServerConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
  }

  @Override
  public void run(final OrganizerServerConfiguration configuration,
      final Environment environment) {
    OrganizerRepositoryRead organizerRepositoryRead = new ObsidianRepository(configuration.getObsidianOrganizerVaultLocation());
    OrganizerRepositoryWrite organizerRepositoryWrite = null;

    Inventory inventory = new Inventory(organizerRepositoryRead, organizerRepositoryWrite);
    final InventoryResource inventoryResource = new InventoryResource(inventory);
    environment.jersey().register(inventoryResource);
  }

}
