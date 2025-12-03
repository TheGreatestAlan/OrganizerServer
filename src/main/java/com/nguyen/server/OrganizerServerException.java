package com.nguyen.server;

public class OrganizerServerException extends RuntimeException {

  public OrganizerServerException(Throwable e) {
    super(e);
  }

  public OrganizerServerException(String message){
    super(message);
  }

  public OrganizerServerException(String message, Throwable e) {
    super(message, e);
  }
}
