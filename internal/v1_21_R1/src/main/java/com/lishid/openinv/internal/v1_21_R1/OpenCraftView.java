package com.lishid.openinv.internal.v1_21_R1;

import com.lishid.openinv.internal.ISpecialInventory;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftAbstractInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

class OpenCraftView extends CraftAbstractInventoryView {

  private final @NotNull Player player;
  private final @NotNull ISpecialInventory inventory;
  private final @NotNull String originalTitle;
  private String title = null;

  OpenCraftView(@NotNull Player player, @NotNull ISpecialInventory inventory, @NotNull String originalTitle) {
    this.player = player;
    this.inventory = inventory;
    this.originalTitle = originalTitle;
  }

  @Override
  public @NotNull Inventory getTopInventory() {
    return inventory.getBukkitInventory();
  }

  @Override
  public @NotNull Inventory getBottomInventory() {
    return player.getInventory();
  }

  @Override
  public @NotNull HumanEntity getPlayer() {
    return player;
  }

  @Override
  public @NotNull InventoryType getType() {
    return inventory.getBukkitInventory().getType();
  }

  @Override
  public @NotNull String getTitle() {
    return title == null ? originalTitle : title;
  }

  @Override
  public @NotNull String getOriginalTitle() {
    return originalTitle;
  }

  @Override
  public void setTitle(@NotNull String title) {
    this.title = title;
  }

}
