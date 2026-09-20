package com.lishid.openinv.internal.paper26_3.container;

import com.lishid.openinv.internal.ISpecialEnderChest;
import com.github.jikoo.openinv.internal.container.InternalOwned;
import com.github.jikoo.openinv.internal.container.slot.InventoryFactory;
import com.lishid.openinv.internal.paper26_3.container.menu.OpenChestMenu;
import com.lishid.openinv.internal.paper26_3.container.menu.OpenEnderChestMenu;
import com.lishid.openinv.internal.paper26_3.player.PlayerManager;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class OpenEnderChest implements Container, StackedContentsCompatible, InternalOwned<ServerPlayer>,
    ISpecialEnderChest {

  private @Nullable CraftInventory inventory;
  private ServerPlayer owner;
  private NonNullList<ItemStack> items;
  private int maxStack = 64;
  private final List<HumanEntity> transaction = new ArrayList<>();

  public OpenEnderChest(org.bukkit.entity.Player player) {
    this.owner = PlayerManager.getHandle(player);
    this.items = getEnderChestItems(owner);
  }

  @Override
  public ServerPlayer getOwnerHandle() {
    return owner;
  }

  @Override
  public org.bukkit.inventory.Inventory getBukkitInventory() {
    if (inventory == null) {
      inventory = new CraftInventory(this) {
        @Override
        public InventoryType getType() {
          return InventoryType.ENDER_CHEST;
        }
      };
    }
    return inventory;
  }

  @Override
  public void setPlayerOnline(org.bukkit.entity.Player player) {
    owner = PlayerManager.getHandle(player);
    NonNullList<ItemStack> activeItems = getEnderChestItems(owner);

    // Guard against size changing. Theoretically on Purpur all row variations still have 6 rows internally.
    int max = Math.min(items.size(), activeItems.size());
    for (int index = 0; index < max; ++index) {
      activeItems.set(index, items.get(index));
    }

    items = activeItems;
  }

  protected NonNullList<ItemStack> getEnderChestItems(ServerPlayer owner) {
    return owner.getEnderChestInventory().getItems();
  }

  @Override
  public org.bukkit.entity.Player getPlayer() {
    return owner.getBukkitEntity();
  }

  @Override
  public int getContainerSize() {
    return items.size();
  }

  @Override
  public boolean isEmpty() {
    return items.stream().allMatch(ItemStack::isEmpty);
  }

  @Override
  public ItemStack getItem(int index) {
    return index >= 0 && index < items.size() ? items.get(index) : ItemStack.EMPTY;
  }

  @Override
  public ItemStack removeItem(int index, int amount) {
    ItemStack itemstack = ContainerHelper.removeItem(items, index, amount);

    if (!itemstack.isEmpty()) {
      setChanged();
    }

    return itemstack;
  }

  @Override
  public ItemStack removeItemNoUpdate(int index) {
    return index >= 0 && index < items.size() ? items.set(index, ItemStack.EMPTY) : ItemStack.EMPTY;
  }

  @Override
  public void setItem(int index, ItemStack itemStack) {
    if (index >= 0 && index < items.size()) {
      items.set(index, itemStack);
    }
  }

  @Override
  public int getMaxStackSize() {
    return maxStack;
  }

  @Override
  public void setChanged() {
    this.owner.getEnderChestInventory().setChanged();
  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public List<ItemStack> getContents() {
    return items;
  }

  @Override
  public void onOpen(CraftHumanEntity craftHumanEntity) {
    transaction.add(craftHumanEntity);
  }

  @Override
  public void onClose(CraftHumanEntity craftHumanEntity) {
    transaction.remove(craftHumanEntity);
  }

  @Override
  public List<HumanEntity> getViewers() {
    return transaction;
  }

  @Override
  public org.bukkit.entity.Player getOwner() {
    return getPlayer();
  }

  @Override
  public void setMaxStackSize(int size) {
    maxStack = size;
  }

  @Override
  public @Nullable Location getLocation() {
    return null;
  }

  @Override
  public void clearContent() {
    items.clear();
    setChanged();
  }

  @Override
  public void fillStackedContents(StackedItemContents stackedContents) {
    for (ItemStack itemstack : items) {
      stackedContents.accountStack(itemstack);
    }
  }

  public Component getTitle(@Nullable OpenChestMenu<?> menu) {
    MutableComponent component;
    if (menu != null && menu.isViewOnly()) {
      component = Component.translatableWithFallback("openinv.container.enderchest.viewonly", "[RO] ");
    } else {
      component = Component.translatableWithFallback("openinv.container.enderchest.editable", "");
    }
    return component
        .append(Component.translatableWithFallback("openinv.container.enderchest.prefix", "", owner.getName()))
        .append(Component.translatable("container.enderchest"))
        .append(Component.translatableWithFallback("openinv.container.enderchest.suffix", " - %s", owner.getName()));
  }

  public @Nullable OpenChestMenu<?> createMenu(
      InventoryFactory<ServerPlayer, ItemStack, Container, Slot, EquipmentSlot> factory,
      Player player,
      int i,
      boolean viewOnly
  ) {
    if (player instanceof ServerPlayer serverPlayer) {
      return new OpenEnderChestMenu(factory, this, serverPlayer, i, viewOnly);
    }
    return null;
  }

}
