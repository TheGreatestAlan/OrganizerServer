package com;

import com.evernote.EvernoteApi;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.interfaces.Evernoteable;

import javax.inject.Named;

public class ServerModule extends AbstractModule {

    nguyenConfiguration configuration;
    ServerModule(nguyenConfiguration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void configure() {
        bind(nguyenConfiguration.class).toInstance(configuration);
        bind(Evernoteable.class).to(EvernoteApi.class);
    }
}
