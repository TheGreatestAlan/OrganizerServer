package com;

import com.evernote.EvernoteApi;
import com.google.inject.AbstractModule;
import com.interfaces.Evernoteable;

public class ServerModule extends AbstractModule {

    OrganizerServerConfiguration configuration;
    ServerModule(OrganizerServerConfiguration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void configure() {
        bind(OrganizerServerConfiguration.class).toInstance(configuration);
        bind(Evernoteable.class).to(EvernoteApi.class);
    }
}
