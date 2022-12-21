package com.tippytappytoes.google;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.junit.jupiter.api.Test;

public class TestApp {

  @Test
  public void intTest() {
    try {
      KeepApi.run();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }

  }

}
