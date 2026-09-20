package com.lishid.openinv.internal.paper26_1;

import com.lishid.openinv.internal.ISpecialEnderChest;
import com.github.jikoo.openinv.internal.container.slot.InventoryFactory;
import com.lishid.openinv.internal.paper26_1.container.AnySilentContainer;
import com.lishid.openinv.internal.paper26_1.container.OpenEnderChest;
import com.lishid.openinv.internal.paper26_1.player.PlayerManager;
import com.lishid.openinv.internal.paper26_3.container.slot.placeholder.PlaceholderLoader;
import com.github.jikoo.openinv.lang.LanguageManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

@NullMarked
public class InternalAccessor extends com.lishid.openinv.internal.paper26_2.InternalAccessor {

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
  protected AnySilentContainer createAnySilentContainer(
      Logger logger,
      LanguageManager lang
  ) {
    return new AnySilentContainer(logger, lang);
  }

  @Override
  protected PlaceholderLoader createPlaceholderLoader() {
    return new com.lishid.openinv.internal.paper26_1.container.slot.placeholder.PlaceholderLoader();
  }

  @Override
  public ISpecialEnderChest createEnderChest(Player player) {
    return new OpenEnderChest(player);
  }

}
