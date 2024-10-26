package com.lishid.openinv.internal.v1_21_R2;

import com.lishid.openinv.internal.Accessor;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.v1_21_R2.container.AnySilentContainer;
import com.lishid.openinv.internal.v1_21_R2.container.OpenEnderChest;
import com.lishid.openinv.internal.v1_21_R2.container.OpenInventory;
import com.lishid.openinv.internal.v1_21_R2.container.Placeholders;
import com.lishid.openinv.internal.v1_21_R2.player.PlayerManager;
import com.lishid.openinv.util.lang.LanguageManager;
import net.minecraft.world.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InternalAccessor implements Accessor {

  private final @NotNull Logger logger;
  private final @NotNull PlayerManager manager;
  private final @NotNull AnySilentContainer anySilentContainer;

  public InternalAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
    this.logger = logger;
    manager = new PlayerManager(logger);
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
    return new OpenInventory(player);
  }

  @Override
  public @NotNull ISpecialEnderChest createEnderChest(@NotNull Player player) {
    return new OpenEnderChest(player);
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
  public void reload(@NotNull ConfigurationSection config) {
    ConfigurationSection placeholders = config.getConfigurationSection("placeholders");
    if (placeholders != null) {
      try {
        Placeholders.load(placeholders);
      } catch (Exception e) {
        logger.log(Level.WARNING, "Caught exception loading placeholder overrides!", e);
      }
    }
  }

}
