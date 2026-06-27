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

package com.lishid.openinv.util;

import com.github.jikoo.planarwrappers.util.version.BukkitVersions;
import com.github.jikoo.planarwrappers.util.version.Version;
import com.lishid.openinv.internal.Accessor;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.PlayerManager;
import com.lishid.openinv.util.lang.LanguageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class InternalAccessor {

  private static final boolean PAPER;

  static {
    boolean paper = false;
    try {
      Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
      paper = true;
    } catch (ClassNotFoundException ignored) {
      // Expect remapped server.
    }
    PAPER = paper;
  }

  private @Nullable Accessor internal;

  public InternalAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
    try {
      internal = getAccessor(logger, lang);

      if (internal != null) {
        InventoryAccess.setProvider(internal::get);
      }
    } catch (NoClassDefFoundError | Exception e) {
      internal = null;
      InventoryAccess.setProvider(null);
    }
  }

  private @Nullable Accessor getAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
    if (!PAPER) {
      if (BukkitVersions.MINECRAFT.equals(Version.of(26, 2))) {
        return new com.github.jikoo.openinv.internal.spigot26_2.InternalAccessor(logger, lang);
      }
      if (BukkitVersions.MINECRAFT.greaterThanOrEqual(Version.of(26, 1))
          && BukkitVersions.MINECRAFT.lessThanOrEqual(Version.of(26, 1, 2))) {
        // Load Spigot accessor.
        return new com.github.jikoo.openinv.internal.spigot26_1.InternalAccessor(logger, lang);
      }
      return null;
    }

    Version maxSupported = Version.of(26, 2);
    Version minSupported = Version.of(1, 21, 9);

    // Ensure version is in supported range.
    if (BukkitVersions.MINECRAFT.greaterThan(maxSupported) || BukkitVersions.MINECRAFT.lessThan(minSupported)) {
      return null;
    }

    // Paper or a Paper fork, can use Mojang-mapped internals.
    if (BukkitVersions.MINECRAFT.greaterThanOrEqual(Version.of(26, 2))) { // 26.2
      return new com.lishid.openinv.internal.paper26_2.InternalAccessor(logger, lang);
    }
    if (BukkitVersions.MINECRAFT.greaterThanOrEqual(Version.of(26, 1))) { // 26.1.1, 26.1.2
      return new com.lishid.openinv.internal.paper26_1.InternalAccessor(logger, lang);
    }
    if (BukkitVersions.MINECRAFT.equals(Version.of(1, 21, 11))) { // 1.21.11
      return new com.lishid.openinv.internal.paper1_21_11.InternalAccessor(logger, lang);
    }
    // 1.21.9, 1.21.10
    return new com.lishid.openinv.internal.paper1_21_10.InternalAccessor(logger, lang);
  }

  /**
   * Reload internal features.
   */
  public void reload(ConfigurationSection config) {
    if (internal != null) {
      internal.reload(config);
    }
  }

  /**
   * Gets the server implementation version.
   *
   * @return the version
   */
  public @NotNull String getVersion() {
    return BukkitVersions.MINECRAFT.toString();
  }

  /**
   * Checks if the server implementation is supported.
   *
   * @return true if initialized for a supported server version
   */
  public boolean isSupported() {
    return internal != null;
  }

  /**
   * Get the instance of the IAnySilentContainer implementation for the current server version.
   *
   * @return the IAnySilentContainer
   * @throws IllegalStateException if server version is unsupported
   */
  public @NotNull IAnySilentContainer getAnySilentContainer() {
    if (internal == null) {
      throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
    }
    return internal.getAnySilentContainer();
  }

  public @Nullable InventoryView openInventory(
      @NotNull Player player,
      @NotNull ISpecialInventory inventory,
      boolean viewOnly
  ) {
    if (internal == null) {
      throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
    }
    return internal.getPlayerManager().openInventory(player, inventory, viewOnly);
  }

  /**
   * Get the instance of the IPlayerDataManager implementation for the current server version.
   *
   * @return the IPlayerDataManager
   * @throws IllegalStateException if server version is unsupported
   */
  @NotNull PlayerManager getPlayerDataManager() {
    if (internal == null) {
      throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
    }
    return internal.getPlayerManager();
  }

  /**
   * Creates an instance of the ISpecialEnderChest implementation for the given Player.
   *
   * @param player the Player
   * @return the ISpecialEnderChest created
   */
  ISpecialEnderChest createEnderChest(final Player player) {
    if (internal == null) {
      throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
    }
    return internal.createEnderChest(player);
  }

  /**
   * Creates an instance of the ISpecialPlayerInventory implementation for the given Player.
   *
   * @param player the Player
   * @return the ISpecialPlayerInventory created
   */
  ISpecialPlayerInventory createInventory(final Player player) {
    if (internal == null) {
      throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
    }
    return internal.createPlayerInventory(player);
  }

}
