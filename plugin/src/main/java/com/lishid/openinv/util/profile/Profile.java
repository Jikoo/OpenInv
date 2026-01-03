package com.lishid.openinv.util.profile;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record Profile(@NotNull String name, @NotNull UUID id) {

  public Profile(@NotNull Player player) {
    this(player.getName(), player.getUniqueId());
  }

  @Override
  public int hashCode() {
    // As names are the unique key, profiles with the same name should collide.
    return name.hashCode();
  }

}
