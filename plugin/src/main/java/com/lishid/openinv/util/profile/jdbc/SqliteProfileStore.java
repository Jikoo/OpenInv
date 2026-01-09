package com.lishid.openinv.util.profile.jdbc;

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

public class SqliteProfileStore extends JdbcProfileStore {

  private static final String CREATE_TABLE =
      """
      CREATE TABLE IF NOT EXISTS profiles(
        name TEXT PRIMARY KEY,
        uuid_least INTEGER NOT NULL,
        uuid_most INTEGER NOT NULL,
        last_update TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
      ) WITHOUT ROWID
      """;
  private static final String INSERT_PROFILE =
      """
      INSERT INTO profiles(name, uuid_least, uuid_most)
        VALUES (?, ?, ?)
      """;
  private static final String BULK_INSERT = ", (?, ?, ?)";
  private static final String UPDATE_PROFILE =
      """
         ON CONFLICT(name) DO UPDATE SET
           uuid_least=excluded.uuid_least,
           uuid_most=excluded.uuid_most,
           last_update=CURRENT_TIMESTAMP
       """;
  private static final String SELECT_PROFILE =
      "SELECT name, uuid_least, uuid_most FROM `profiles` WHERE name = ?";
  private static final String MATCH_PROFILE =
      """
      SELECT name, uuid_least, uuid_most FROM `profiles`
          WHERE name like ?
          ORDER BY JaroWinkler(?, name) DESC
          LIMIT 1
      """;
  private static final String DELETE_BEFORE =
      "DELETE FROM `profiles` WHERE last_update < ?";

  private Connection connection;

  public SqliteProfileStore(@NotNull WrappedJavaPlugin plugin) throws Exception {
    super(plugin);
  }

  @Override
  protected void setup() throws Exception {
    Class.forName("org.sqlite.JDBC");
    connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/profiles.db");
    Function.create(connection, "JaroWinkler", new SqliteJaroWinkler());
    try (PreparedStatement statement = connection.prepareStatement(CREATE_TABLE)) {
      statement.execute();
    }
  }

  @Override
  protected @NotNull PreparedStatement createBulkUpsertProfile(int count) throws SQLException {
    return connection.prepareStatement(
        INSERT_PROFILE + BULK_INSERT.repeat(count - 1) + UPDATE_PROFILE
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
      PreparedStatement statement = connection.prepareStatement(SELECT_PROFILE);
      statement.setString(1, text);
    }

    PreparedStatement statement = connection.prepareStatement(MATCH_PROFILE);
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
