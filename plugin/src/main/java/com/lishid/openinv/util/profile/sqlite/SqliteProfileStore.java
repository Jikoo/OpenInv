package com.lishid.openinv.util.profile.sqlite;

import com.github.jikoo.planarwrappers.function.ThrowingFunction;
import com.lishid.openinv.util.profile.BatchProfileStore;
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
import java.sql.Statement;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class SqliteProfileStore extends BatchProfileStore {

  private static final int BATCH_SIZE = 1_000;
  private static final int MAX_BATCHES = 20;

  private Connection connection;

  public SqliteProfileStore(@NotNull WrappedJavaPlugin plugin) {
    super(plugin);
  }

  @Override
  public void setup() throws Exception {
    // Touch implementation to ensure it is available, then create connection.
    Class.forName("org.sqlite.JDBC");
    connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/profiles.db");

    try (Statement statement = connection.createStatement()) {
      // Create main profile table.
      statement.executeUpdate(
          """
            CREATE TABLE IF NOT EXISTS profiles(
              name TEXT PRIMARY KEY,
              uuid_least INTEGER NOT NULL,
              uuid_most INTEGER NOT NULL,
              last_update TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            ) WITHOUT ROWID
            """
      );
      // Create meta table.
      statement.executeUpdate(
          """
            CREATE TABLE IF NOT EXISTS openinv_meta(
              name TEXT PRIMARY KEY,
              value INTEGER NOT NULL
            )
            """
      );
    }

    // Add JaroWinkler function to the connection.
    Function.create(connection, "JaroWinkler", new JaroWinklerFunction());

  }

  @Override
  public void shutdown() {
    super.shutdown();
    try {
      if (!connection.getAutoCommit()) {
        connection.commit();
      }
      connection.close();
    } catch (SQLException e) {
      plugin.getLogger().log(Level.WARNING, "Exception closing database: " + e.getMessage(), e);
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
          plugin.getLogger().log(Level.WARNING, "Exception marking import complete: " + e.getMessage(), e);
        }
      }
    }.runTaskAsynchronously(plugin);
  }

  @Override
  protected void pushBatch(@NotNull Set<Profile> batch) {
    if (batch.isEmpty()) {
      return;
    }

    int batchSize = Math.min(batch.size(), BATCH_SIZE);
    int rem = batch.size() % batchSize;

    Iterator<Profile> iterator = batch.iterator();

    try {
      boolean autoCommit = connection.getAutoCommit();
      if (autoCommit) {
        connection.setAutoCommit(false);
      }

      pushBatch(iterator, batch.size(), batchSize);

      if (rem > 0) {
        pushBatch(iterator, rem, rem);
      }

      connection.commit();

      if (autoCommit) {
        connection.setAutoCommit(true);
      }
    } catch (SQLException e) {
      // TODO retry on error? Might be best-handled at the BatchProfileStore level.
      plugin.getLogger().log(Level.WARNING, "Encountered an exception updating profiles", e);
    }

  }

  private void pushBatch(Iterator<Profile> iterator, int iteratorSize, int batchSize) throws SQLException {
    try (PreparedStatement upsert = createBulkUpsertProfile(batchSize)) {
      for (int batchIndex = 0; batchIndex < iteratorSize / batchSize; ++batchIndex) {
        for (int entryIndex = 0; entryIndex < batchSize; ++entryIndex) {
          int startIndex = entryIndex * 3;
          Profile profile = iterator.next();
          upsert.setString(startIndex + 1, profile.name());
          upsert.setLong(startIndex + 2, profile.id().getLeastSignificantBits());
          upsert.setLong(startIndex + 3, profile.id().getMostSignificantBits());
        }
        upsert.addBatch();

        // If we're at the maximum number of batches allowed, commit.
        if ((batchIndex + 1) % MAX_BATCHES == 0) {
          upsert.executeBatch();
          connection.commit();
        }
      }
      upsert.executeBatch();
    }
  }

  private @NotNull PreparedStatement createBulkUpsertProfile(int count) throws SQLException {
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
  public @Nullable Profile getProfileExact(@NotNull String name) {
    return getProfile(
        name,
        text -> {
          PreparedStatement statement = connection.prepareStatement(
              "SELECT name, uuid_least, uuid_most FROM `profiles` WHERE name = ?"
          );
          statement.setString(1, text);
          return statement;
        }
    );
  }

  @Override
  public @Nullable Profile getProfileInexact(@NotNull String search) {
    return getProfile(
        search,
        text -> {
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
    );
  }

  private @Nullable Profile getProfile(
      @NotNull String text,
      @NotNull ThrowingFunction<String, PreparedStatement, SQLException> create
  ) {
    try (PreparedStatement statement = create.apply(text)) {
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        // Fetch profile data.
        String name = resultSet.getString(1);

        // Ignore illegal data.
        if (name == null || name.isEmpty()) {
          return null;
        }

        UUID uuid = new UUID(resultSet.getLong(2), resultSet.getLong(3));
        return new Profile(name, uuid);
      }
    } catch (SQLException e) {
      plugin.getLogger().log(Level.WARNING, "Encountered an exception retrieving profile", e);
    }
    return null;
  }

}
