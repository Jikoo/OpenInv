package com.github.jikoo.openinv.internal.spigot26_1;

import com.github.jikoo.openinv.internal.container.slot.InventoryFactory;
import com.github.jikoo.openinv.internal.spigot26_1.container.slot.placeholder.PlaceholderLoader;
import com.github.jikoo.openinv.internal.spigot26_1.player.PlayerManager;
import com.github.jikoo.openinv.lang.LanguageManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Level;
import java.util.logging.Logger;

@NullMarked
public class InternalAccessor extends com.github.jikoo.openinv.internal.spigot26_2.InternalAccessor {

  public InternalAccessor(Logger logger, LanguageManager lang) {
    super(logger, lang);
  }

  @Override
  protected PlayerManager createPlayerManager(
      Logger logger,
      InventoryFactory<ServerPlayer, ItemStack, Container, Slot, EquipmentSlot> factory
  ) {
    return new PlayerManager(logger, factory);
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
