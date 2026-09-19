package com.github.jikoo.openinv.internal.spigot26_3.container.menu;

import com.github.jikoo.openinv.internal.spigot26_3.container.OpenEnderChest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenEnderChestMenu extends OpenSyncMenu<OpenEnderChest> {

  public OpenEnderChestMenu(
      OpenEnderChest enderChest,
      ServerPlayer viewer,
      int containerId,
      boolean viewOnly
  ) {
    super(getChestMenuType(enderChest.getContainerSize()), containerId, enderChest, viewer, viewOnly);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (viewOnly) {
      return ItemStack.EMPTY;
    }

    // See ChestMenu
    Slot slot = this.slots.get(index);

    if (slot.isFake() || !slot.hasItem()) {
      return ItemStack.EMPTY;
    }

    ItemStack itemStack = slot.getItem();
    ItemStack original = itemStack.copy();

    if (index < topSize) {
      if (!this.moveItemStackTo(itemStack, topSize, this.slots.size(), true)) {
        return ItemStack.EMPTY;
      }
    } else if (!this.moveItemStackTo(itemStack, 0, topSize, false)) {
      return ItemStack.EMPTY;
    }

    if (itemStack.isEmpty()) {
      slot.setByPlayer(ItemStack.EMPTY);
    } else {
      slot.setChanged();
    }

    return original;
  }

}
