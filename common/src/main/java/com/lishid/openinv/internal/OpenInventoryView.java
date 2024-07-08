/*
 * Copyright (C) 2011-2022 lishid. All rights reserved.
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

package com.lishid.openinv.internal;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

public class OpenInventoryView extends InventoryView {

    private final @NotNull Player player;
    private final @NotNull ISpecialInventory inventory;
    private final @NotNull String originalTitle;
    private String title;

    public OpenInventoryView(
            @NotNull Player player,
            @NotNull ISpecialInventory inventory,
            @NotNull String originalTitle) {
        this.player = player;
        this.inventory = inventory;
        this.originalTitle = originalTitle;
    }

    @Override
    public @NotNull Inventory getTopInventory() {
        return inventory.getBukkitInventory();
    }

    @Override
    public @NotNull Inventory getBottomInventory() {
        return getPlayer().getInventory();
    }

    @Override
    public @NotNull HumanEntity getPlayer() {
        return player;
    }

    @Override
    public @NotNull InventoryType getType() {
        return inventory.getBukkitInventory().getType();
    }

    @Override
    public @NotNull String getTitle() {
        return title == null ? originalTitle : title;
    }

    @Override
    public @NotNull String getOriginalTitle() {
        return originalTitle;
    }

    @Override
    public void setTitle(@NotNull String title) {
        this.title = title;
    }

}
