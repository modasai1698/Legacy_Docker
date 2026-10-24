package com.example;

/**
 * Builds friendly greeting messages.
 */
public class Greeter {

  /**
   * Greets someone by name.
   *
   * @param someone the person to greet; blank or {@code null} falls back to "World"
   * @return a greeting such as {@code "Hello, World!"}
   */
  public String greet(String someone) {
    String name = (someone == null || someone.isBlank()) ? "World" : someone.strip();
    return "Hello, %s!".formatted(name);
  }
}
