package com;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.interfaces.Evernoteable;
import com.resources.TodoListResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class OrganizerServerApplication extends Application<OrganizerServerConfiguration> {

    public static void main(final String[] args) throws Exception {
        new OrganizerServerApplication().run(args);
    }

    @Override
    public String getName() {
        return "nguyen";
    }

    @Override
    public void run(final OrganizerServerConfiguration configuration,
                    final Environment environment) {
        Injector injector = Guice.createInjector(new ServerModule(configuration));
        final TodoListResource resource = new TodoListResource(injector.getInstance(Evernoteable.class));
        environment.jersey().register(resource);
    }

}
