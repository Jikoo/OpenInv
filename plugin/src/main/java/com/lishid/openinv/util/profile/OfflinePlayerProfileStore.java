package com.lishid.openinv.util.profile;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
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
  public @NotNull @Unmodifiable Collection<Profile> getProfiles(@NotNull String search) {
    ProfileStore.warnMainThread(logger);
    return Arrays.stream(Bukkit.getOfflinePlayers())
        .map(player -> {
          String name = player.getName();
          if (name == null) {
            // Discount UUID-only profiles; Direct UUID lookup should already be done.
            return null;
          }
          return new Profile(player.getName(), player.getUniqueId());
        })
        .filter(Objects::nonNull)
        .toList();
  }

}
