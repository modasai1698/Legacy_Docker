package com.example;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Salted PBKDF2 password hashing, shared by every {@link UserStore}. */
final class PasswordHasher {

  static final int SALT_BYTES = 16;
  static final int HASH_BYTES = 32;

  private static final int ITERATIONS = 120_000;
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final byte[] DUMMY_SALT = newSalt();

  private PasswordHasher() { }

  static byte[] newSalt() {
    byte[] salt = new byte[SALT_BYTES];
    RANDOM.nextBytes(salt);
    return salt;
  }

  static byte[] hash(String password, byte[] salt) {
    try {
      var spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_BYTES * 8);
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
          .generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Password hashing is not available", e);
    }
  }

  /** Constant-time check of a password against a stored salt + hash. */
  static boolean matches(String password, byte[] salt, byte[] expectedHash) {
    return MessageDigest.isEqual(expectedHash, hash(password, salt));
  }

  /** Spends the same time as a real check, so unknown emails can't be detected by timing. */
  static void burnTime(String password) {
    hash(password, DUMMY_SALT);
  }

  /** Emails are compared case-insensitively and without surrounding spaces. */
  static String normalizeEmail(String email) {
    return email.strip().toLowerCase(Locale.ROOT);
  }
}
