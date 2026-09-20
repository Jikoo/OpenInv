package com.github.jikoo.openinv.internal;

import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Accessor {

  PlayerManager getPlayerManager();

  IAnySilentContainer getAnySilentContainer();

  ISpecialPlayerInventory createPlayerInventory(Player player);

  ISpecialEnderChest createEnderChest(Player player);

  <T extends ISpecialInventory> @Nullable T get(Inventory bukkitInventory, Class<T> clazz);

  void reload(ConfigurationSection config);

}
