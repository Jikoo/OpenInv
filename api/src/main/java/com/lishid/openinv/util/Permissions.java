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

package com.lishid.openinv.util;

import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

/**
 * An enum containing all permissions directly checked by OpenInv.
 *
 * <p>Note that this is not an exhaustive list! This does not contain
 * all permissions managed by Bukkit, largely parent nodes.</p>
 */
public enum Permissions {

  INVENTORY_OPEN_SELF("inventory.open.self"),
  INVENTORY_OPEN_OTHER("inventory.open.other"),
  INVENTORY_EDIT_SELF("inventory.edit.self"),
  INVENTORY_EDIT_OTHER("inventory.edit.other"),
  INVENTORY_SLOT_HEAD_ANY("inventory.slot.head.any"),
  INVENTORY_SLOT_CHEST_ANY("inventory.slot.chest.any"),
  INVENTORY_SLOT_LEGS_ANY("inventory.slot.legs.any"),
  INVENTORY_SLOT_FEET_ANY("inventory.slot.feet.any"),
  INVENTORY_SLOT_DROP("inventory.slot.drop"),

  ENDERCHEST_OPEN_SELF("enderchest.open.self"),
  ENDERCHEST_OPEN_OTHER("enderchest.open.other"),
  ENDERCHEST_EDIT_SELF("enderchest.edit.self"),
  ENDERCHEST_EDIT_OTHER("enderchest.edit.other"),

  CLEAR_SELF("clear.self"),
  CLEAR_OTHER("clear.other"),

  ACCESS_CROSSWORLD("access.crossworld"),
  ACCESS_OFFLINE("access.offline"),
  ACCESS_ONLINE("access.online"),
  ACCESS_EQUAL_EDIT("access.equal.edit"),
  ACCESS_EQUAL_VIEW("access.equal.view"),
  ACCESS_EQUAL_DENY("access.equal.deny"),

  SPECTATE_CLICK("spectate.click"),

  CONTAINER_ANY("container.any"),
  CONTAINER_ANY_USE("container.any.use"),
  CONTAINER_SILENT("container.silent"),
  CONTAINER_SILENT_USE("container.silent.use"),
  SEARCH_INVENTORY("search.inventory"),
  SEARCH_CONTAINER("search.container");

  private final String permission;

  Permissions(String permission) {
    this.permission = "openinv." + permission;
  }

  public boolean hasPermission(@NotNull Permissible permissible) {
    return permissible.hasPermission(permission);
  }

  public boolean hasPermission(@NotNull Permissible permissible, @NotNull Permissions parent) {
    if (permissible.hasPermission(permission)) return true;
    if (permissible.isPermissionSet(permission) && !permissible.hasPermission(permission)) return false;
    return permissible.hasPermission(parent.permission);
  }
}
