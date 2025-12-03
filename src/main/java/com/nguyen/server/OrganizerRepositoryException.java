package com.nguyen.server;

public class OrganizerRepositoryException extends OrganizerServerException {

  public OrganizerRepositoryException(Throwable e) {
    super(e);
  }

  public OrganizerRepositoryException(String message) {
    super(message);
  }

  public OrganizerRepositoryException(String message, Throwable e) {
    super(message, e);
  }
}
