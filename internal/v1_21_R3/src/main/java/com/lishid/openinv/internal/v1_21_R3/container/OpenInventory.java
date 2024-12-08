package com.lishid.openinv.internal.v1_21_R3.container;

import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.InternalOwned;
import com.lishid.openinv.internal.v1_21_R3.container.bukkit.OpenPlayerInventory;
import com.lishid.openinv.internal.v1_21_R3.container.menu.OpenInventoryMenu;
import com.lishid.openinv.internal.v1_21_R3.container.slot.Content;
import com.lishid.openinv.internal.v1_21_R3.container.slot.ContentCrafting;
import com.lishid.openinv.internal.v1_21_R3.container.slot.ContentCraftingResult;
import com.lishid.openinv.internal.v1_21_R3.container.slot.ContentCursor;
import com.lishid.openinv.internal.v1_21_R3.container.slot.ContentDrop;
import com.lishid.openinv.internal.v1_21_R3.container.slot.ContentEquipment;
import com.lishid.openinv.internal.v1_21_R3.container.slot.ContentList;
import com.lishid.openinv.internal.v1_21_R3.container.slot.ContentOffHand;
import com.lishid.openinv.internal.v1_21_R3.container.slot.ContentViewOnly;
import com.lishid.openinv.internal.v1_21_R3.container.slot.SlotViewOnly;
import com.lishid.openinv.internal.v1_21_R3.player.PlayerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OpenInventory implements Container, InternalOwned<ServerPlayer>, ISpecialPlayerInventory {

  private final List<Content> slots;
  private final int size;
  private ServerPlayer owner;
  private int maxStackSize = 99;
  private CraftInventory bukkitEntity;
  public List<HumanEntity> transaction = new ArrayList<>();

  public OpenInventory(@NotNull org.bukkit.entity.Player bukkitPlayer) {
    owner = PlayerManager.getHandle(bukkitPlayer);

    // Get total size, rounding up to nearest 9 for client compatibility.
    int rawSize = owner.getInventory().getContainerSize() + owner.inventoryMenu.getCraftSlots().getContainerSize() + 1;
    size = ((int) Math.ceil(rawSize / 9.0)) * 9;

    slots = NonNullList.withSize(size, new ContentViewOnly(owner));
    setupSlots();
  }

  private void setupSlots() {
    // Top of inventory: Regular contents.
    int nextIndex = addMainInventory();

    // If inventory is expected size, we can arrange slots to be pretty.
    Inventory ownerInv = owner.getInventory();
    if (ownerInv.items.size() == 36
        && ownerInv.armor.size() == 4
        && ownerInv.offhand.size() == 1
        && owner.inventoryMenu.getCraftSlots().getContainerSize() == 4) {
      // Armor slots: Bottom left.
      addArmor(36);
      // Off-hand: Below chestplate.
      addOffHand(46);
      // Drop slot: Bottom right.
      slots.set(53, new ContentDrop(owner));
      // Cursor slot: Above drop.
      slots.set(44, new ContentCursor(owner));

      // Crafting is displayed in the bottom right corner.
      // As we're using the pretty view, this is a 3x2.
      addCrafting(41, true);
      return;
    }

    // Otherwise we'll just add elements linearly.
    nextIndex = addArmor(nextIndex);
    nextIndex = addOffHand(nextIndex);
    nextIndex = addCrafting(nextIndex, false);
    slots.set(nextIndex, new ContentCursor(owner));
    // Drop slot last.
    slots.set(slots.size() - 1, new ContentDrop(owner));
  }

  private int addMainInventory() {
    int listSize = owner.getInventory().items.size();
    // Hotbar slots are 0-8. We want those to appear on the bottom of the inventory like a normal player inventory,
    // so everything else needs to move up a row.
    int hotbarDiff = listSize - 9;
    for (int localIndex = 0; localIndex < listSize; ++localIndex) {
      InventoryType.SlotType type;
      int invIndex;
      if (localIndex < hotbarDiff) {
        invIndex = localIndex + 9;
        type = InventoryType.SlotType.CONTAINER;
      } else {
        type = InventoryType.SlotType.QUICKBAR;
        invIndex = localIndex - hotbarDiff;
      }

      slots.set(localIndex, new ContentList(owner, invIndex, type) {
        @Override
        public void setHolder(@NotNull ServerPlayer holder) {
          items = holder.getInventory().items;
        }
      });
    }
    return listSize;
  }

  private int addArmor(int startIndex) {
    int listSize = owner.getInventory().armor.size();

    for (int i = 0; i < listSize; ++i) {
      // Armor slots go bottom to top; boots are slot 0, helmet is slot 3.
      // Since we have to display horizontally due to space restrictions,
      // making the left side the "top" is more user-friendly.
      int armorIndex;
      EquipmentSlot slot;
      switch (i) {
        case 3 -> {
          armorIndex = 0;
          slot = EquipmentSlot.FEET;
        }
        case 2 -> {
          armorIndex = 1;
          slot = EquipmentSlot.LEGS;
        }
        case 1 -> {
          armorIndex = 2;
          slot = EquipmentSlot.CHEST;
        }
        case 0 -> {
          armorIndex = 3;
          slot = EquipmentSlot.HEAD;
        }
        default -> {
          // In the event that new armor slots are added, they can be placed at the end.
          armorIndex = i;
          slot = EquipmentSlot.MAINHAND;
        }
      }

      slots.set(startIndex + i, new ContentEquipment(owner, armorIndex, slot));
    }

    return startIndex + listSize;
  }

  private int addOffHand(int startIndex) {
    int listSize = owner.getInventory().offhand.size();
    for (int localIndex = 0; localIndex < listSize; ++localIndex) {
      slots.set(startIndex + localIndex, new ContentOffHand(owner, localIndex));
    }
    return startIndex + listSize;
  }

  private int addCrafting(int startIndex, boolean pretty) {
    int listSize = owner.inventoryMenu.getCraftSlots().getContents().size();
    pretty &= listSize == 4;

    for (int localIndex = 0; localIndex < listSize; ++localIndex) {
      // Pretty display is a 2x2 rather than linear.
      // If index is in top row, grid is not 2x2, or pretty is disabled, just use current index.
      // Otherwise, subtract 2 and add 9 to start in the same position on the next row.
      int modIndex = startIndex + (localIndex < 2 || !pretty ? localIndex : localIndex + 7);

      slots.set(modIndex, new ContentCrafting(owner, localIndex));
    }

    if (pretty) {
      slots.set(startIndex + 2, new ContentViewOnly(owner) {
        @Override
        public Slot asSlot(Container container, int slot, int x, int y) {
          return new SlotViewOnly(container, slot, x, y) {
            @Override
            public ItemStack getOrDefault() {
              return Placeholders.craftingOutput;
            }
          };
        }
      });
      slots.set(startIndex + 11, new ContentCraftingResult(owner));
    }

    return startIndex + listSize;
  }

  public Slot getMenuSlot(int index, int x, int y) {
    return slots.get(index).asSlot(this, index, x, y);
  }

  public InventoryType.SlotType getSlotType(int index) {
    return slots.get(index).getSlotType();
  }

  public @NotNull Component getTitle(@Nullable ServerPlayer viewer) {
    MutableComponent component = Component.empty();
    // Prefix for use with custom bitmap image fonts.
    if (owner.equals(viewer)) {
      component.append(
          Component.translatableWithFallback("openinv.container.inventory.self", "")
              .withStyle(style -> style
                  .withFont(ResourceLocation.parse("openinv:font/inventory"))
                  .withColor(ChatFormatting.WHITE)));
    } else {
      component.append(
          Component.translatableWithFallback("openinv.container.inventory.other", "")
              .withStyle(style -> style
                  .withFont(ResourceLocation.parse("openinv:font/inventory"))
                  .withColor(ChatFormatting.WHITE)));
    }
    // Normal title: "Inventory - OwnerName"
    component.append(Component.translatableWithFallback("openinv.container.inventory.prefix", "", owner.getName()))
        .append(Component.translatable("container.inventory"))
        .append(Component.translatableWithFallback("openinv.container.inventory.suffix", " - %s", owner.getName()));
    return component;
  }

  @Override
  public ServerPlayer getOwnerHandle() {
    return owner;
  }

  @Override
  public @NotNull org.bukkit.inventory.Inventory getBukkitInventory() {
    if (bukkitEntity == null) {
      bukkitEntity = new OpenPlayerInventory(this);
    }
    return bukkitEntity;
  }

  @Override
  public void setPlayerOnline(@NotNull org.bukkit.entity.Player player) {
    ServerPlayer newOwner = PlayerManager.getHandle(player);
    // Only transfer regular inventory - crafting and cursor slots are transient.
    newOwner.getInventory().replaceWith(owner.getInventory());
    owner = newOwner;
    // Update slots to point to new inventory.
    slots.forEach(slot -> slot.setHolder(newOwner));
  }

  @Override
  public void setPlayerOffline() {}

  @Override
  public boolean isInUse() {
    return !transaction.isEmpty();
  }

  @Override
  public @NotNull org.bukkit.entity.Player getPlayer() {
    return getOwner();
  }

  @Override
  public int getContainerSize() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return slots.stream().map(Content::get).allMatch(ItemStack::isEmpty);
  }

  @Override
  public ItemStack getItem(int index) {
    return slots.get(index).get();
  }

  @Override
  public ItemStack removeItem(int index, int amount) {
    return slots.get(index).removePartial(amount);
  }

  @Override
  public ItemStack removeItemNoUpdate(int index) {
    return slots.get(index).remove();
  }

  @Override
  public void setItem(int index, ItemStack itemStack) {
    slots.get(index).set(itemStack);
  }

  @Override
  public int getMaxStackSize() {
    return maxStackSize;
  }

  @Override
  public void setMaxStackSize(int maxStackSize) {
    this.maxStackSize = maxStackSize;
  }

  @Override
  public void setChanged() {}

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public List<ItemStack> getContents() {
    NonNullList<ItemStack> contents = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    for (int i = 0; i < getContainerSize(); ++i) {
      contents.set(i, getItem(i));
    }
    return contents;
  }

  @Override
  public void onOpen(CraftHumanEntity viewer) {
    transaction.add(viewer);
  }

  @Override
  public void onClose(CraftHumanEntity viewer) {
    transaction.remove(viewer);
  }

  @Override
  public List<HumanEntity> getViewers() {
    return transaction;
  }

  @Override
  public org.bukkit.entity.Player getOwner() {
    return owner.getBukkitEntity();
  }

  @Override
  public Location getLocation() {
    return owner.getBukkitEntity().getLocation();
  }

  @Override
  public void clearContent() {
    owner.getInventory().clearContent();
    owner.inventoryMenu.getCraftSlots().clearContent();
    owner.inventoryMenu.slotsChanged(owner.inventoryMenu.getCraftSlots());
    owner.containerMenu.setCarried(ItemStack.EMPTY);
  }

  public @Nullable AbstractContainerMenu createMenu(Player player, int i, boolean viewOnly) {
    if (player instanceof ServerPlayer serverPlayer) {
      return new OpenInventoryMenu(this, serverPlayer, i, viewOnly);
    }
    return null;
  }

}
