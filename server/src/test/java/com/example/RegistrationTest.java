package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RegistrationTest {

  private static Registration valid() {
    return new Registration("Asha Rao", "+91 98765 43210", "asha@example.com",
        "s3cret-pass", "s3cret-pass");
  }

  @Test
  void validRegistrationHasNoErrors() {
    assertTrue(valid().isValid());
  }

  @Test
  void blankNameIsRejected() {
    var r = new Registration(" ", "9876543210", "a@b.co", "longenough", "longenough");
    assertEquals(1, r.validate().size());
  }

  @Test
  void badEmailIsRejected() {
    var r = new Registration("Asha", "9876543210", "not-an-email", "longenough", "longenough");
    assertFalse(r.isValid());
  }

  @Test
  void badMobileIsRejected() {
    var r = new Registration("Asha", "abc", "a@b.co", "longenough", "longenough");
    assertFalse(r.isValid());
  }

  @Test
  void shortPasswordIsRejected() {
    var r = new Registration("Asha", "9876543210", "a@b.co", "short", "short");
    assertFalse(r.isValid());
  }

  @Test
  void mismatchedPasswordsAreRejected() {
    var r = new Registration("Asha", "9876543210", "a@b.co", "longenough1", "longenough2");
    assertEquals(1, r.validate().size());
  }

  @Test
  void nullFieldsDoNotThrow() {
    assertEquals(4, new Registration(null, null, null, null, null).validate().size());
  }

  @Test
  void toStringNeverContainsThePassword() {
    assertFalse(valid().toString().contains("s3cret-pass"));
  }

  @Test
  void tooLongValuesAreRejected() {
    String longName = "a".repeat(101);
    assertFalse(new Registration(longName, "9876543210", "a@b.co", "longenough", "longenough").isValid());
    String longEmail = "a".repeat(250) + "@b.co";
    assertFalse(new Registration("Asha", "9876543210", longEmail, "longenough", "longenough").isValid());
    String longPass = "p".repeat(129);
    assertFalse(new Registration("Asha", "9876543210", "a@b.co", longPass, longPass).isValid());
  }
}
