/*
 * Copyright (C) 2011-2023 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.internal.v1_21_R1.inventory;

import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.v1_21_R1.AnySilentContainer;
import com.lishid.openinv.internal.v1_21_R1.PlayerManager;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OpenEnderChest implements Container, StackedContentsCompatible, MenuProvider, ISpecialEnderChest {

  private CraftInventory inventory;
  private @NotNull ServerPlayer owner;
  private NonNullList<ItemStack> items;
  private int maxStack = 64;
  private final List<HumanEntity> transaction = new ArrayList<>();

  public OpenEnderChest(@NotNull org.bukkit.entity.Player player) {
    this.owner = PlayerManager.getHandle(player);
    this.items = owner.getEnderChestInventory().items;
  }

  @Override
  public @NotNull org.bukkit.inventory.Inventory getBukkitInventory() {
    if (inventory == null) {
      inventory = new CraftInventory(this) {
        @Override
        public @NotNull InventoryType getType() {
          return InventoryType.ENDER_CHEST;
        }
      };
    }
    return inventory;
  }

  @Override
  public void setPlayerOnline(@NotNull org.bukkit.entity.Player player) {
    owner = PlayerManager.getHandle(player);
    NonNullList<ItemStack> activeItems = owner.getEnderChestInventory().items;

    // Guard against size changing. Theoretically on Purpur all row variations still have 6 rows internally.
    int max = Math.min(items.size(), activeItems.size());
    for (int index = 0; index < max; ++index) {
      activeItems.set(index, items.get(index));
    }

    items = activeItems;
  }

  @Override
  public void setPlayerOffline() {}

  @Override
  public @NotNull org.bukkit.entity.Player getPlayer() {
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
  public void fillStackedContents(StackedContents stackedContents) {
    for (ItemStack itemstack : items) {
      stackedContents.accountStack(itemstack);
    }
  }

  @Override
  public Component getDisplayName() {
    return Component.translatableWithFallback("openinv.container.enderchest.prefix", "", owner.getName())
        .append(Component.translatable("container.enderchest"))
        .append(Component.translatableWithFallback("openinv.container.enderchest.suffix", " - %s", owner.getName()));
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
    int rows = (getContainerSize() % 9) + 1;
    return new ChestMenu(AnySilentContainer.getContainers(getContainerSize()), i, inventory, this, rows) {
      private CraftInventoryView view;
      @Override
      public CraftInventoryView getBukkitView() {
        if (view == null) {
          view = new CraftInventoryView(player.getBukkitEntity(), getBukkitInventory(), this);
        }
        return view;
      }
    };
  }

}
