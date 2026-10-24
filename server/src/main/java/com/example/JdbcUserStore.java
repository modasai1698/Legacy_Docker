package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * Stores users in a SQL database (written for MySQL, also works with MariaDB and H2).
 *
 * <p>The {@code users} table is created automatically on first start. Passwords are stored
 * as salted PBKDF2 hashes, never in clear text. All queries use prepared statements.
 */
public final class JdbcUserStore implements UserStore {

  private static final String CREATE_TABLE = """
      CREATE TABLE IF NOT EXISTS users (
        email         VARCHAR(254) NOT NULL PRIMARY KEY,
        name          VARCHAR(100) NOT NULL,
        mobile        VARCHAR(32)  NOT NULL,
        password_salt VARBINARY(16) NOT NULL,
        password_hash VARBINARY(32) NOT NULL,
        created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
      )""";

  private static final String INSERT =
      "INSERT INTO users (email, name, mobile, password_salt, password_hash) VALUES (?,?,?,?,?)";

  private static final String SELECT =
      "SELECT name, mobile, email, password_salt, password_hash FROM users WHERE email = ?";

  private final DataSource dataSource;

  /** Connects to the database and creates the table if it does not exist yet. */
  public JdbcUserStore(DataSource dataSource) {
    this.dataSource = dataSource;
    try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
      s.execute(CREATE_TABLE);
    } catch (SQLException e) {
      throw new IllegalStateException("Could not prepare the users table", e);
    }
  }

  @Override
  public boolean register(Registration r) {
    if (!r.isValid()) {
      throw new IllegalArgumentException("Invalid registration");
    }
    byte[] salt = PasswordHasher.newSalt();
    byte[] hash = PasswordHasher.hash(r.password(), salt);

    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(INSERT)) {
      ps.setString(1, PasswordHasher.normalizeEmail(r.email()));
      ps.setString(2, r.name().strip());
      ps.setString(3, r.mobile().strip());
      ps.setBytes(4, salt);
      ps.setBytes(5, hash);
      ps.executeUpdate();
      return true;
    } catch (SQLException e) {
      // SQLState class 23 = integrity constraint violation (the email already exists)
      if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
        return false;
      }
      throw new IllegalStateException("Database error while registering", e);
    }
  }

  @Override
  public Optional<User> authenticate(String email, String password) {
    if (email == null || password == null) {
      return Optional.empty();
    }
    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(SELECT)) {
      ps.setString(1, PasswordHasher.normalizeEmail(email));
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          PasswordHasher.burnTime(password);
          return Optional.empty();
        }
        boolean ok = PasswordHasher.matches(
            password, rs.getBytes("password_salt"), rs.getBytes("password_hash"));
        return ok
            ? Optional.of(new User(rs.getString("name"), rs.getString("mobile"),
                rs.getString("email")))
            : Optional.empty();
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Database error while signing in", e);
    }
  }
}
