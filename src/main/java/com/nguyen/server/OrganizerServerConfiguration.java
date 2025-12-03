package com.nguyen.server;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;

public class OrganizerServerConfiguration extends Configuration {

  @NotEmpty
  private String obsidianOrganizerVaultLocation;
  @NotEmpty
  private String noteBaseLocation;

  @NotEmpty
  private String baseGitRepoLocation;

  @NotEmpty
  private String gitToken;

  @NotEmpty
  private String gitUser;

  @JsonProperty
  public String getObsidianOrganizerVaultLocation() {
    return obsidianOrganizerVaultLocation;
  }

  @JsonProperty
  public void setObsidianOrganizerVaultLocation(String obsidianOrganizerVaultLocation) {
    this.obsidianOrganizerVaultLocation = obsidianOrganizerVaultLocation;
  }
  @JsonProperty
  public String getNoteBaseLocation() {
    return noteBaseLocation;
  }

  @JsonProperty
  public void setNoteBaseLocation(String noteBaseLocation) {
    this.noteBaseLocation = noteBaseLocation;
  }
  @JsonProperty
  public String getBaseGitRepoLocation() {
    return baseGitRepoLocation;
  }

  @JsonProperty
  public void setBaseGitRepoLocation(String baseGitRepoLocation) {
    this.baseGitRepoLocation = baseGitRepoLocation;
  }

  @JsonProperty
  public String getGitToken() {
    return gitToken;
  }

  @JsonProperty
  public void setGitToken(String gitToken) {
    this.gitToken = gitToken;
  }

  @JsonProperty
  public String getGitUser() {
    return gitUser;
  }

  @JsonProperty
  public void setGitUser(String gitUser) {
    this.gitUser = gitUser;
  }
}
