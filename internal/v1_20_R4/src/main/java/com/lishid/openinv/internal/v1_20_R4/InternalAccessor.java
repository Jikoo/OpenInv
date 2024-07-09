package com.lishid.openinv.internal.v1_20_R4;

import com.lishid.openinv.internal.Accessor;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.lang.LanguageManager;
import net.minecraft.world.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class InternalAccessor implements Accessor {

  private final @NotNull PlayerManager manager;
  private final @NotNull AnySilentContainer anySilentContainer;

  public InternalAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
    manager = new PlayerManager(logger, lang);
    anySilentContainer = new AnySilentContainer(logger, lang);
  }

  @Override
  public @NotNull PlayerManager getPlayerManager() {
    return manager;
  }

  @Override
  public @NotNull IAnySilentContainer getAnySilentContainer() {
    return anySilentContainer;
  }

  @Override
  public @NotNull ISpecialPlayerInventory createPlayerInventory(@NotNull Player player) {
    return new SpecialPlayerInventory(player, player.isOnline());
  }

  @Override
  public @NotNull ISpecialEnderChest createEnderChest(@NotNull Player player) {
    return new SpecialEnderChest(player, player.isOnline());
  }

  @Override
  public <T extends ISpecialInventory> @Nullable T get(@NotNull Inventory bukkitInventory, @NotNull Class<T> clazz) {
    if (!(bukkitInventory instanceof CraftInventory craftInventory)) {
      return null;
    }
    Container container = craftInventory.getInventory();
    if (clazz.isInstance(container)) {
      return clazz.cast(container);
    }
    return null;
  }

  @Override
  public void reload(@NotNull ConfigurationSection config) {}

}
