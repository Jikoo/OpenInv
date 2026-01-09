package com.lishid.openinv.util.profile;

import com.lishid.openinv.util.StringMetric;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class OfflinePlayerProfileStore implements ProfileStore {

  private final @NotNull Logger logger;

  public OfflinePlayerProfileStore(@NotNull Logger logger) {
    this.logger = logger;
  }

  @Override
  public void addProfile(@NotNull Profile profile) {
    // No-op. Server handles profile creation and storage.
  }

  @Override
  public void shutdown() {
    // No-op. Nothing to push.
  }

  @Override
  public @Nullable Profile getProfileExact(@NotNull String name) {
    ProfileStore.warnMainThread(logger);
    @SuppressWarnings("deprecation")
    OfflinePlayer offline = Bukkit.getOfflinePlayer(name);

    if (!offline.hasPlayedBefore()) {
      return null;
    }

    String realName = offline.getName();
    if (realName == null) {
      realName = name;
    }

    return new Profile(realName, offline.getUniqueId());
  }

  @Override
  public @Nullable Profile getProfileInexact(@NotNull String search) {
    ProfileStore.warnMainThread(logger);

    float bestMatch = 0.0F;
    Profile bestProfile = null;
    for (OfflinePlayer player : Bukkit.getOnlinePlayers()) {
      String name = player.getName();
      if (name == null) {
        // Discount UUID-only profiles; Direct UUID lookup should already be done.
        return null;
      }

      float currentMatch = StringMetric.compareJaroWinkler(name, name);

      if (currentMatch > bestMatch) {
        bestMatch = currentMatch;
        bestProfile = new Profile(name, player.getUniqueId());
      }

      // A score of 1 is an exact match. Don't bother checking the rest.
      if (currentMatch == 1.0F) {
        break;
      }
    }

    return bestProfile;
  }

}
