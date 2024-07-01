package com.lishid.openinv.internal;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.util.lang.Replacement;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public enum InventoryViewTitle {

  PLAYER_INVENTORY("container.player", "'s Inventory"),
  ENDER_CHEST("container.enderchest", "'s Ender Chest");

  private final String localizationKey;
  private final String defaultSuffix;

  InventoryViewTitle(String localizationKey, String defaultSuffix) {
    this.localizationKey = localizationKey;
    this.defaultSuffix = defaultSuffix;
  }

  public @NotNull String getTitle(@NotNull Player viewer, @NotNull ISpecialInventory inventory) {
    HumanEntity owner = inventory.getPlayer();

    String localTitle = OpenInv.getPlugin(OpenInv.class)
        .getLocalizedMessage(
            viewer,
            localizationKey,
            new Replacement("%player%", owner.getName()));
    return Objects.requireNonNullElseGet(localTitle, () -> owner.getName() + defaultSuffix);
  }

  public static @Nullable InventoryViewTitle of(@NotNull ISpecialInventory inventory) {
    if (inventory instanceof ISpecialPlayerInventory) {
      return PLAYER_INVENTORY;
    } else if (inventory instanceof ISpecialEnderChest) {
      return ENDER_CHEST;
    } else {
      return null;
    }
  }

}
