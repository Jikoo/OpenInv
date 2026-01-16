package com.lishid.openinv.util.profile;

import me.nahu.scheduler.wrapper.runnable.WrappedRunnable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public abstract class OfflinePlayerImporter extends WrappedRunnable {

  private final @NotNull BatchProfileStore profileStore;
  private final int batchSize;

  public OfflinePlayerImporter(@NotNull BatchProfileStore profileStore, int batchSize) {
    this.profileStore = profileStore;
    this.batchSize = batchSize;
  }

  @Override
  public void run() {
    Set<Profile> batch = new HashSet<>();
    for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
      String name = offline.getName();

      if (name == null) {
        continue;
      }

      batch.add(new Profile(name, offline.getUniqueId()));

      if (batch.size() >= batchSize) {
        profileStore.pushBatch(batch);
        batch.clear();
      }
    }

    if (!batch.isEmpty()) {
      profileStore.pushBatch(batch);
    }

    onComplete();
  }

  public abstract void onComplete();

}
