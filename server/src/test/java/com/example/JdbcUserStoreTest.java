package com.example;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

/** Runs the same contract against a real SQL engine (H2 in memory, no setup needed). */
class JdbcUserStoreTest extends UserStoreContract {

  private JdbcDataSource ds;

  @Override
  protected UserStore createStore() {
    ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
    return new JdbcUserStore(ds);
  }

  @Test
  void usersSurviveANewStoreOnTheSameDatabase() {
    store.register(new Registration("Asha Rao", "9876543210", "asha@example.com",
        "s3cret-pass", "s3cret-pass"));

    // simulates restarting the application: new store object, same database
    var restarted = new JdbcUserStore(ds);

    assertTrue(restarted.authenticate("asha@example.com", "s3cret-pass").isPresent());
  }

  @Test
  void passwordIsNeverStoredInClearText() throws Exception {
    store.register(new Registration("Asha Rao", "9876543210", "asha@example.com",
        "s3cret-pass", "s3cret-pass"));

    try (var c = ds.getConnection();
         var rs = c.createStatement().executeQuery("SELECT * FROM users")) {
      rs.next();
      for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
        Object v = rs.getObject(i);
        String text = v instanceof byte[] b ? new String(b, java.nio.charset.StandardCharsets.ISO_8859_1)
                                            : String.valueOf(v);
        assertTrue(!text.contains("s3cret-pass"), "column " + i + " leaks the password");
      }
    }
  }
}
