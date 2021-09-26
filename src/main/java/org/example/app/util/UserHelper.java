package org.example.app.util;

import org.example.app.domain.User;
import org.example.framework.security.Authentication;

public class UserHelper {
  private UserHelper() {
  }

  public static User getUser(Authentication auth) {
    return (User) auth.getPrincipal();
  }
}
