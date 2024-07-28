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

package com.lishid.openinv.listener;

import com.google.errorprone.annotations.Keep;
import com.lishid.openinv.internal.ViewOnly;
import com.lishid.openinv.util.InternalAccessor;
import com.lishid.openinv.util.Permissions;
import com.lishid.openinv.util.setting.PlayerToggles;
import com.lishid.openinv.util.lang.LanguageManager;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A listener managing AnyContainer, SilentContainer, and more.
 */
public class ContainerListener implements Listener {

  private final @NotNull InternalAccessor accessor;
  private final @NotNull LanguageManager lang;

  public ContainerListener(@NotNull InternalAccessor accessor, @NotNull LanguageManager lang) {
    this.accessor = accessor;
    this.lang = lang;
  }

  @Keep
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  private void onPlayerInteract(@NotNull PlayerInteractEvent event) {
    // Ignore events from other plugins.
    if (!PlayerInteractEvent.class.equals(event.getClass())) {
      return;
    }

    if (event.getAction() != Action.RIGHT_CLICK_BLOCK
        || event.getPlayer().isSneaking()
        || event.useInteractedBlock() == Result.DENY
        || event.getClickedBlock() == null
        || !accessor.getAnySilentContainer().isAnySilentContainer(event.getClickedBlock())) {
      return;
    }

    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    boolean any = Permissions.CONTAINER_ANY.hasPermission(player) && PlayerToggles.any().is(playerId);
    boolean needsAny = accessor.getAnySilentContainer().isAnyContainerNeeded(event.getClickedBlock());

    if (!any && needsAny) {
      return;
    }

    boolean silent = Permissions.CONTAINER_SILENT.hasPermission(player) && PlayerToggles.silent().is(playerId);

    // If anycontainer or silentcontainer is active
    if (any || silent) {
      if (accessor.getAnySilentContainer().activateContainer(player, silent, event.getClickedBlock())) {
        if (silent && needsAny) {
          lang.sendSystemMessage(player, "messages.info.containerBlockedSilent");
        } else if (needsAny) {
          lang.sendSystemMessage(player, "messages.info.containerBlocked");
        } else if (silent) {
          lang.sendSystemMessage(player, "messages.info.containerSilent");
        }
      }
      event.setCancelled(true);
    }
  }

  @Keep
  @EventHandler
  private void onInventoryClose(@NotNull final InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player player)) {
      return;
    }

    InventoryHolder holder = event.getInventory().getHolder();
    if (PlayerToggles.silent().is(player.getUniqueId())
        && holder != null
        && this.accessor.getAnySilentContainer().isAnySilentContainer(holder)) {
      this.accessor.getAnySilentContainer().deactivateContainer(player);
    }
  }

  @Keep
  @EventHandler(priority = EventPriority.LOWEST)
  private void onInventoryClick(@NotNull final InventoryClickEvent event) {
    handleInventoryInteract(event);
  }

  @Keep
  @EventHandler(priority = EventPriority.LOWEST)
  private void onInventoryDrag(@NotNull final InventoryDragEvent event) {
    handleInventoryInteract(event);
  }

  private void handleInventoryInteract(@NotNull final InventoryInteractEvent event) {
    HumanEntity entity = event.getWhoClicked();

    // Un-cancel spectator interactions.
    if (entity.getGameMode() == GameMode.SPECTATOR && Permissions.SPECTATE_CLICK.hasPermission(entity)) {
      event.setCancelled(false);
    }

    if (event.isCancelled()) {
      return;
    }

    Inventory inventory = event.getView().getTopInventory();
    if (inventory instanceof ViewOnly) {
      event.setCancelled(true);
    }
  }

}
