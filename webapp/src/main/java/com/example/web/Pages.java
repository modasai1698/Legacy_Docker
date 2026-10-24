package com.example.web;

/** Shared HTML layout so every page looks the same. */
final class Pages {

  private Pages() { }

  private static final String BRAND = """
      <div class="brand">
        <svg viewBox="0 0 48 48" width="44" height="44" aria-hidden="true">
          <rect width="48" height="48" rx="12" fill="#6366f1"/>
          <path d="M14 26l7 7 13-16" fill="none" stroke="#fff" stroke-width="4"
                stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>DevOps Learning</span>
      </div>
      """;

  /** Left-aligned card with the brand header (used for forms). */
  static String form(String ctx, String title, String body) {
    return layout(ctx, title, "card", BRAND + body);
  }

  /** Centred card (used for results and the welcome page). */
  static String result(String ctx, String title, String body) {
    return layout(ctx, title, "card result", body);
  }

  private static String layout(String ctx, String title, String cssClass, String body) {
    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>%s &middot; DevOps Learning</title>
          <link rel="stylesheet" href="%s/assets/style.css">
        </head>
        <body>
          <main class="%s">
        %s
          </main>
        </body>
        </html>
        """.formatted(Html.escape(title), Html.escape(ctx), cssClass, body);
  }
}
