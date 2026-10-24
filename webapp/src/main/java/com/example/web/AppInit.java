package com.example.web;

import com.example.InMemoryUserStore;
import com.example.JdbcUserStore;
import com.example.UserStore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Creates the shared {@link UserStore} when the application starts.
 *
 * <ul>
 *   <li>{@code DB_URL} set   &rarr; users are stored in MySQL (persistent).</li>
 *   <li>{@code DB_URL} unset &rarr; users are kept in memory (demo mode).</li>
 * </ul>
 *
 * <p>Settings (environment variables): {@code DB_URL} e.g.
 * {@code jdbc:mysql://db:3306/regapp}, {@code DB_USER}, {@code DB_PASSWORD}.
 */
@WebListener
public class AppInit implements ServletContextListener {

  static final String USERS = "users";
  private static final String DATA_SOURCE = "dataSource";

  @Override
  public void contextInitialized(ServletContextEvent event) {
    ServletContext ctx = event.getServletContext();
    String url = env("DB_URL");

    if (url == null) {
      ctx.setAttribute(USERS, new InMemoryUserStore());
      ctx.log("DB_URL is not set: using the IN-MEMORY user store (users are lost on restart).");
      return;
    }

    var config = new HikariConfig();
    config.setJdbcUrl(url);
    config.setUsername(env("DB_USER"));
    config.setPassword(env("DB_PASSWORD"));
    config.setMaximumPoolSize(10);
    config.setPoolName("regapp");
    // Wait up to 60 s for the database to come up (e.g. MySQL still starting in Docker).
    config.setInitializationFailTimeout(60_000);
    String driver = env("DB_DRIVER");
    if (driver != null) {
      config.setDriverClassName(driver);
    } else if (url.startsWith("jdbc:mysql:")) {
      config.setDriverClassName("com.mysql.cj.jdbc.Driver");
    }

    var dataSource = new HikariDataSource(config);
    ctx.setAttribute(DATA_SOURCE, dataSource);
    ctx.setAttribute(USERS, new JdbcUserStore(dataSource));
    ctx.log("Using the MySQL/JDBC user store (persistent).");
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    Object ds = event.getServletContext().getAttribute(DATA_SOURCE);
    if (ds instanceof HikariDataSource hikari) {
      hikari.close();
    }
  }

  private static String env(String name) {
    String v = System.getenv(name);
    return (v == null || v.isBlank()) ? null : v.strip();
  }
}
