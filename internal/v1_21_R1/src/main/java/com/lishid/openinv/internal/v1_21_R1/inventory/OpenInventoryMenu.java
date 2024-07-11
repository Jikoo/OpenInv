package com.lishid.openinv.internal.v1_21_R1.inventory;

import com.lishid.openinv.internal.v1_21_R1.inventory.slot.ContentDrop;
import com.lishid.openinv.internal.v1_21_R1.inventory.slot.ContentEquipment;
import com.lishid.openinv.internal.v1_21_R1.inventory.slot.SlotViewOnly;
import com.lishid.openinv.util.Permissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class OpenInventoryMenu extends OpenContainerMenu {

  private final OpenInventory inventory;
  private final ServerPlayer viewer;
  private final int topSize;
  private final int offset;
  private CraftInventoryView bukkitEntity;

  protected OpenInventoryMenu(OpenInventory inventory, ServerPlayer viewer, int i) {
    super(getMenuType(inventory, viewer), i);
    this.inventory = inventory;
    this.viewer = viewer;

    int upperRows;
    boolean ownInv = inventory.getOwnerHandle().equals(viewer);
    if (ownInv) {
      // Disallow duplicate access to own main inventory contents.
      offset = viewer.getInventory().items.size();
      upperRows = ((int) Math.ceil((inventory.getContainerSize() - offset) / 9.0));
    } else {
      offset = 0;
      upperRows = inventory.getContainerSize() / 9;
    }

    boolean viewOnly = !(ownInv ? Permissions.INVENTORY_EDIT_SELF : Permissions.INVENTORY_EDIT_OTHER)
        .hasPermission(viewer.getBukkitEntity());

    // View's upper inventory - our container
    for (int row = 0; row < upperRows; ++row) {
      for (int col = 0; col < 9; ++col) {
        // x and y for client purposes, but hey, we're thorough here.
        // Adapted from net.minecraft.world.inventory.ChestMenu
        int x = 8 + col * 18;
        int y = 18 + row * 18;
        int index = offset + row * 9 + col;

        // Guard against weird inventory sizes.
        if (index >= inventory.getContainerSize()) {
          addSlot(new SlotViewOnly(inventory, index, x, y));
          continue;
        }

        Slot slot = getUpperSlot(index, x, y, ownInv, viewOnly);

        addSlot(slot);
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

    this.topSize = slots.size() - 36;
  }

  private Slot getUpperSlot(int index, int x, int y, boolean ownInv, boolean viewOnly) {
    Slot slot = inventory.getMenuSlot(index, x, y);

    // If the slot is cannot be interacted with there's nothing to configure.
    if (slot.getClass().equals(SlotViewOnly.class)) {
      return slot;
    }

    // Remove drop slot if viewer is not allowed to use it.
    if (slot instanceof ContentDrop.SlotDrop
        && (viewOnly || !Permissions.INVENTORY_SLOT_DROP.hasPermission(viewer.getBukkitEntity()))) {
      return new SlotViewOnly(inventory, index, x, y);
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
        equipment.onlyEquipmentFor(inventory.getOwnerHandle());
      }

      // Equipment slots are a core part of the inventory, so they will always be shown.
      return slot;
    }

    // When viewing own inventory, only allow access to equipment and drop slots (equipment allowed above).
    if (ownInv && !(slot instanceof ContentDrop.SlotDrop)) {
      return new SlotViewOnly(inventory, index, x, y);
    }

    if (viewOnly) {
      return SlotViewOnly.wrap(slot);
    }

    return slot;
  }

  private static MenuType<?> getMenuType(OpenInventory inventory, ServerPlayer viewer) {
    int size = inventory.getContainerSize();
    if (inventory.getOwnerHandle().equals(viewer)) {
      size -= viewer.getInventory().items.size();
      size = ((int) Math.ceil(size / 9.0)) * 9;
    }

    return OpenContainerMenu.getContainers(size);
  }

  @Override
  public CraftInventoryView getBukkitView() {
    if (bukkitEntity == null) {
      bukkitEntity = new CraftInventoryView(viewer.getBukkitEntity(), inventory.getBukkitInventory(), this) {
        @Override
        public org.bukkit.inventory.ItemStack getItem(int index) {
          if (index < 0) {
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
        public @NotNull InventoryType.SlotType getSlotType(int slot) {
          if (slot < 0) {
            return InventoryType.SlotType.OUTSIDE;
          }
          if (slot >= topSize) {
            return super.getSlotType(offset + slot);
          }
          return inventory.getSlotType(offset + slot);
        }
      };
    }
    return bukkitEntity;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
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
          for (int extra = inventory.getOwnerHandle().getInventory().items.size() - offset; extra < topSize; ++extra) {
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
        if (!movedGear && !this.moveItemStackTo(itemStack, 0, inventory.getOwnerHandle().getInventory().items.size(), true)) {
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

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public boolean canDragTo(Slot slot) {
    return !(slot instanceof ContentDrop.SlotDrop || slot instanceof SlotViewOnly);
  }

}
