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

package com.lishid.openinv.command;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.util.AccessEqualMode;
import com.lishid.openinv.util.InventoryManager;
import com.lishid.openinv.util.Permissions;
import com.lishid.openinv.util.PlayerLoader;
import com.lishid.openinv.util.TabCompleter;
import com.lishid.openinv.util.config.Config;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.lang.Replacement;
import me.nahu.scheduler.wrapper.runnable.WrappedRunnable;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;

public class ClearInvCommand implements TabExecutor {

    private final @NotNull OpenInv plugin;
    private final @NotNull Config config;
    private final @NotNull InventoryManager manager;
    private final @NotNull LanguageManager lang;
    private final @NotNull PlayerLoader playerLoader;

    public ClearInvCommand(
            @NotNull OpenInv plugin,
            @NotNull Config config,
            @NotNull InventoryManager manager,
            @NotNull LanguageManager lang,
            @NotNull PlayerLoader playerLoader
    ) {
        this.plugin = plugin;
        this.config = config;
        this.manager = manager;
        this.lang = lang;
        this.playerLoader = playerLoader;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length < 1) {
            //must specify player name
            return false;
        }

        final String targetName = args[0];

        //absolute mess copy-pasted from `OpenInvCommand`
        new WrappedRunnable() {
            @Override
            public void run() {
                OfflinePlayer offlinePlayer = playerLoader.match(targetName);
                if (offlinePlayer == null || (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline())) {
                    lang.sendMessage(sender, "messages.error.invalidPlayer");
                    return;
                }

                new WrappedRunnable() {
                    @Override
                    public void run() {
                        clearInventory(sender, offlinePlayer);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }

    private void clearInventory(final CommandSender sender, OfflinePlayer target) {
        Player onlineTarget;
        boolean online = target.isOnline();

        if (online) {
            if (Permissions.ACCESS_ONLINE.hasPermission(sender)) {
                onlineTarget = target.getPlayer();
            } else {
                lang.sendMessage(sender, "messages.error.permissionPlayerOnline");
                return;
            }
        } else {
            if (!config.isOfflineDisabled() && Permissions.ACCESS_OFFLINE.hasPermission(sender)) {
                // Try loading the player's data
                onlineTarget = playerLoader.load(target);
            } else {
                lang.sendMessage(sender, "messages.error.permissionPlayerOffline");
                return;
            }
        }

        if (onlineTarget == null) {
            lang.sendMessage(sender, "messages.error.invalidPlayer");
            return;
        }

        // Protected check
        for (int level = 4; level > 0; --level) {
            String permission = "openinv.access.level." + level;
            if (onlineTarget.hasPermission(permission)
                    && (!sender.hasPermission(permission) || config.getAccessEqualMode() == AccessEqualMode.DENY)) {
                lang.sendMessage(
                        sender,
                        "messages.error.permissionExempt",
                        new Replacement("%target%", onlineTarget.getDisplayName()));
                return;
            }
        }

        // Crossworld check
        if (sender instanceof Player senderPlayer && !Permissions.ACCESS_CROSSWORLD.hasPermission(sender)
                && !onlineTarget.getWorld().equals(senderPlayer.getWorld())) {
            lang.sendMessage(
                    sender,
                    "messages.error.permissionCrossWorld",
                    new Replacement("%target%", onlineTarget.getDisplayName()));
            return;
        }

        // Create the inventory
        final ISpecialInventory inv;
        try {
            inv = manager.getInventory(onlineTarget);
        } catch (Exception e) {
            lang.sendMessage(sender, "messages.error.commandException");
            plugin.getLogger().log(Level.WARNING, "Unable to create ISpecialInventory", e);
            return;
        }

        // Clear the inventory
        inv.getBukkitInventory().clear();
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (!command.testPermissionSilent(sender) || args.length != 1) {
            return Collections.emptyList();
        }
        return TabCompleter.completeOnlinePlayer(sender, args[0]);
    }
}