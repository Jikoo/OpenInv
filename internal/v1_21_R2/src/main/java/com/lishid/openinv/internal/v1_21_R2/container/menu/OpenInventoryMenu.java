package com.lishid.openinv.internal.v1_21_R2.container.menu;

import com.google.common.base.Preconditions;
import com.lishid.openinv.internal.v1_21_R2.container.OpenInventory;
import com.lishid.openinv.internal.v1_21_R2.container.bukkit.OpenDummyPlayerInventory;
import com.lishid.openinv.internal.v1_21_R2.container.bukkit.OpenPlayerInventorySelf;
import com.lishid.openinv.internal.v1_21_R2.container.slot.ContentDrop;
import com.lishid.openinv.internal.v1_21_R2.container.slot.ContentEquipment;
import com.lishid.openinv.internal.v1_21_R2.container.slot.SlotViewOnly;
import com.lishid.openinv.util.Permissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenInventoryMenu extends OpenChestMenu<OpenInventory> {

  private int offset;

  public OpenInventoryMenu(OpenInventory inventory, ServerPlayer viewer, int i, boolean viewOnly) {
    super(getMenuType(inventory, viewer), i, inventory, viewer, viewOnly);
  }

  private static MenuType<ChestMenu> getMenuType(OpenInventory inventory, ServerPlayer viewer) {
    int size = inventory.getContainerSize();
    // Disallow duplicate access to own main inventory contents.
    if (inventory.getOwnerHandle().equals(viewer)) {
      size -= viewer.getInventory().items.size();
      size = ((int) Math.ceil(size / 9.0)) * 9;
    }

    return getChestMenuType(size);
  }

  @Override
  protected void preSlotSetup() {
    offset = ownContainer ? viewer.getInventory().items.size() : 0;
  }

  @Override
  protected @NotNull Slot getUpperSlot(int index, int x, int y) {
    index += offset;
    Slot slot = container.getMenuSlot(index, x, y);

    // If the slot cannot be interacted with there's nothing to configure.
    if (slot.getClass().equals(SlotViewOnly.class)) {
      return slot;
    }

    // Remove drop slot if viewer is not allowed to use it.
    if (slot instanceof ContentDrop.SlotDrop
        && (viewOnly || !Permissions.INVENTORY_SLOT_DROP.hasPermission(viewer.getBukkitEntity()))) {
      return new SlotViewOnly(container, index, x, y);
    }

    if (slot instanceof ContentEquipment.SlotEquipment equipment) {
      if (viewOnly) {
        return SlotViewOnly.wrap(slot);
      }

      Permissions perm = switch (equipment.getEquipmentSlot()) {
        case HEAD -> Permissions.INVENTORY_SLOT_HEAD_ANY;
        case CHEST -> Permissions.INVENTORY_SLOT_CHEST_ANY;
        case LEGS -> Permissions.INVENTORY_SLOT_LEGS_ANY;
        case FEET -> Permissions.INVENTORY_SLOT_FEET_ANY;
        // Off-hand can hold anything, not just equipment.
        default -> null;
      };

      // If the viewer doesn't have permission, only allow equipment the viewee can equip in the slot.
      if (perm != null && !perm.hasPermission(viewer.getBukkitEntity())) {
        equipment.onlyEquipmentFor(container.getOwnerHandle());
      }

      // Equipment slots are a core part of the inventory, so they will always be shown.
      return slot;
    }

    // When viewing own inventory, only allow access to equipment and drop slots (equipment allowed above).
    if (ownContainer && !(slot instanceof ContentDrop.SlotDrop)) {
      return new SlotViewOnly(container, index, x, y);
    }

    if (viewOnly) {
      return SlotViewOnly.wrap(slot);
    }

    return slot;
  }

  @Override
  protected @NotNull CraftInventoryView<OpenChestMenu<OpenInventory>, Inventory> createBukkitEntity() {
    org.bukkit.inventory.Inventory bukkitInventory;
    if (viewOnly) {
      bukkitInventory = new OpenDummyPlayerInventory(container);
    } else if (ownContainer) {
      bukkitInventory = new OpenPlayerInventorySelf(container, offset);
    } else {
      bukkitInventory = container.getBukkitInventory();
    }

    return new CraftInventoryView<>(viewer.getBukkitEntity(), bukkitInventory, this) {
      @Override
      public org.bukkit.inventory.ItemStack getItem(int index) {
        if (viewOnly || index < 0) {
          return null;
        }

        Slot slot = slots.get(index);
        return CraftItemStack.asCraftMirror(slot.hasItem() ? slot.getItem() : ItemStack.EMPTY);
      }

      @Override
      public boolean isInTop(int rawSlot) {
        return rawSlot < topSize;
      }

      @Override
      public @Nullable Inventory getInventory(int rawSlot) {
        if (viewOnly) {
          return null;
        }
        if (rawSlot == InventoryView.OUTSIDE || rawSlot == -1) {
          return null;
        }
        Preconditions.checkArgument(rawSlot >= 0 && rawSlot < topSize + offset + BOTTOM_INVENTORY_SIZE,
            "Slot %s outside of inventory", rawSlot);
        if (rawSlot > topSize) {
          return getBottomInventory();
        }
        Slot slot = slots.get(rawSlot);
        if (slot.isFake()) {
          return null;
        }
        return getTopInventory();
      }

      @Override
      public int convertSlot(int rawSlot) {
        if (viewOnly) {
          return InventoryView.OUTSIDE;
        }
        if (rawSlot < 0) {
          return rawSlot;
        }
        if (rawSlot < topSize) {
          Slot slot = slots.get(rawSlot);
          if (slot.isFake()) {
            return InventoryView.OUTSIDE;
          }
          return rawSlot;
        }

        int slot = rawSlot - topSize;

        if (slot >= 27) {
          slot -= 27;
        } else {
          slot += 9;
        }

        return slot;
      }

      @Override
      public @NotNull InventoryType.SlotType getSlotType(int slot) {
        if (viewOnly || slot < 0) {
          return InventoryType.SlotType.OUTSIDE;
        }
        if (slot >= topSize) {
          slot -= topSize;
          if (slot >= 27) {
            return InventoryType.SlotType.QUICKBAR;
          }
          return InventoryType.SlotType.CONTAINER;
        }
        return OpenInventoryMenu.this.container.getSlotType(offset + slot);
      }

      @Override
      public int countSlots() {
        return topSize + BOTTOM_INVENTORY_SIZE;
      }
    };
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (viewOnly) {
      return ItemStack.EMPTY;
    }

    // See ChestMenu and InventoryMenu
    Slot slot = this.slots.get(index);

    if (!slot.hasItem() || slot.isFake()) {
      return ItemStack.EMPTY;
    }

    ItemStack itemStack = slot.getItem();
    ItemStack originalStack = itemStack.copy();

    if (index < topSize) {
      // If we're moving top to bottom, do a normal transfer.
      if (!this.moveItemStackTo(itemStack, topSize, this.slots.size(), true)) {
        return ItemStack.EMPTY;
      }
    } else {
      EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(itemStack);
      boolean movedGear = switch (equipmentSlot) {
        // If this is gear, try to move it to the correct slot first.
        case OFFHAND, FEET, LEGS, CHEST, HEAD -> {
          // Locate the correct slot in the contents following the main inventory.
          for (int extra = container.getOwnerHandle().getInventory().items.size() - offset; extra < topSize; ++extra) {
            Slot extraSlot = getSlot(extra);
            if (extraSlot instanceof ContentEquipment.SlotEquipment equipSlot
                && equipSlot.getEquipmentSlot() == equipmentSlot) {
              // If we've found a matching slot, try to move to it.
              // If this succeeds, even partially, we will not attempt to move to other slots.
              // Otherwise, armor is already occupied, so we'll fall through to main inventory.
              yield this.moveItemStackTo(itemStack, extra, extra + 1, false);
            }
          }
          yield false;
        }
        // Non-gear gets no special treatment.
        default -> false;
      };

      // If main inventory is not available, there's nowhere else to move.
      if (offset != 0) {
        if (!movedGear) {
          return ItemStack.EMPTY;
        }
      } else {
        // If we didn't move to a gear slot, try to move to a main inventory slot.
        if (!movedGear && !this.moveItemStackTo(itemStack, 0, container.getOwnerHandle().getInventory().items.size(), true)) {
          return ItemStack.EMPTY;
        }
      }
    }

    if (itemStack.isEmpty()) {
      slot.setByPlayer(ItemStack.EMPTY);
    } else {
      slot.setChanged();
    }

    return originalStack;
  }

}
