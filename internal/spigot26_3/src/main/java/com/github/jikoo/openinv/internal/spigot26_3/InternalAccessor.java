package com.github.jikoo.openinv.internal.spigot26_3;

import com.github.jikoo.openinv.internal.spigot26_3.container.AnySilentContainer;
import com.github.jikoo.openinv.internal.spigot26_3.container.OpenInventory;
import com.github.jikoo.openinv.internal.spigot26_3.container.OpenEnderChest;
import com.github.jikoo.openinv.internal.spigot26_3.container.slot.placeholder.PlaceholderLoader;
import com.github.jikoo.openinv.internal.spigot26_3.player.PlayerManager;
import com.lishid.openinv.internal.Accessor;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.lang.LanguageManager;
import net.minecraft.world.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Level;
import java.util.logging.Logger;

@NullMarked
public class InternalAccessor implements Accessor {

  protected final Logger logger;
  private final PlayerManager manager;
  private final AnySilentContainer anySilentContainer;

  public InternalAccessor(Logger logger, LanguageManager lang) {
    this.logger = logger;
    manager = new PlayerManager(logger);
    anySilentContainer = new AnySilentContainer(logger, lang);
  }

  @Override
  public PlayerManager getPlayerManager() {
    return manager;
  }

  @Override
  public IAnySilentContainer getAnySilentContainer() {
    return anySilentContainer;
  }

  @Override
  public ISpecialPlayerInventory createPlayerInventory(Player player) {
    return new OpenInventory(player);
  }

  @Override
  public ISpecialEnderChest createEnderChest(Player player) {
    return new OpenEnderChest(player);
  }

  @Override
  public <T extends ISpecialInventory> @Nullable T get(Inventory bukkitInventory, Class<T> clazz) {
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
  public void reload(ConfigurationSection config) {
    ConfigurationSection placeholders = config.getConfigurationSection("placeholders");
    try {
      // Reset placeholders to defaults and try to load configuration.
      new PlaceholderLoader().load(placeholders);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Caught exception loading placeholder overrides!", e);
    }
  }

}
