package com.lishid.openinv.event;

import com.lishid.openinv.internal.ISpecialInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Construct and call events.
 */
public final class OpenEvents {

  public static boolean saveCancelled(@NotNull Player player) {
    return call(new PlayerSaveEvent(player));
  }

  public static boolean saveCancelled(@NotNull Player player, @NotNull ISpecialInventory inventory) {
    return call(new OpenPlayerSaveEvent(player, inventory));
  }

  private static <T extends Event & Cancellable> boolean call(T event) {
    Bukkit.getPluginManager().callEvent(event);
    return event.isCancelled();
  }

  private OpenEvents() {
    throw new IllegalStateException("Cannot create instance of utility class.");
  }

}
