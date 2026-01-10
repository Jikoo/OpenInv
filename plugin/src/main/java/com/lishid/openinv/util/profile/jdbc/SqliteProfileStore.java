package com.lishid.openinv.util.profile.jdbc;

import com.lishid.openinv.util.profile.OfflinePlayerImporter;
import com.lishid.openinv.util.profile.Profile;
import me.nahu.scheduler.wrapper.WrappedJavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.Function;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class SqliteProfileStore extends JdbcProfileStore {

  private Connection connection;

  public SqliteProfileStore(@NotNull WrappedJavaPlugin plugin) {
    super(plugin);
  }

  @Override
  public void setup() throws Exception {
    // Touch implementation to ensure it is available, then create connection.
    Class.forName("org.sqlite.JDBC");
    connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/profiles.db");

    // Add JaroWinkler function to the connection.
    Function.create(connection, "JaroWinkler", new SqliteJaroWinkler());

    // Create main profile table.
    try (
        PreparedStatement createProfiles = connection.prepareStatement(
        """
            CREATE TABLE IF NOT EXISTS profiles(
              name TEXT PRIMARY KEY,
              uuid_least INTEGER NOT NULL,
              uuid_most INTEGER NOT NULL,
              last_update TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            ) WITHOUT ROWID
            """
        );
        PreparedStatement createMeta = connection.prepareStatement(
            """
            CREATE TABLE IF NOT EXISTS openinv_meta(
              name TEXT PRIMARY KEY,
              value INTEGER NOT NULL
            )
            """
        )
    ) {
      createProfiles.execute();
      createMeta.execute();
    }
  }

  @Override
  public void shutdown() {
    super.shutdown();
    try {
      connection.commit();
      connection.close();
    } catch (SQLException e) {
      logger.log(Level.WARNING, "Exception closing database: " + e.getMessage(), e);
    }
  }

  @Override
  public void tryImport() throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(
        "SELECT value FROM openinv_meta WHERE name = 'imported'"
    )) {
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next() && resultSet.getInt("value") == 1) {
        return;
      }
    }

    new OfflinePlayerImporter(this) {
      @Override
      public void onComplete() {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO openinv_meta(name, value) VALUES ('imported', 1)"
        )) {
          statement.executeQuery();
        } catch (SQLException e) {
          logger.log(Level.WARNING, "Exception marking import complete: " + e.getMessage(), e);
        }
      }
    }.runTaskAsynchronously(plugin);
  }

  @Override
  protected @NotNull PreparedStatement createBulkUpsertProfile(int count) throws SQLException {
    return connection.prepareStatement(
        """
        INSERT INTO profiles(name, uuid_least, uuid_most)
          VALUES (?, ?, ?)
        """ + ", (?, ?, ?)".repeat(count - 1) +
        """
         ON CONFLICT(name) DO UPDATE SET
           uuid_least=excluded.uuid_least,
           uuid_most=excluded.uuid_most,
           last_update=CURRENT_TIMESTAMP
       """
    );
  }

  @Override
  protected void addBulkProfile(
      @NotNull PreparedStatement upsert,
      int index,
      @NotNull Profile profile
  ) throws SQLException {
    index *= 3;
    upsert.setString(index + 1, profile.name());
    upsert.setLong(index + 2, profile.id().getLeastSignificantBits());
    upsert.setLong(index + 3, profile.id().getMostSignificantBits());
  }

  @Override
  protected @NotNull PreparedStatement createSelectProfile(
      @NotNull String text,
      boolean exact
  ) throws SQLException {
    if (exact) {
      PreparedStatement statement = connection.prepareStatement(
          "SELECT name, uuid_least, uuid_most FROM `profiles` WHERE name = ?"
      );
      statement.setString(1, text);
      return statement;
    }

    PreparedStatement statement = connection.prepareStatement(
        """
        SELECT name, uuid_least, uuid_most FROM `profiles`
          WHERE name LIKE ?
          ORDER BY JaroWinkler(?, name) DESC
          LIMIT 1
        """
    );
    String likePrefix = text.isEmpty() ? "%" : text.charAt(0) + "%";
    statement.setString(1, likePrefix);
    statement.setString(2, text);
    return statement;
  }

  @Override
  protected @Nullable Profile parseProfile(@NotNull ResultSet results) throws SQLException {
    // Fetch profile data.
    String name = results.getString(1);

    // Ignore illegal data.
    if (name == null || name.isEmpty()) {
      return null;
    }

    UUID uuid = new UUID(results.getLong(2), results.getLong(3));
    return new Profile(name, uuid);
  }

}
