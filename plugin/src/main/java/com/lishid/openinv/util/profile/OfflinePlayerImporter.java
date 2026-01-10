package com.lishid.openinv.util.profile;

import me.nahu.scheduler.wrapper.runnable.WrappedRunnable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public abstract class OfflinePlayerImporter extends WrappedRunnable {

  private final @NotNull BatchProfileStore profileStore;

  public OfflinePlayerImporter(@NotNull BatchProfileStore profileStore) {
    this.profileStore = profileStore;
  }

  @Override
  public void run() {
    for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
      String name = offline.getName();
      if (name != null) {
        profileStore.addProfile(new Profile(name, offline.getUniqueId()));
      }
    }
    onComplete();
  }

  public abstract void onComplete();

}
