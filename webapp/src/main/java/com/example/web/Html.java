package com.example.web;

/** Minimal HTML escaping so user input can never inject markup. */
final class Html {

  private Html() { }

  static String escape(String s) {
    if (s == null) {
      return "";
    }
    var sb = new StringBuilder(s.length() + 16);
    for (char c : s.toCharArray()) {
      switch (c) {
        case '&' -> sb.append("&amp;");
        case '<' -> sb.append("&lt;");
        case '>' -> sb.append("&gt;");
        case '"' -> sb.append("&quot;");
        case '\'' -> sb.append("&#39;");
        default -> sb.append(c);
      }
    }
    return sb.toString();
  }
}
