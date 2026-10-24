package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GreeterTest {

  private Greeter greeter;

  @BeforeEach
  void setUp() {
    greeter = new Greeter();
  }

  @Test
  void greetIncludesTheOneBeingGreeted() {
    assertTrue(greeter.greet("Asha").contains("Asha"));
  }

  @Test
  void greetIsLongerThanTheName() {
    assertTrue(greeter.greet("World").length() > "World".length());
  }

  @Test
  void greetFallsBackToWorldWhenNameIsMissing() {
    assertEquals("Hello, World!", greeter.greet(null));
    assertEquals("Hello, World!", greeter.greet("   "));
  }
}
