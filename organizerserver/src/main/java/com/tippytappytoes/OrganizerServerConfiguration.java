package com.tippytappytoes;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrganizerServerConfiguration extends Configuration {

  private String obsidianVaultRepoLocation;

  public String getObsidianVaultRepoLocation() {
    return obsidianVaultRepoLocation;
  }

  @JsonProperty
  public void setObsidianVaultRepoLocation(String obsidianVaultRepoLocation) {
    this.obsidianVaultRepoLocation = obsidianVaultRepoLocation;

  }
}
