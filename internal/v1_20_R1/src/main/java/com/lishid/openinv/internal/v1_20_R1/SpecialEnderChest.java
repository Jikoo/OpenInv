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

package com.lishid.openinv.internal.v1_20_R1;

import com.lishid.openinv.internal.ISpecialEnderChest;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpecialEnderChest extends PlayerEnderChestContainer implements ISpecialEnderChest {

    private final CraftInventory inventory;
    private ServerPlayer owner;
    private NonNullList<ItemStack> items;
    private boolean playerOnline;

    public SpecialEnderChest(final org.bukkit.entity.Player player, final Boolean online) {
        super(PlayerDataManager.getHandle(player));
        this.inventory = new CraftInventory(this);
        this.owner = PlayerDataManager.getHandle(player);
        this.playerOnline = online;
        this.items = this.owner.getEnderChestInventory().items;
    }

    @Override
    public @NotNull CraftInventory getBukkitInventory() {
        return inventory;
    }

    @Override
    public void setPlayerOffline() {
        this.playerOnline = false;
    }

    @Override
    public void setPlayerOnline(@NotNull final org.bukkit.entity.Player player) {
        if (this.playerOnline) {
            return;
        }

        ServerPlayer offlinePlayer = this.owner;
        ServerPlayer onlinePlayer = PlayerDataManager.getHandle(player);

        // Set owner to new player.
        this.owner = onlinePlayer;

        // Set player's ender chest contents to our modified contents.
        PlayerEnderChestContainer onlineEnderChest = onlinePlayer.getEnderChestInventory();
        for (int i = 0; i < onlineEnderChest.getContainerSize(); ++i) {
            onlineEnderChest.setItem(i, this.items.get(i));
        }

        // Set our item array to the new inventory's array.
        this.items = onlineEnderChest.items;

        // Add viewers to new inventory.
        onlineEnderChest.transaction.addAll(offlinePlayer.getEnderChestInventory().transaction);

        this.playerOnline = true;
    }

    @Override
    public @NotNull org.bukkit.entity.Player getPlayer() {
        return owner.getBukkitEntity();
    }

    @Override
    public void setChanged() {
        this.owner.getEnderChestInventory().setChanged();
    }

    @Override
    public List<ItemStack> getContents() {
        return this.items;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        this.owner.getEnderChestInventory().onOpen(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        this.owner.getEnderChestInventory().onClose(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return this.owner.getEnderChestInventory().getViewers();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void setActiveChest(EnderChestBlockEntity enderChest) {
        this.owner.getEnderChestInventory().setActiveChest(enderChest);
    }

    @Override
    public boolean isActiveChest(EnderChestBlockEntity enderChest) {
        return this.owner.getEnderChestInventory().isActiveChest(enderChest);
    }

    @Override
    public int getMaxStackSize() {
        return this.owner.getEnderChestInventory().getMaxStackSize();
    }

    @Override
    public void setMaxStackSize(int i) {
        this.owner.getEnderChestInventory().setMaxStackSize(i);
    }

    @Override
    public InventoryHolder getOwner() {
        return this.owner.getEnderChestInventory().getOwner();
    }

    @Override
    public @Nullable Location getLocation() {
        return null;
    }

    @Override
    public void addListener(ContainerListener listener) {
        this.owner.getEnderChestInventory().addListener(listener);
    }

    @Override
    public void removeListener(ContainerListener listener) {
        this.owner.getEnderChestInventory().removeListener(listener);
    }

    @Override
    public ItemStack getItem(int i) {
        return i >= 0 && i < this.items.size() ? this.items.get(i) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack itemstack = ContainerHelper.removeItem(this.items, i, j);
        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    @Override
    public ItemStack addItem(ItemStack itemstack) {
        ItemStack localItem = itemstack.copy();
        this.moveItemToOccupiedSlotsWithSameType(localItem);
        if (localItem.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.moveItemToEmptySlots(localItem);
            return localItem.isEmpty() ? ItemStack.EMPTY : localItem;
        }
    }

    @Override
    public boolean canAddItem(ItemStack itemstack) {
        for (ItemStack itemstack1 : this.items) {
            if (itemstack1.isEmpty() || ItemStack.isSameItemSameTags(itemstack1, itemstack) && itemstack1.getCount() < itemstack1.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

    private void moveItemToEmptySlots(ItemStack itemstack) {
        for(int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack localItem = this.getItem(i);
            if (localItem.isEmpty()) {
                this.setItem(i, itemstack.copy());
                itemstack.setCount(0);
                return;
            }
        }
    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack itemstack) {
        for(int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack localItem = this.getItem(i);
            if (ItemStack.isSameItemSameTags(localItem, itemstack)) {
                this.moveItemsBetweenStacks(itemstack, localItem);
                if (itemstack.isEmpty()) {
                    return;
                }
            }
        }
    }

    private void moveItemsBetweenStacks(ItemStack itemstack, ItemStack itemstack1) {
        int i = Math.min(this.getMaxStackSize(), itemstack1.getMaxStackSize());
        int j = Math.min(itemstack.getCount(), i - itemstack1.getCount());
        if (j > 0) {
            itemstack1.grow(j);
            itemstack.shrink(j);
            this.setChanged();
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        ItemStack itemstack = this.items.get(i);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.items.set(i, ItemStack.EMPTY);
            return itemstack;
        }
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        this.items.set(i, itemstack);
        if (!itemstack.isEmpty() && itemstack.getCount() > this.getMaxStackSize()) {
            itemstack.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    @Override
    public int getContainerSize() {
        return this.owner.getEnderChestInventory().getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public void startOpen(Player player) {
    }

    @Override
    public void stopOpen(Player player) {
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemstack) {
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    @Override
    public void fillStackedContents(StackedContents stackedContents) {
        for (ItemStack itemstack : this.items) {
            stackedContents.accountStack(itemstack);
        }

    }

    @Override
    public List<ItemStack> removeAllItems() {
        List<ItemStack> list = this.items.stream().filter(Predicate.not(ItemStack::isEmpty)).collect(Collectors.toList());
        this.clearContent();
        return list;
    }

    @Override
    public ItemStack removeItemType(Item item, int i) {
        ItemStack itemstack = new ItemStack(item, 0);

        for(int j = this.getContainerSize() - 1; j >= 0; --j) {
            ItemStack localItem = this.getItem(j);
            if (localItem.getItem().equals(item)) {
                int k = i - itemstack.getCount();
                ItemStack splitItem = localItem.split(k);
                itemstack.grow(splitItem.getCount());
                if (itemstack.getCount() == i) {
                    break;
                }
            }
        }

        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    @Override
    public String toString() {
        return this.items.stream().filter((itemStack) -> !itemStack.isEmpty()).toList().toString();
    }

    @Override
    public void fromTag(ListTag listTag) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            this.setItem(i, ItemStack.EMPTY);
        }

        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            int j = compoundTag.getByte("Slot") & 255;
            if (j < this.getContainerSize()) {
                this.setItem(j, ItemStack.of(compoundTag));
            }
        }

    }

}
