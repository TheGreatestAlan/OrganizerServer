package com.nguyen.server;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;

public class OrganizerServerConfiguration extends Configuration {

  @NotEmpty
  private String obsidianOrganizerVaultLocation;

  @JsonProperty
  public String getObsidianOrganizerVaultLocation() {
    return obsidianOrganizerVaultLocation;
  }

  @JsonProperty
  public void setObsidianOrganizerVaultLocation(String obsidianOrganizerVaultLocation) {
    this.obsidianOrganizerVaultLocation = obsidianOrganizerVaultLocation;
  }
}
