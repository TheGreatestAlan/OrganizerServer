package com.nguyen.server;

import com.nguyen.server.interfaces.OrganizerRepository;
import com.nguyen.server.repository.H2InventoryRepository;
import com.nguyen.server.resources.InventoryResource;
import com.nguyen.server.services.Inventory;
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
        bootstrap.setConfigurationSourceProvider(
            new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)
            )
        );
    }

    @Override
    public void run(final OrganizerServerConfiguration configuration,
                    final Environment environment) {

        String dbUrl = configuration.getDatabaseUrl();
        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = "jdbc:h2:file:./inventory.db;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
        }

        OrganizerRepository organizerRepository = new H2InventoryRepository(dbUrl);
        Inventory inventory = new Inventory(organizerRepository);

        final InventoryResource inventoryResource = new InventoryResource(inventory);
        environment.jersey().register(inventoryResource);
    }
}
