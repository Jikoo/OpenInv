package com.lishid.openinv.internal.v1_21_R1.container.menu;

import com.lishid.openinv.internal.v1_21_R1.container.OpenEnderChest;
import com.lishid.openinv.util.Permissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class OpenEnderChestMenu extends OpenChestMenu<OpenEnderChest> {

  public OpenEnderChestMenu(OpenEnderChest enderChest, ServerPlayer viewer, int containerId) {
    super(getChestMenuType(enderChest.getContainerSize()), containerId, enderChest, viewer);
  }

  @Override
  protected boolean checkViewOnly() {
    return !(ownContainer ? Permissions.ENDERCHEST_EDIT_SELF : Permissions.ENDERCHEST_OPEN_OTHER)
        .hasPermission(viewer.getBukkitEntity());
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
