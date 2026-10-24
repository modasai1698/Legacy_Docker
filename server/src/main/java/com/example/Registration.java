package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A registration request plus its validation rules.
 *
 * <p>The password is intentionally excluded from {@link #toString()} so it can never
 * end up in a log file by accident.
 */
public record Registration(String name, String mobile, String email,
                           String password, String repeatPassword) {

  private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
  private static final Pattern MOBILE = Pattern.compile("^\\+?[0-9 ()-]{7,15}$");
  static final int MIN_PASSWORD_LENGTH = 8;
  static final int MAX_PASSWORD_LENGTH = 128;
  static final int MAX_NAME_LENGTH = 100;
  static final int MAX_EMAIL_LENGTH = 254;

  /**
   * Checks every field and returns all problems found (empty list = valid).
   */
  public List<String> validate() {
    List<String> errors = new ArrayList<>();

    if (isBlank(name) || name.strip().length() > MAX_NAME_LENGTH) {
      errors.add("Please enter your full name (up to " + MAX_NAME_LENGTH + " characters).");
    }
    if (isBlank(mobile) || !MOBILE.matcher(mobile.strip()).matches()) {
      errors.add("Please enter a valid mobile number (7-15 digits).");
    }
    if (isBlank(email) || email.strip().length() > MAX_EMAIL_LENGTH
        || !EMAIL.matcher(email.strip()).matches()) {
      errors.add("Please enter a valid email address.");
    }
    if (password == null || password.length() < MIN_PASSWORD_LENGTH
        || password.length() > MAX_PASSWORD_LENGTH) {
      errors.add("Password must be " + MIN_PASSWORD_LENGTH + "-" + MAX_PASSWORD_LENGTH
          + " characters long.");
    }
    if (password != null && !password.equals(repeatPassword)) {
      errors.add("The two passwords do not match.");
    }
    return errors;
  }

  public boolean isValid() {
    return validate().isEmpty();
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  @Override
  public String toString() {
    return "Registration[name=%s, mobile=%s, email=%s, password=***]"
        .formatted(name, mobile, email);
  }
}
