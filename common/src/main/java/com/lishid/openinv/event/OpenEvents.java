package com.lishid.openinv.event;

import com.lishid.openinv.internal.ISpecialInventory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class OpenEvents {

  public static OpenPlayerSaveEvent save(@NotNull Player player, @NotNull ISpecialInventory inventory) {
    return new OpenPlayerSaveEvent(player, inventory);
  }

  private OpenEvents() {
    throw new IllegalStateException("Cannot create instance of utility class.");
  }

}
