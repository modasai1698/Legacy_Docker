package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Behaviour every {@link UserStore} must have. Subclasses just supply the store. */
abstract class UserStoreContract {

  protected UserStore store;

  protected abstract UserStore createStore() throws Exception;

  private static Registration reg(String email, String password) {
    return new Registration("Asha Rao", "9876543210", email, password, password);
  }

  @BeforeEach
  void setUp() throws Exception {
    store = createStore();
  }

  @Test
  void registeredUserCanLogIn() {
    assertTrue(store.register(reg("asha@example.com", "s3cret-pass")));

    var user = store.authenticate("asha@example.com", "s3cret-pass");

    assertTrue(user.isPresent());
    assertEquals("Asha Rao", user.get().name());
    assertEquals("9876543210", user.get().mobile());
    assertEquals("asha@example.com", user.get().email());
  }

  @Test
  void emailIsCaseInsensitive() {
    store.register(reg("Asha@Example.com", "s3cret-pass"));
    assertTrue(store.authenticate("  asha@example.COM ", "s3cret-pass").isPresent());
  }

  @Test
  void wrongPasswordIsRejected() {
    store.register(reg("asha@example.com", "s3cret-pass"));
    assertFalse(store.authenticate("asha@example.com", "wrong-pass").isPresent());
  }

  @Test
  void unknownUserIsRejected() {
    assertFalse(store.authenticate("nobody@example.com", "whatever1").isPresent());
  }

  @Test
  void duplicateEmailIsNotRegisteredTwice() {
    assertTrue(store.register(reg("asha@example.com", "s3cret-pass")));
    assertFalse(store.register(reg("ASHA@example.com", "other-pass1")));
    // the original password still works, the second one does not
    assertTrue(store.authenticate("asha@example.com", "s3cret-pass").isPresent());
    assertFalse(store.authenticate("asha@example.com", "other-pass1").isPresent());
  }

  @Test
  void invalidRegistrationIsRefused() {
    assertThrows(IllegalArgumentException.class, () -> store.register(reg("bad", "x")));
  }

  @Test
  void nullCredentialsAreRejected() {
    assertFalse(store.authenticate(null, null).isPresent());
  }

  @Test
  void sqlInjectionAttemptsAreJustWrongCredentials() {
    store.register(reg("asha@example.com", "s3cret-pass"));
    assertFalse(store.authenticate("' OR '1'='1", "' OR '1'='1").isPresent());
    assertFalse(store.authenticate("asha@example.com' --", "x").isPresent());
  }
}
