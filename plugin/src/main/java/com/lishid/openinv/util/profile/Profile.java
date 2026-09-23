package com.lishid.openinv.util.profile;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public record Profile(String name, UUID id) {

  public Profile(Player player) {
    this(player.getName(), player.getUniqueId());
  }

  @Override
  public int hashCode() {
    // As names are the unique key, profiles with the same name should collide.
    return name.hashCode();
  }

}
