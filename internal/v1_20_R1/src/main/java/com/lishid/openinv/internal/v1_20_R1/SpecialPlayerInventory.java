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

import com.google.common.collect.ImmutableList;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpecialPlayerInventory extends Inventory implements ISpecialPlayerInventory {

    private final CraftInventory inventory;
    private boolean playerOnline;
    private Player player;
    private NonNullList<ItemStack> items;
    private NonNullList<ItemStack> armor;
    private NonNullList<ItemStack> offhand;
    private List<NonNullList<ItemStack>> compartments;

    public SpecialPlayerInventory(@NotNull org.bukkit.entity.Player bukkitPlayer, @NotNull Boolean online) {
        super(PlayerDataManager.getHandle(bukkitPlayer));
        this.inventory = new CraftInventory(this);
        this.playerOnline = online;
        this.player = super.player;
        this.selected = player.getInventory().selected;
        this.items = this.player.getInventory().items;
        this.armor = this.player.getInventory().armor;
        this.offhand = this.player.getInventory().offhand;
        this.compartments = ImmutableList.of(this.items, this.armor, this.offhand);
    }

    @Override
    public void setPlayerOnline(@NotNull org.bukkit.entity.Player player) {
        if (this.playerOnline) {
            return;
        }

        Player offlinePlayer = this.player;
        Player onlinePlayer = PlayerDataManager.getHandle(player);
        onlinePlayer.getInventory().transaction.addAll(this.transaction);

        // Set owner to new player.
        this.player = onlinePlayer;

        // Set player's inventory contents to our modified contents.
        Inventory onlineInventory = onlinePlayer.getInventory();
        for (int i = 0; i < getContainerSize(); ++i) {
            onlineInventory.setItem(i, getRawItem(i));
        }
        onlineInventory.selected = this.selected;

        // Set our item arrays to the new inventory's arrays.
        this.items = onlineInventory.items;
        this.armor = onlineInventory.armor;
        this.offhand = onlineInventory.offhand;
        this.compartments = ImmutableList.of(this.items, this.armor, this.offhand);

        // Add existing viewers to new viewer list.
        Inventory offlineInventory = offlinePlayer.getInventory();
        // Remove self from listing - player is always a viewer of their own inventory, prevent duplicates.
        offlineInventory.transaction.remove(offlinePlayer.getBukkitEntity());
        onlineInventory.transaction.addAll(offlineInventory.transaction);

        this.playerOnline = true;
    }

    @Override
    public @NotNull CraftInventory getBukkitInventory() {
        return this.inventory;
    }

    @Override
    public void setPlayerOffline() {
        this.playerOnline = false;
    }

    @Override
    public @NotNull HumanEntity getPlayer() {
        return this.player.getBukkitEntity();
    }

    private @NotNull ItemStack getRawItem(int i) {
        if (i < 0) {
            return ItemStack.EMPTY;
        }

        NonNullList<ItemStack> list;
        for (Iterator<NonNullList<ItemStack>> iterator = this.compartments.iterator(); iterator.hasNext(); i -= list.size()) {
            list = iterator.next();
            if (i < list.size()) {
                return list.get(i);
            }
        }

        return ItemStack.EMPTY;
    }

    private void setRawItem(int i, @NotNull ItemStack itemStack) {
        if (i < 0) {
            return;
        }

        NonNullList<ItemStack> list;
        for (Iterator<NonNullList<ItemStack>> iterator = this.compartments.iterator(); iterator.hasNext(); i -= list.size()) {
            list = iterator.next();
            if (i < list.size()) {
                list.set(i, itemStack);
            }
        }
    }

    private record IndexedCompartment(@Nullable NonNullList<ItemStack> compartment, int index) {}

    private @NotNull SpecialPlayerInventory.IndexedCompartment getIndexedContent(int index) {
        if (index < items.size()) {
            return new IndexedCompartment(items, getReversedItemSlotNum(index));
        }

        index -= items.size();

        if (index < armor.size()) {
            return new IndexedCompartment(armor, getReversedArmorSlotNum(index));
        }

        index -= armor.size();

        if (index < offhand.size()) {
            return new IndexedCompartment(offhand, index);
        }

        index -= offhand.size();

        return new IndexedCompartment(null, index);
    }

    private int getReversedArmorSlotNum(final int i) {
        if (i == 0) {
            return 3;
        }
        if (i == 1) {
            return 2;
        }
        if (i == 2) {
            return 1;
        }
        if (i == 3) {
            return 0;
        }
        return i;
    }

    private int getReversedItemSlotNum(final int i) {
        if (i >= 27) {
            return i - 27;
        }
        return i + 9;
    }

    private boolean contains(Predicate<ItemStack> predicate) {
        return this.compartments.stream().flatMap(NonNullList::stream).anyMatch(predicate);
    }

    @Override
    public List<ItemStack> getArmorContents() {
        return this.armor;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        this.player.getInventory().onOpen(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        this.player.getInventory().onClose(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return this.player.getInventory().getViewers();
    }

    @Override
    public InventoryHolder getOwner() {
        return this.player.getBukkitEntity();
    }

    @Override
    public int getMaxStackSize() {
        return this.player.getInventory().getMaxStackSize();
    }

    @Override
    public void setMaxStackSize(int size) {
        this.player.getInventory().setMaxStackSize(size);
    }

    @Override
    public Location getLocation() {
        return this.player.getBukkitEntity().getLocation();
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public List<ItemStack> getContents() {
        return this.compartments.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public ItemStack getSelected() {
        return isHotbarSlot(this.selected) ? this.items.get(this.selected) : ItemStack.EMPTY;
    }

    private boolean hasRemainingSpaceForItem(ItemStack itemstack, ItemStack itemstack1) {
        return !itemstack.isEmpty() && ItemStack.isSameItemSameTags(itemstack, itemstack1) && itemstack.isStackable() && itemstack.getCount() < itemstack.getMaxStackSize() && itemstack.getCount() < this.getMaxStackSize();
    }

    @Override
    public int canHold(ItemStack itemstack) {
        int remains = itemstack.getCount();

        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemstack1 = this.getRawItem(i);
            if (itemstack1.isEmpty()) {
                return itemstack.getCount();
            }

            if (this.hasRemainingSpaceForItem(itemstack1, itemstack)) {
                remains -= (itemstack1.getMaxStackSize() < this.getMaxStackSize() ? itemstack1.getMaxStackSize() : this.getMaxStackSize()) - itemstack1.getCount();
            }

            if (remains <= 0) {
                return itemstack.getCount();
            }
        }

        ItemStack offhandItemStack = this.getRawItem(this.items.size() + this.armor.size());
        if (this.hasRemainingSpaceForItem(offhandItemStack, itemstack)) {
            remains -= (offhandItemStack.getMaxStackSize() < this.getMaxStackSize() ? offhandItemStack.getMaxStackSize() : this.getMaxStackSize()) - offhandItemStack.getCount();
        }

        return remains <= 0 ? itemstack.getCount() : itemstack.getCount() - remains;
    }

    @Override
    public int getFreeSlot() {
        for(int i = 0; i < this.items.size(); ++i) {
            if (this.items.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void setPickedItem(ItemStack itemstack) {
        int i = this.findSlotMatchingItem(itemstack);
        if (isHotbarSlot(i)) {
            this.selected = i;
        } else if (i == -1) {
            this.selected = this.getSuitableHotbarSlot();
            if (!this.items.get(this.selected).isEmpty()) {
                int j = this.getFreeSlot();
                if (j != -1) {
                    this.items.set(j, this.items.get(this.selected));
                }
            }

            this.items.set(this.selected, itemstack);
        } else {
            this.pickSlot(i);
        }

    }

    @Override
    public void pickSlot(int i) {
        this.selected = this.getSuitableHotbarSlot();
        ItemStack itemstack = this.items.get(this.selected);
        this.items.set(this.selected, this.items.get(i));
        this.items.set(i, itemstack);
    }

    @Override
    public int findSlotMatchingItem(ItemStack itemstack) {
        for(int i = 0; i < this.items.size(); ++i) {
            if (!this.items.get(i).isEmpty() && ItemStack.isSameItemSameTags(itemstack, this.items.get(i))) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int findSlotMatchingUnusedItem(ItemStack itemStack) {
        for(int i = 0; i < this.items.size(); ++i) {
            ItemStack localItem = this.items.get(i);
            if (!this.items.get(i).isEmpty() && ItemStack.isSameItemSameTags(itemStack, this.items.get(i)) && !this.items.get(i).isDamaged() && !localItem.isEnchanted() && !localItem.hasCustomHoverName()) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int getSuitableHotbarSlot() {
        int i;
        int j;
        for(j = 0; j < 9; ++j) {
            i = (this.selected + j) % 9;
            if (this.items.get(i).isEmpty()) {
                return i;
            }
        }

        for(j = 0; j < 9; ++j) {
            i = (this.selected + j) % 9;
            if (!this.items.get(i).isEnchanted()) {
                return i;
            }
        }

        return this.selected;
    }

    @Override
    public void swapPaint(double d0) {
        if (d0 > 0.0D) {
            d0 = 1.0D;
        }

        if (d0 < 0.0D) {
            d0 = -1.0D;
        }

        this.selected = (int) (this.selected - d0);

        while (this.selected < 0) {
            this.selected += 9;
        }

        while(this.selected >= 9) {
            this.selected -= 9;
        }
    }

    @Override
    public int clearOrCountMatchingItems(Predicate<ItemStack> predicate, int i, Container container) {
        byte b0 = 0;
        boolean flag = i == 0;
        int j = b0 + ContainerHelper.clearOrCountMatchingItems(this, predicate, i - b0, flag);
        j += ContainerHelper.clearOrCountMatchingItems(container, predicate, i - j, flag);
        ItemStack itemstack = this.player.containerMenu.getCarried();
        j += ContainerHelper.clearOrCountMatchingItems(itemstack, predicate, i - j, flag);
        if (itemstack.isEmpty()) {
            this.player.containerMenu.setCarried(ItemStack.EMPTY);
        }

        return j;
    }

    private int addResource(ItemStack itemstack) {
        int i = this.getSlotWithRemainingSpace(itemstack);
        if (i == -1) {
            i = this.getFreeSlot();
        }

        return i == -1 ? itemstack.getCount() : this.addResource(i, itemstack);
    }

    private int addResource(int i, ItemStack itemstack) {
        Item item = itemstack.getItem();
        int j = itemstack.getCount();
        ItemStack localItemStack = this.getRawItem(i);
        if (localItemStack.isEmpty()) {
            localItemStack = new ItemStack(item, 0);
            if (itemstack.hasTag()) {
                // hasTag ensures tag not null
                //noinspection ConstantConditions
                localItemStack.setTag(itemstack.getTag().copy());
            }

            this.setRawItem(i, localItemStack);
        }

        int k = Math.min(j, localItemStack.getMaxStackSize() - localItemStack.getCount());

        if (k > this.getMaxStackSize() - localItemStack.getCount()) {
            k = this.getMaxStackSize() - localItemStack.getCount();
        }

        if (k != 0) {
            j -= k;
            localItemStack.grow(k);
            localItemStack.setPopTime(5);
        }

        return j;
    }

    @Override
    public int getSlotWithRemainingSpace(ItemStack itemstack) {
        if (this.hasRemainingSpaceForItem(this.getRawItem(this.selected), itemstack)) {
            return this.selected;
        } else if (this.hasRemainingSpaceForItem(this.getRawItem(40), itemstack)) {
            return 40;
        } else {
            for(int i = 0; i < this.items.size(); ++i) {
                if (this.hasRemainingSpaceForItem(this.items.get(i), itemstack)) {
                    return i;
                }
            }

            return -1;
        }
    }

    @Override
    public void tick() {
        for (NonNullList<ItemStack> compartment : this.compartments) {
            for (int i = 0; i < compartment.size(); ++i) {
                if (!compartment.get(i).isEmpty()) {
                    compartment.get(i).inventoryTick(this.player.level(), this.player, i, this.selected == i);
                }
            }
        }

    }

    @Override
    public boolean add(ItemStack itemStack) {
        return this.add(-1, itemStack);
    }

    @Override
    public boolean add(int i, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        } else {
            try {
                if (itemStack.isDamaged()) {
                    if (i == -1) {
                        i = this.getFreeSlot();
                    }

                    if (i >= 0) {
                        this.items.set(i, itemStack.copy());
                        this.items.get(i).setPopTime(5);
                        itemStack.setCount(0);
                        return true;
                    } else if (this.player.getAbilities().instabuild) {
                        itemStack.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int j;
                    do {
                        j = itemStack.getCount();
                        if (i == -1) {
                            itemStack.setCount(this.addResource(itemStack));
                        } else {
                            itemStack.setCount(this.addResource(i, itemStack));
                        }
                    } while(!itemStack.isEmpty() && itemStack.getCount() < j);

                    if (itemStack.getCount() == j && this.player.getAbilities().instabuild) {
                        itemStack.setCount(0);
                        return true;
                    } else {
                        return itemStack.getCount() < j;
                    }
                }
            } catch (Throwable var6) {
                CrashReport crashReport = CrashReport.forThrowable(var6, "Adding item to inventory");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Item being added");
                crashReportCategory.setDetail("Item ID", Item.getId(itemStack.getItem()));
                crashReportCategory.setDetail("Item data", itemStack.getDamageValue());
                crashReportCategory.setDetail("Item name", () -> itemStack.getHoverName().getString());
                throw new ReportedException(crashReport);
            }
        }
    }

    @Override
    public void placeItemBackInInventory(ItemStack itemStack) {
        this.placeItemBackInInventory(itemStack, true);
    }

    @Override
    public void placeItemBackInInventory(ItemStack itemStack, boolean flag) {
        while(true) {
            if (!itemStack.isEmpty()) {
                int i = this.getSlotWithRemainingSpace(itemStack);
                if (i == -1) {
                    i = this.getFreeSlot();
                }

                if (i != -1) {
                    int j = itemStack.getMaxStackSize() - this.getRawItem(i).getCount();
                    if (this.add(i, itemStack.split(j)) && flag && this.player instanceof ServerPlayer) {
                        ((ServerPlayer)this.player).connection.send(new ClientboundContainerSetSlotPacket(-2, 0, i, this.getRawItem(i)));
                    }
                    continue;
                }

                this.player.drop(itemStack, false);
            }

            return;
        }
    }

    @Override
    public ItemStack removeItem(int rawIndex, final int j) {
        IndexedCompartment indexedCompartment = getIndexedContent(rawIndex);

        if (indexedCompartment.compartment() == null
                || indexedCompartment.compartment().get(indexedCompartment.index()).isEmpty()) {
            return ItemStack.EMPTY;
        }

        return ContainerHelper.removeItem(indexedCompartment.compartment(), indexedCompartment.index(), j);
    }

    @Override
    public void removeItem(ItemStack itemStack) {
        for (NonNullList<ItemStack> compartment : this.compartments) {
            for (int i = 0; i < compartment.size(); ++i) {
                if (compartment.get(i) == itemStack) {
                    compartment.set(i, ItemStack.EMPTY);
                    break;
                }
            }
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int rawIndex) {
        IndexedCompartment indexedCompartment = getIndexedContent(rawIndex);

        if (indexedCompartment.compartment() == null) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = indexedCompartment.compartment().set(indexedCompartment.index(), ItemStack.EMPTY);

        if (removed.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return removed;
    }

    @Override
    public void setItem(int rawIndex, final ItemStack itemStack) {
        IndexedCompartment indexedCompartment = getIndexedContent(rawIndex);

        if (indexedCompartment.compartment() == null) {
            this.player.drop(itemStack, true);
            return;
        }

        indexedCompartment.compartment().set(indexedCompartment.index(), itemStack);
    }

    @Override
    public float getDestroySpeed(BlockState blockState) {
        return this.items.get(this.selected).getDestroySpeed(blockState);
    }

    @Override
    public ListTag save(ListTag listTag) {
        for (int i = 0; i < this.items.size(); ++i) {
            if (!this.items.get(i).isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte)i);
                this.items.get(i).save(compoundTag);
                listTag.add(compoundTag);
            }
        }

        for (int i = 0; i < this.armor.size(); ++i) {
            if (!this.armor.get(i).isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte)(i + 100));
                this.armor.get(i).save(compoundTag);
                listTag.add(compoundTag);
            }
        }

        for (int i = 0; i < this.offhand.size(); ++i) {
            if (!this.offhand.get(i).isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte)(i + 150));
                this.offhand.get(i).save(compoundTag);
                listTag.add(compoundTag);
            }
        }

        return listTag;
    }

    @Override
    public void load(ListTag listTag) {
        this.items.clear();
        this.armor.clear();
        this.offhand.clear();

        for(int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            int j = compoundTag.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.of(compoundTag);
            if (!itemstack.isEmpty()) {
                if (j < this.items.size()) {
                    this.items.set(j, itemstack);
                } else if (j >= 100 && j < this.armor.size() + 100) {
                    this.armor.set(j - 100, itemstack);
                } else if (j >= 150 && j < this.offhand.size() + 150) {
                    this.offhand.set(j - 150, itemstack);
                }
            }
        }

    }

    @Override
    public int getContainerSize() {
        return 45;
    }

    @Override
    public boolean isEmpty() {
        return !contains(itemStack -> !itemStack.isEmpty());
    }

    @Override
    public ItemStack getItem(int rawIndex) {
        IndexedCompartment indexedCompartment = getIndexedContent(rawIndex);

        if (indexedCompartment.compartment() == null) {
            return ItemStack.EMPTY;
        }

        return indexedCompartment.compartment().get(indexedCompartment.index());
    }

    @Override
    public Component getName() {
        return this.player.getName();
    }

    @Override
    public ItemStack getArmor(int index) {
        return this.armor.get(index);
    }

    @Override
    public void hurtArmor(DamageSource damagesource, float damage, int[] armorIndices) {
        if (damage > 0.0F) {
            damage /= 4.0F;
            if (damage < 1.0F) {
                damage = 1.0F;
            }

            for (int index : armorIndices) {
                ItemStack itemstack = this.armor.get(index);
                if ((!damagesource.is(DamageTypeTags.IS_FIRE) || !itemstack.getItem().isFireResistant()) && itemstack.getItem() instanceof ArmorItem) {
                    itemstack.hurtAndBreak((int) damage, this.player, localPlayer -> localPlayer.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, index)));
                }
            }
        }
    }

    @Override
    public void dropAll() {
        for (NonNullList<ItemStack> compartment : this.compartments) {
            for (int i = 0; i < compartment.size(); ++i) {
                ItemStack itemstack = compartment.get(i);
                if (!itemstack.isEmpty()) {
                    this.player.drop(itemstack, true, false);
                    compartment.set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public int getTimesChanged() {
        return super.getTimesChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean contains(ItemStack itemstack) {
        return contains(itemStack -> itemStack.isEmpty() && itemStack.is(itemstack.getItem()));
    }

    @Override
    public boolean contains(TagKey<Item> tagKey) {

        return contains(itemStack -> !itemStack.isEmpty() && itemStack.is(tagKey));
    }

    @Override
    public void replaceWith(Inventory inventory) {
        Function<Integer, ItemStack> getter;

        if (inventory instanceof SpecialPlayerInventory specialPlayerInventory) {
            getter = specialPlayerInventory::getRawItem;
        } else {
            getter = inventory::getItem;
        }

        for(int i = 0; i < this.getContainerSize(); ++i) {
            this.setRawItem(i, getter.apply(i));
        }

        this.selected = inventory.selected;
    }

    @Override
    public void clearContent() {
        for (NonNullList<ItemStack> compartment : this.compartments) {
            compartment.clear();
        }
    }

    @Override
    public void fillStackedContents(StackedContents stackedContents) {
        for (ItemStack itemstack : this.items) {
            stackedContents.accountSimpleStack(itemstack);
        }
    }

    @Override
    public ItemStack removeFromSelected(boolean dropWholeStack) {
        ItemStack itemstack = this.getSelected();
        return itemstack.isEmpty() ? ItemStack.EMPTY : this.removeItem(this.selected, dropWholeStack ? itemstack.getCount() : 1);
    }

}
