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

  /// Permission to open one's own inventory.
  INVENTORY_OPEN_SELF("inventory.open.self"),
  /// Permission to open someone else's inventory.
  INVENTORY_OPEN_OTHER("inventory.open.other"),
  /**
   * Permission to edit one's own inventory.
   *
   * <p>Note that this does not guarantee that the user has access to open the inventory!
   * Be sure to check {@link #INVENTORY_OPEN_SELF} first.</p>
   */
  INVENTORY_EDIT_SELF("inventory.edit.self"),
  /**
   * Permission to edit someone else's inventory.
   *
   * <p>Note that this does not guarantee that the user has access to open the inventory!
   * Be sure to check {@link #INVENTORY_OPEN_OTHER} first.</p>
   */
  INVENTORY_EDIT_OTHER("inventory.edit.other"),
  /// Permission to insert any item into the head slot.
  INVENTORY_SLOT_HEAD_ANY("inventory.slot.head.any"),
  /// Permission to insert any item into the chest slot.
  INVENTORY_SLOT_CHEST_ANY("inventory.slot.chest.any"),
  /// Permission to insert any item into the legs slot.
  INVENTORY_SLOT_LEGS_ANY("inventory.slot.legs.any"),
  /// Permission to insert any item into the feet slot.
  INVENTORY_SLOT_FEET_ANY("inventory.slot.feet.any"),
  /// Permission to drop items as the player via the drop slot.
  INVENTORY_SLOT_DROP("inventory.slot.drop"),

  /// Permission to open one's own ender chest.
  ENDERCHEST_OPEN_SELF("enderchest.open.self"),
  /// Permission to open someone else's ender chest.
  ENDERCHEST_OPEN_OTHER("enderchest.open.other"),
  /**
   * Permission to edit one's own ender chest.
   *
   * <p>Note that this does not guarantee that the user has access to open the inventory!
   * Be sure to check {@link #ENDERCHEST_OPEN_SELF} first.</p>
   */
  ENDERCHEST_EDIT_SELF("enderchest.edit.self"),
  /**
   * Permission to edit someone else's ender chest.
   *
   * <p>Note that this does not guarantee that the user has access to open the inventory!
   * Be sure to check {@link #ENDERCHEST_OPEN_OTHER} first.</p>
   */
  ENDERCHEST_EDIT_OTHER("enderchest.edit.other"),

  /// Permission to clear one's own inventory.
  CLEAR_SELF("clear.self"),
  /// Permission to clear someone else's inventory.
  CLEAR_OTHER("clear.other"),

  /// Permission to view inventories and ender chests from other worlds.
  ACCESS_CROSSWORLD("access.crossworld"),
  /// Permission to access offline players' inventories and ender chests.
  ACCESS_OFFLINE("access.offline"),
  /// Permission to access online players' inventories and ender chests.
  ACCESS_ONLINE("access.online"),
  /// Permission granting the ability to edit players with the same access level.
  ACCESS_EQUAL_EDIT("access.equal.edit"),
  /// Permission to view, but not edit, players with the same access level.
  ACCESS_EQUAL_VIEW("access.equal.view"),
  /// Permission to deny access to players with the same access level.
  ACCESS_EQUAL_DENY("access.equal.deny"),

  /// Permission to perform inventory interaction while in spectator mode.
  SPECTATE_CLICK("spectate.click"),

  /// Permission to use the AnyContainer feature.
  CONTAINER_ANY("container.any"),
  /// Permission to use the AnyContainer feature but not the command to toggle it.
  CONTAINER_ANY_USE("container.any.use"),
  /// Permission to use the SilentContainer feature.
  CONTAINER_SILENT("container.silent"),
  /// Permission to use the SilentContainer feature but not the command to toggle it.
  CONTAINER_SILENT_USE("container.silent.use"),
  /// Permission to search inventories and ender chests.
  SEARCH_INVENTORY("search.inventory"),
  /// Permission to search containers.
  SEARCH_CONTAINER("search.container");

  private final String permission;

  Permissions(String permission) {
    this.permission = "openinv." + permission;
  }

  /**
   * Check if a {@link Permissible} has this permission.
   *
   * @param permissible the Permissible
   * @return true if the permission is granted
   */
  public boolean hasPermission(@NotNull Permissible permissible) {
    return permissible.hasPermission(permission);
  }

}
