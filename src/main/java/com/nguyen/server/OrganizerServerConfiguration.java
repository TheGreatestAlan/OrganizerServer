package com.nguyen.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotEmpty;

public class OrganizerServerConfiguration extends Configuration {

    @NotEmpty
    private String databaseUrl;

    @JsonProperty
    public String getDatabaseUrl() {
        return databaseUrl;
    }

    @JsonProperty
    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }
}
