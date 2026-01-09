package com.lishid.openinv.util.profile.jdbc;

import com.lishid.openinv.util.profile.BatchProfileStore;
import com.lishid.openinv.util.profile.Profile;
import me.nahu.scheduler.wrapper.WrappedJavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class JdbcProfileStore extends BatchProfileStore {

  private static final int BATCH_SIZE = 1_000;

  protected final @NotNull Logger logger;

  public JdbcProfileStore(@NotNull WrappedJavaPlugin plugin) throws Exception {
    super(plugin);
    this.logger = plugin.getLogger();
    setup();
  }

  protected abstract void setup() throws Exception;

  @Override
  protected void pushBatch(@NotNull Set<Profile> batch) {
    if (batch.isEmpty()) {
      return;
    }

    int batchSize = Math.min(batch.size(), BATCH_SIZE);
    int rem = batch.size() % batchSize;

    Iterator<Profile> iterator = batch.iterator();

    try {
      for (int batchIndex = 0; batchIndex < batch.size() / batchSize; ++batchIndex) {
        pushBatch(iterator, batchSize);
      }

      if (rem > 0) {
        pushBatch(iterator, rem);
      }
    } catch (SQLException e) {
      logger.log(Level.WARNING, "Encountered an exception updating profiles", e);
    }

  }

  private void pushBatch(Iterator<Profile> iterator, int batchSize) throws SQLException {
    try (PreparedStatement upsert = createBulkUpsertProfile(batchSize)) {
      for (int index = 0; index < batchSize; index++) {
        addBulkProfile(upsert, index, iterator.next());
      }
      upsert.execute();
    }
  }

  @Contract("_ -> new")
  protected abstract @NotNull PreparedStatement createBulkUpsertProfile(
      int count
  ) throws SQLException;

  protected abstract void addBulkProfile(
      @NotNull PreparedStatement upsert,
      int index,
      @NotNull Profile profile
  ) throws SQLException;

  @Override
  public @Nullable Profile getProfileExact(@NotNull String name) {
    return getProfile(name, true);
  }

  @Override
  public @Nullable Profile getProfileInexact(@NotNull String search) {
    return getProfile(search, false);
  }

  private @Nullable Profile getProfile(@NotNull String text, boolean exact) {
    try (PreparedStatement statement = createSelectProfile(text, exact)) {
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return parseProfile(resultSet);
      }
    } catch (SQLException e) {
      logger.log(Level.WARNING, "Encountered an exception retrieving profile", e);
    }
    return null;
  }

  @Contract("_, _ -> new")
  protected abstract @NotNull PreparedStatement createSelectProfile(
      @NotNull String name,
      boolean exact
  ) throws SQLException;

  protected abstract @Nullable Profile parseProfile(@NotNull ResultSet results) throws SQLException;

}
