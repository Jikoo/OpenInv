package com.github.jikoo.openinv.internal.spigot26_2;

import com.github.jikoo.openinv.internal.container.slot.InventoryFactory;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.OpenInventoryFactory;
import com.github.jikoo.openinv.lang.LanguageManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

@NullMarked
public class InternalAccessor extends com.github.jikoo.openinv.internal.spigot26_3.InternalAccessor {

  public InternalAccessor(Logger logger, LanguageManager lang) {
    super(logger, lang);
  }

  @Override
  protected InventoryFactory<ServerPlayer, ItemStack, Container, Slot, EquipmentSlot> createInventoryFactory() {
    return new OpenInventoryFactory();
  }

}
