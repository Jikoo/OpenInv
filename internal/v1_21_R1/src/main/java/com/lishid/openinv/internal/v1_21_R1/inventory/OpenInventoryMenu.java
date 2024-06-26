package com.lishid.openinv.internal.v1_21_R1.inventory;

import com.lishid.openinv.internal.InventoryViewTitle;
import com.lishid.openinv.internal.v1_21_R1.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventoryView;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class OpenInventoryMenu extends AbstractContainerMenu {

  private final OpenInventory inventory;
  private final ServerPlayer viewer;
  private CraftInventoryView bukkitEntity;

  protected OpenInventoryMenu(OpenInventory inventory, ServerPlayer viewer, int i) {
    super(PlayerDataManager.getContainers(inventory.getContainerSize()), i);
    this.inventory = inventory;
    this.viewer = viewer;

    int upperRows = inventory.getContainerSize() / 9;

    // View's upper inventory - our container
    for (int row = 0; row < upperRows; ++row) {
      for (int col = 0; col < 9; ++col) {
        // x and y for client purposes, but hey, we're thorough here.
        // Adapted from net.minecraft.world.inventory.ChestMenu
        int x = 8 + col * 18;
        int y = 18 + row * 18;
        addSlot(inventory.getMenuSlot(row * 9 + col, x, y));
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

  @Override
  public CraftInventoryView getBukkitView() {
    if (bukkitEntity == null) {
      bukkitEntity = new CraftInventoryView(viewer.getBukkitEntity(), new OpenPlayerInventory(inventory), this) {
        private String title;

        @Override
        public @NotNull String getOriginalTitle() {
          return InventoryViewTitle.PLAYER_INVENTORY.getTitle(viewer.getBukkitEntity(), inventory);
        }

        @Override
        public @NotNull String getTitle() {
          if (title == null) {
            title = getOriginalTitle();
          }
          return title;
        }

        @Override
        public void setTitle(String title) {
          CraftInventoryView.sendInventoryTitleChange(this, title);
          this.title = title;
        }

        @NotNull
        @Override
        public InventoryType.SlotType getSlotType(int slot) {
          if (slot < 0 || slot >= inventory.getContainerSize()) {
            return super.getSlotType(slot);
          }
          return inventory.getSlotType(slot);
        }
      };
    }
    return bukkitEntity;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    // TODO remove item, then move, then re-add if necessary. Can remove some extra inv handling from main plugin.
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);
    if (slot.hasItem()) {
      ItemStack itemstack1 = slot.getItem();
      itemstack = itemstack1.copy();
      if (index < inventory.getContainerSize()) {
        // TODO maybe should override moveItemStackTo - exclude all fake items all the time
        if (!this.moveItemStackTo(itemstack1,  inventory.getContainerSize(), this.slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.moveItemStackTo(itemstack1, 0, inventory.getContainerSize(), false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.isEmpty()) {
        slot.setByPlayer(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
    }

    return itemstack;
  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

}
