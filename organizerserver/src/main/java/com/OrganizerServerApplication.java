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
        //String token = System.getenv("AUTH_TOKEN");
        //String token = "S=s1:U=95a34:E=176c9f3bb22:C=16f72428e10:P=1cd:A=en-devtoken:V=2:H=5cff1bec49c8ecc113bf7afc5eacce33";
        //EvernoteApi demo = new EvernoteApi(token);
        //demo.GetTodoList();
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
