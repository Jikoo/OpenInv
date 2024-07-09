package com.lishid.openinv.internal;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Accessor {

  @NotNull
  PlayerManager getPlayerManager();

  @NotNull IAnySilentContainer getAnySilentContainer();

  @NotNull ISpecialPlayerInventory createPlayerInventory(@NotNull Player player);

  @NotNull ISpecialEnderChest createEnderChest(@NotNull Player player);

  <T extends ISpecialInventory> @Nullable T get(@NotNull Inventory bukkitInventory, @NotNull Class<T> clazz);

  void reload(@NotNull ConfigurationSection config);

}
