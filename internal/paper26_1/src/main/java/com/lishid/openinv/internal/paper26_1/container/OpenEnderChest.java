package com.lishid.openinv.internal.paper26_1.container;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenEnderChest extends com.lishid.openinv.internal.paper26_3.container.OpenEnderChest {

  public OpenEnderChest(org.bukkit.entity.Player player) {
    super(player);
  }

  @Override
  protected NonNullList<ItemStack> getEnderChestItems(ServerPlayer owner) {
    return owner.getEnderChestInventory().items;
  }

}
