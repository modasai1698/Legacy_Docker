package com.example;

import java.util.Optional;

/**
 * Where user accounts are kept. Two implementations exist:
 * {@link InMemoryUserStore} (demo, lost on restart) and {@link JdbcUserStore} (MySQL, persistent).
 */
public interface UserStore {

  /** What the rest of the app is allowed to know about a user (never the password). */
  record User(String name, String mobile, String email) { }

  /**
   * Saves a new user.
   *
   * @return {@code true} if created, {@code false} if that email is already registered
   * @throws IllegalArgumentException if the registration is not valid
   */
  boolean register(Registration registration);

  /** Returns the user if the email and password match, otherwise empty. */
  Optional<User> authenticate(String email, String password);
}
