package com.lishid.openinv.internal.v1_21_R1.inventory;

import com.lishid.openinv.internal.v1_21_R1.inventory.slot.SlotViewOnly;
import com.lishid.openinv.util.Permissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventoryView;

class OpenEnderChestMenu extends OpenContainerMenu {

  private final OpenEnderChest enderChest;
  private final boolean viewOnly;
  private final ServerPlayer viewer;
  private final int topSize;
  private CraftInventoryView view;

  OpenEnderChestMenu(OpenEnderChest enderChest, ServerPlayer viewer, int containerId) {
    super(getMenuType(enderChest), containerId);
    this.enderChest = enderChest;
    this.viewer = viewer;
    var bukkitViewer = viewer.getBukkitEntity();
    viewOnly = !(bukkitViewer.equals(enderChest.getPlayer())
        ? Permissions.ENDERCHEST_EDIT_SELF
        : Permissions.ENDERCHEST_OPEN_OTHER)
        .hasPermission(bukkitViewer);
    int upperRows = (int) Math.ceil(enderChest.getContainerSize() / 9.0);
    topSize = upperRows * 9;

    // View's upper inventory - our container
    for (int row = 0; row < upperRows; ++row) {
      for (int col = 0; col < 9; ++col) {
        // x and y for client purposes, but hey, we're thorough here.
        // Adapted from net.minecraft.world.inventory.ChestMenu
        int x = 8 + col * 18;
        int y = 18 + row * 18;
        int index = row * 9 + col;

        // Guard against weird inventory sizes.
        if (index >= enderChest.getContainerSize()) {
          addSlot(new SlotViewOnly(enderChest, index, x, y));
          continue;
        }

        addSlot(new Slot(enderChest, index, x, y));
      }
    }

    // View's lower inventory - viewer inventory
    int playerInvPad = (upperRows - 4) * 18;
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 9; ++col) {
        int x = 8 + col * 18;
        int y = playerInvPad + row * 18 + 103;
        addSlot(new Slot(viewer.getInventory(), row * 9 + col + 9, x, y));
      }
    }
    // Hotbar
    for (int col = 0; col < 9; ++col) {
      int x = 8 + col * 18;
      int y = playerInvPad + 161;
      addSlot(new Slot(viewer.getInventory(), col, x, y));
    }
  }

  private static MenuType<?> getMenuType(OpenEnderChest enderChest) {
    return OpenContainerMenu.getContainers(((int) Math.ceil(enderChest.getContainerSize() / 9.0)) * 9);
  }

  @Override
  public CraftInventoryView getBukkitView() {
    if (view == null) {
      view = new CraftInventoryView(viewer.getBukkitEntity(), enderChest.getBukkitInventory(), this);
    }
    return view;
  }

  @Override
  protected Slot addSlot(Slot slot) {
    slot = super.addSlot(slot);

    // If view-only and slot is in upper inventory, wrap it.
    if (viewOnly && slot.index < enderChest.getContainerSize()) {
      slot = SlotViewOnly.wrap(slot);
      slots.set(slot.index, slot);
    }

    return slot;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
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

  @Override
  public boolean stillValid(Player entityhuman) {
    return true;
  }

}
