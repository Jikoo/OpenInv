package com.lishid.openinv.internal.v1_21_R1.inventory;

import com.lishid.openinv.internal.InventoryViewTitle;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class OpenInventoryMenu extends AbstractContainerMenu {

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
          addSlot(new ContainerSlotEmpty.SlotEmpty(inventory, index, x, y));
          continue;
        }

        Slot slot = inventory.getMenuSlot(index, x, y);

        // Disallow access to cursor slot for own inventory - opens the door to a lot of ways to delete items.
        if (ownInv && slot instanceof ContainerSlotCursor.SlotCursor) {
          slot = new ContainerSlotEmpty.SlotEmpty(inventory, index, x, y);
        }
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

  static MenuType<?> getMenuType(OpenInventory inventory, ServerPlayer viewer) {
    int size = inventory.getContainerSize();
    if (inventory.getOwnerHandle().equals(viewer)) {
      size -= viewer.getInventory().items.size();
      size = ((int) Math.ceil(size / 9.0)) * 9;
    }

    return switch (size) {
      case 9 -> MenuType.GENERIC_9x1;
      case 18 -> MenuType.GENERIC_9x2;
      case 36 -> MenuType.GENERIC_9x4;
      case 45 -> MenuType.GENERIC_9x5;
      case 54 -> MenuType.GENERIC_9x6;
      // Default to 27-slot inventory.
      default -> MenuType.GENERIC_9x3;
    };
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

        @NotNull
        @Override
        public InventoryType.SlotType getSlotType(int slot) {
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
  public NonNullList<ItemStack> getItems() {
    NonNullList<ItemStack> items = NonNullList.withSize(slots.size(), ItemStack.EMPTY);
    for (Slot slot : slots) {
      if (!slot.isFake() && slot.hasItem()) {
        items.set(slot.index, slot.getItem());
      }
    }
    return items;
  }

  public void clicked(int i, int j, ClickType clickType, Player player) {
    try {
      this.modifiedClick(i, j, clickType, player);
    } catch (Exception var8) {
      CrashReport crashreport = CrashReport.forThrowable(var8, "Container click");
      CrashReportCategory crashreportsystemdetails = crashreport.addCategory("Click info");
      crashreportsystemdetails.setDetail("Menu Type", () -> this.getType() != null ? Objects.toString(BuiltInRegistries.MENU.getKey(this.getType())) : "<no type>");
      crashreportsystemdetails.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
      crashreportsystemdetails.setDetail("Slot Count", this.slots.size());
      crashreportsystemdetails.setDetail("Slot", i);
      crashreportsystemdetails.setDetail("Button", j);
      crashreportsystemdetails.setDetail("Type", clickType);
      throw new ReportedException(crashreport);
    }
  }

  private void modifiedClick(int index, int clickData, ClickType clickType, Player player) {
    if (clickType == ClickType.QUICK_CRAFT) {
      clickQuickCraft(index, clickData, player);
    } else if (this.quickcraftStatus != 0) {
      this.resetQuickCraft();
    } else if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (clickData == 0 || clickData == 1)) {
      ClickAction clickAction = clickData == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
      if (index == -999) {
        clickOutsideDrop(player, clickAction);
      } else if (clickType == ClickType.QUICK_MOVE) {
        clickQuickMove(index, player);
      } else {
        clickPickup(index, player, clickAction);
      }
    } else if (clickType == ClickType.SWAP && (clickData >= 0 && clickData < 9 || clickData == 40)) {
      clickSwap(index, clickData, player);
    } else if (clickType == ClickType.CLONE && player.hasInfiniteMaterials() && this.getCarried().isEmpty() && index >= 0) {
      Slot slot2 = this.slots.get(index);
      if (slot2.hasItem()) {
        ItemStack itemstack1x = slot2.getItem();
        this.setCarried(itemstack1x.copyWithCount(itemstack1x.getMaxStackSize()));
      }
    } else if (clickType == ClickType.THROW && this.getCarried().isEmpty() && index >= 0) {
      clickThrow(index, clickData, player);
    } else if (clickType == ClickType.PICKUP_ALL && index >= 0) {
      clickPickupAll(index, clickData, player);
    }
  }

  //<editor-fold desc="If you don't look at it, it probably can't hurt you." defaultstate="collapsed">
  private int quickcraftStatus;
  private int quickcraftType;
  private final Set<Slot> quickcraftSlots = new HashSet<>();

  // <strike>A little slice of purgatory</strike> Drag clicks
  private void clickQuickCraft(int index, int clickData, Player player) {
    int previousStatus = this.quickcraftStatus;
    this.quickcraftStatus = getQuickcraftHeader(clickData);
    if ((previousStatus != 1 || this.quickcraftStatus != 2) && previousStatus != this.quickcraftStatus) {
      this.resetQuickCraft();
    } else if (this.getCarried().isEmpty()) {
      this.resetQuickCraft();
    } else if (this.quickcraftStatus == 0) {
      this.quickcraftType = getQuickcraftType(clickData);
      if (isValidQuickcraftType(this.quickcraftType, player)) {
        this.quickcraftStatus = 1;
        this.quickcraftSlots.clear();
      } else {
        this.resetQuickCraft();
      }
    } else if (this.quickcraftStatus == 1) {
      Slot slot = this.slots.get(index);
      ItemStack itemstack = this.getCarried();
      if (canItemQuickReplace(slot, itemstack, true)
          && slot.mayPlace(itemstack)
          && (this.quickcraftType == 2 || itemstack.getCount() > this.quickcraftSlots.size())
          && this.canDragTo(slot)) {
        this.quickcraftSlots.add(slot);
      }
    } else if (this.quickcraftStatus == 2) {
      if (!this.quickcraftSlots.isEmpty()) {
        ItemStack itemstack1 = this.getCarried().copy();
        if (itemstack1.isEmpty()) {
          this.resetQuickCraft();
          return;
        }

        int count = this.getCarried().getCount();
        Iterator<Slot> iterator = this.quickcraftSlots.iterator();
        Map<Integer, ItemStack> draggedSlots = new HashMap<>();

        while (iterator.hasNext()) {
          Slot slot1 = iterator.next();
          ItemStack itemstack2 = this.getCarried();
          if (slot1 != null
              && canItemQuickReplace(slot1, itemstack2, true)
              && slot1.mayPlace(itemstack2)
              && (this.quickcraftType == 2 || itemstack2.getCount() >= this.quickcraftSlots.size())
              && this.canDragTo(slot1)) {
            int inSlot = slot1.hasItem() ? slot1.getItem().getCount() : 0;
            int max = Math.min(itemstack1.getMaxStackSize(), slot1.getMaxStackSize(itemstack1));
            int targetNumber = Math.min(getQuickCraftPlaceCount(this.quickcraftSlots, this.quickcraftType, itemstack1) + inSlot, max);
            count -= targetNumber - inSlot;
            draggedSlots.put(slot1.index, itemstack1.copyWithCount(targetNumber));
          }
        }

        InventoryView view = this.getBukkitView();
        org.bukkit.inventory.ItemStack newCursor = CraftItemStack.asCraftMirror(itemstack1);
        newCursor.setAmount(count);
        Map<Integer, org.bukkit.inventory.ItemStack> eventMap = new HashMap<>();

        for (Map.Entry<Integer, ItemStack> draggedEntry : draggedSlots.entrySet()) {
          eventMap.put(draggedEntry.getKey(), CraftItemStack.asBukkitCopy(draggedEntry.getValue()));
        }

        ItemStack oldCursor = this.getCarried();
        this.setCarried(CraftItemStack.asNMSCopy(newCursor));
        InventoryDragEvent event = new InventoryDragEvent(
            view,
            newCursor.getType() != Material.AIR ? newCursor : null,
            CraftItemStack.asBukkitCopy(oldCursor),
            this.quickcraftType == 1,
            eventMap
        );
        player.level().getCraftServer().getPluginManager().callEvent(event);
        boolean needsUpdate = event.getResult() != Event.Result.DEFAULT;
        if (event.getResult() != Event.Result.DENY) {
          for (Map.Entry<Integer, ItemStack> dslot : draggedSlots.entrySet()) {
            view.setItem(dslot.getKey(), CraftItemStack.asBukkitCopy(dslot.getValue()));
          }

          if (this.getCarried() != null) {
            this.setCarried(CraftItemStack.asNMSCopy(event.getCursor()));
            needsUpdate = true;
          }
        } else {
          this.setCarried(oldCursor);
        }

        if (needsUpdate && player instanceof ServerPlayer) {
          this.sendAllDataToRemote();
        }
      }

      this.resetQuickCraft();
    } else {
      this.resetQuickCraft();
    }
  }

  @Override
  protected void resetQuickCraft() {
    this.quickcraftStatus = 0;
    this.quickcraftSlots.clear();
  }
  //</editor-fold> I lied, it absolutely can hurt you.

  // Normal clicks outside of window
  private void clickOutsideDrop(Player player, ClickAction clickAction) {
    if (!this.getCarried().isEmpty()) {
      if (clickAction == ClickAction.PRIMARY) {
        ItemStack carried = this.getCarried();
        this.setCarried(ItemStack.EMPTY);
        player.drop(carried, true);
      } else {
        player.drop(this.getCarried().split(1), true);
      }
    }
  }

  // Shift-clicks
  private void clickQuickMove(int index, Player player) {
    if (index < 0) {
      return;
    }

    Slot slot = this.slots.get(index);
    if (!slot.mayPickup(player)) {
      return;
    }

    ItemStack itemstack = this.quickMoveStack(player, index);

    while (!itemstack.isEmpty() && ItemStack.isSameItem(slot.getItem(), itemstack)) {
      itemstack = this.quickMoveStack(player, index);
    }
  }

  // Normal clicks
  private void clickPickup(int index, Player player, ClickAction clickAction) {
    if (index < 0) {
      return;
    }

    Slot slot = this.slots.get(index);
    ItemStack slotItem = slot.getItem();
    ItemStack carried = this.getCarried();
    player.updateTutorialInventoryAction(carried, slot.getItem(), clickAction);
    if (!this.tryItemClickBehaviourOverride(player, clickAction, slot, slotItem, carried)) {
      if (!slot.hasItem()) { // OI: use slot's interpretation of state.
        if (!carried.isEmpty()) {
          int amount = clickAction == ClickAction.PRIMARY ? carried.getCount() : 1;
          this.setCarried(slot.safeInsert(carried, amount));
        }
      } else if (slot.mayPickup(player)) {
        if (carried.isEmpty()) {
          int amount = clickAction == ClickAction.PRIMARY ? slotItem.getCount() : (slotItem.getCount() + 1) / 2;
          Optional<ItemStack> optional = slot.tryRemove(amount, Integer.MAX_VALUE, player);
          optional.ifPresent(itemstack4 -> {
            this.setCarried(itemstack4);
            slot.onTake(player, itemstack4);
          });
        } else if (slot.mayPlace(carried)) {
          if (ItemStack.isSameItemSameComponents(slotItem, carried)) {
            int amount = clickAction == ClickAction.PRIMARY ? carried.getCount() : 1;
            this.setCarried(slot.safeInsert(carried, amount));
          } else if (carried.getCount() <= slot.getMaxStackSize(carried)) {
            this.setCarried(slotItem);
            slot.setByPlayer(carried);
          }
        } else if (ItemStack.isSameItemSameComponents(slotItem, carried)) {
          Optional<ItemStack> optional = slot.tryRemove(
              slotItem.getCount(), carried.getMaxStackSize() - carried.getCount(), player
          );
          optional.ifPresent(itemstack4 -> {
            carried.grow(itemstack4.getCount());
            slot.onTake(player, itemstack4);
          });
        }
      }
    }

    slot.setChanged();
    if (player instanceof ServerPlayer && slot.getMaxStackSize() != 64) {
      ((ServerPlayer) player).connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), slot.index, slot.getItem()));
      if (this.getBukkitView().getType() == InventoryType.WORKBENCH || this.getBukkitView().getType() == InventoryType.CRAFTING) {
        ((ServerPlayer) player).connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0, this.getSlot(0).getItem()));
      }
    }
  }

  // Hotbar buttons
  private void clickSwap(int index, int clickData, Player player) {
    Inventory playerInventory = player.getInventory();
    ItemStack hotbarItem = playerInventory.getItem(clickData);
    Slot slot = this.slots.get(index);
    if (!hotbarItem.isEmpty() || slot.hasItem()) { // OI: use slot's interpretation of state.
      if (hotbarItem.isEmpty()) {
        if (slot.mayPickup(player)) {
          ItemStack itemStack = slot.getItem();
          playerInventory.setItem(clickData, itemStack);
          //slot.onSwapCraft(itemstackx.getCount());
          slot.setByPlayer(ItemStack.EMPTY);
          slot.onTake(player, itemStack);
        }
      } else if (!slot.hasItem()) { // OI: use slot's interpretation of state.
        if (slot.mayPlace(hotbarItem)) {
          int j2 = slot.getMaxStackSize(hotbarItem);
          if (hotbarItem.getCount() > j2) {
            slot.setByPlayer(hotbarItem.split(j2));
          } else {
            playerInventory.setItem(clickData, ItemStack.EMPTY);
            slot.setByPlayer(hotbarItem);
          }
        }
      } else if (slot.mayPickup(player) && slot.mayPlace(hotbarItem)) {
        int max = slot.getMaxStackSize(hotbarItem);
        ItemStack itemStack = slot.getItem();
        if (hotbarItem.getCount() > max) {
          slot.setByPlayer(hotbarItem.split(max));
          slot.onTake(player, itemStack);
          if (!playerInventory.add(itemStack)) {
            player.drop(itemStack, true);
          }
        } else {
          playerInventory.setItem(clickData, itemStack);
          slot.setByPlayer(hotbarItem);
          slot.onTake(player, itemStack);
        }
      }
    }
  }

  // Drop item key while hovering a slot
  private void clickThrow(int index, int clickData, Player player) {
    // TODO drop as player if drop slot
    Slot slot = this.slots.get(index);
    int amount = clickData == 0 ? 1 : slot.getItem().getCount();
    ItemStack itemstackx = slot.safeTake(amount, Integer.MAX_VALUE, player);
    player.drop(itemstackx, true);
  }

  // Double click gather item
  private void clickPickupAll(int index, int clickData, Player player) {
    Slot slot2 = this.slots.get(index);
    ItemStack itemstack1x = this.getCarried();
    if (!itemstack1x.isEmpty() && (!slot2.hasItem() || !slot2.mayPickup(player))) {
      int l = clickData == 0 ? 0 : this.slots.size() - 1;
      int j2 = clickData == 0 ? 1 : -1;

      for (int i2 = 0; i2 < 2; i2++) {
        for (int k2 = l; k2 >= 0 && k2 < this.slots.size() && itemstack1x.getCount() < itemstack1x.getMaxStackSize(); k2 += j2) {
          Slot slot3 = this.slots.get(k2);
          if (slot3.hasItem()
              && canItemQuickReplace(slot3, itemstack1x, true)
              && slot3.mayPickup(player)
              && this.canTakeItemForPickAll(itemstack1x, slot3)) {
            ItemStack itemstack5 = slot3.getItem();
            if (i2 != 0 || itemstack5.getCount() != itemstack5.getMaxStackSize()) {
              ItemStack itemstack6 = slot3.safeTake(
                  itemstack5.getCount(), itemstack1x.getMaxStackSize() - itemstack1x.getCount(), player
              );
              itemstack1x.grow(itemstack6.getCount());
            }
          }
        }
      }
    }
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    // See net.minecraft.world.inventory.ChestMenu#quickMoveStack(Player, int)
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);
    if (slot.hasItem()) {
      ItemStack itemstack1 = slot.getItem();
      itemstack = itemstack1.copy();
      int topSize = slots.size() - 36;
      if (index < topSize) {
        if (!this.moveItemStackTo(itemstack1, topSize, this.slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.moveItemStackTo(itemstack1, 0, topSize, false)) {
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

  private boolean tryItemClickBehaviourOverride(Player entityhuman, ClickAction clickaction, Slot slot, ItemStack itemstack, ItemStack itemstack1) {
    FeatureFlagSet featureflagset = entityhuman.level().enabledFeatures();
    return itemstack1.isItemEnabled(featureflagset) && itemstack1.overrideStackedOnOther(slot, clickaction, entityhuman) || itemstack.isItemEnabled(featureflagset)
        && itemstack.overrideOtherStackedOnMe(itemstack1, slot, clickaction, entityhuman, this.createCarriedSlotAccess());
  }

  private SlotAccess createCarriedSlotAccess() {
    return new SlotAccess() {
      @Override
      public ItemStack get() {
        return getCarried();
      }

      @Override
      public boolean set(ItemStack itemstack) {
        setCarried(itemstack);
        return true;
      }
    };
  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  /**
   * Reimplementation of {@link AbstractContainerMenu#moveItemStackTo(ItemStack, int, int, boolean)} that ignores fake
   * slots and respects {@link Slot#hasItem()}.
   * 
   * @param itemStack the stack to quick-move
   * @param rangeLow the start of the range of slots that can be moved to, inclusive
   * @param rangeHigh the end of the range of slots that can be moved to, exclusive
   * @param topDown whether to start at the top of the range or bottom
   * @return whether the stack was modified as a result of being quick-moved
   */
  protected boolean moveItemStackTo(ItemStack itemStack, int rangeLow, int rangeHigh, boolean topDown) {
    boolean modified = false;

    if (itemStack.isStackable()) {
      for (int index = topDown ? rangeHigh - 1 : rangeLow;
           !itemStack.isEmpty() && topDown ? index >= rangeLow : index < rangeHigh;
           index += topDown ? -1 : 1
      ) {
        Slot slot = slots.get(index);
        if (!slot.isFake() && slot.mayPlace(itemStack) && slot.hasItem()) {
          modified = addToExistingStack(itemStack, slot);
        }
      }
    }

    for (int index = topDown ? rangeHigh - 1 : rangeLow;
         !itemStack.isEmpty() && topDown ? index >= rangeLow : index < rangeHigh;
         index += topDown ? -1 : 1
    ) {
      Slot slot = slots.get(index);
      if (!slot.isFake() && slot.mayPlace(itemStack) && !slot.hasItem()) {
        // If there's no item here, add as many as we can of the item.
        slot.setByPlayer(itemStack.split(Math.min(itemStack.getCount(), slot.getMaxStackSize(itemStack))));
        slot.setChanged();
        modified = true;
        break;
      }
    }

    return modified;
  }

  private static boolean addToExistingStack(ItemStack itemStack, Slot slot) {
    ItemStack existing = slot.getItem();

    // If the items aren't the same, we can't add our item.
    if (!ItemStack.isSameItemSameComponents(itemStack, existing)) {
      return false;
    }

    int total = existing.getCount() + itemStack.getCount();
    int max = slot.getMaxStackSize(existing);

    // If the existing item can accept the entirety of our item, we're done!
    if (total <= max) {
      itemStack.setCount(0);
      existing.setCount(total);
      slot.setChanged();
      return true;
    }

    // Otherwise, add as many as we can.
    itemStack.shrink(max - existing.getCount());
    existing.setCount(max);
    slot.setChanged();
    return true;
  }

  @Override
  public boolean canDragTo(Slot slot) {
    return !(slot instanceof ContainerSlotDrop.SlotDrop || slot instanceof ContainerSlotEmpty.SlotEmpty);
  }

}
