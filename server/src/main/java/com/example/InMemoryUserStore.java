package com.example;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Keeps users in memory only: restart the server and they are gone.
 * Used automatically when no database is configured.
 */
public final class InMemoryUserStore implements UserStore {

  private record Account(User user, byte[] salt, byte[] hash) { }

  private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();

  @Override
  public boolean register(Registration r) {
    if (!r.isValid()) {
      throw new IllegalArgumentException("Invalid registration");
    }
    String email = PasswordHasher.normalizeEmail(r.email());
    byte[] salt = PasswordHasher.newSalt();
    var account = new Account(
        new User(r.name().strip(), r.mobile().strip(), email),
        salt,
        PasswordHasher.hash(r.password(), salt));
    return accounts.putIfAbsent(email, account) == null;
  }

  @Override
  public Optional<User> authenticate(String email, String password) {
    if (email == null || password == null) {
      return Optional.empty();
    }
    Account account = accounts.get(PasswordHasher.normalizeEmail(email));
    if (account == null) {
      PasswordHasher.burnTime(password);
      return Optional.empty();
    }
    boolean ok = PasswordHasher.matches(password, account.salt(), account.hash());
    return ok ? Optional.of(account.user()) : Optional.empty();
  }
}
