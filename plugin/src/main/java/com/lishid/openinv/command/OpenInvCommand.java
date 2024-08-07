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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;

public class OpenInvCommand implements TabExecutor {

    private final @NotNull OpenInv plugin;
    private final @NotNull Config config;
    private final @NotNull InventoryManager manager;
    private final @NotNull LanguageManager lang;
    private final @NotNull PlayerLoader playerLoader;
    private final HashMap<Player, String> openInvHistory = new HashMap<>();
    private final HashMap<Player, String> openEnderHistory = new HashMap<>();

    public OpenInvCommand(
        @NotNull OpenInv plugin,
        @NotNull Config config,
        @NotNull InventoryManager manager,
        @NotNull LanguageManager lang,
        @NotNull PlayerLoader playerLoader) {
        this.plugin = plugin;
        this.config = config;
        this.manager = manager;
        this.lang = lang;
        this.playerLoader = playerLoader;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        boolean openInv = command.getName().equals("openinv");

        if (openInv && args.length > 0 && (args[0].equalsIgnoreCase("help") || args[0].equals("?"))) {
            this.showHelp(sender);
            return true;
        }

        if (!(sender instanceof Player player)) {
            lang.sendMessage(sender, "messages.error.consoleUnsupported");
            return true;
        }

        String noArgValue;
        if (config.doesNoArgsOpenSelf()) {
            noArgValue = player.getUniqueId().toString();
        } else {
            // History management
            noArgValue = (openInv ? this.openInvHistory : this.openEnderHistory).get(player);

            if (noArgValue == null || noArgValue.isEmpty()) {
                noArgValue = player.getUniqueId().toString();
                (openInv ? this.openInvHistory : this.openEnderHistory).put(player, noArgValue);
            }
        }

        final String name;

        if (args.length < 1) {
            name = noArgValue;
        } else {
            name = args[0];
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                final OfflinePlayer offlinePlayer = playerLoader.match(name);

                if (offlinePlayer == null || (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline())) {
                    lang.sendMessage(player, "messages.error.invalidPlayer");
                    return;
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline()) {
                            return;
                        }
                        OpenInvCommand.this.openInventory(player, offlinePlayer, openInv);
                    }
                }.runTask(OpenInvCommand.this.plugin);

            }
        }.runTaskAsynchronously(this.plugin);

        return true;
    }

    private void showHelp(final CommandSender sender) {
        // Get registered commands
        for (String commandName : plugin.getDescription().getCommands().keySet()) {
            PluginCommand command = plugin.getCommand(commandName);

            // Ensure command is successfully registered and sender can use it
            if (command == null  || !command.testPermissionSilent(sender)) {
                continue;
            }

            // Send usage
            sender.sendMessage(command.getUsage().replace("<command>", commandName));

            List<String> aliases = command.getAliases();
            if (!aliases.isEmpty()) {
                // Assemble alias list
                StringJoiner aliasJoiner = new StringJoiner(", ", "   (aliases: ", ")");
                for (String alias : aliases) {
                    aliasJoiner.add(alias);
                }

                // Send all aliases
                sender.sendMessage(aliasJoiner.toString());
            }

        }
    }

    private void openInventory(final Player player, final OfflinePlayer target, boolean openinv) {
        Player onlineTarget;
        boolean online = target.isOnline();

        if (!online) {
            if (!config.isOfflineDisabled() && Permissions.ACCESS_OFFLINE.hasPermission(player)) {
                // Try loading the player's data
                onlineTarget = playerLoader.load(target);
            } else {
                lang.sendMessage(player, "messages.error.permissionPlayerOffline");
                return;
            }
        } else {
            if (Permissions.ACCESS_ONLINE.hasPermission(player)) {
                onlineTarget = target.getPlayer();
            } else {
                lang.sendMessage(player, "messages.error.permissionPlayerOnline");
                return;
            }
        }

        if (onlineTarget == null) {
            lang.sendMessage(player, "messages.error.invalidPlayer");
            return;
        }

        // Permissions checks
        if (onlineTarget.equals(player)) {
            // Permission for opening own inventory.
            if (!(openinv ? Permissions.INVENTORY_OPEN_SELF : Permissions.ENDERCHEST_OPEN_SELF).hasPermission(player)) {
                lang.sendMessage(player, "messages.error.permissionOpenSelf");
                return;

            }
        } else {
            // Permission for opening others' inventories.
            if (!(openinv ? Permissions.INVENTORY_OPEN_OTHER : Permissions.ENDERCHEST_OPEN_OTHER).hasPermission(player)) {
                lang.sendMessage(player, "messages.error.permissionOpenOther");
                return;
            }

            // Protected check
            for (int level = 4; level > 0; --level) {
                String permission = "openinv.access.level." + level;
                if (onlineTarget.hasPermission(permission)
                        && (!player.hasPermission(permission) || config.getAccessEqualMode() == AccessEqualMode.DENY)) {
                    lang.sendMessage(
                        player,
                        "messages.error.permissionExempt",
                        new Replacement("%target%", onlineTarget.getDisplayName()));
                    return;
                }
            }

            // Crossworld check
            if (!Permissions.ACCESS_CROSSWORLD.hasPermission(player)
                    && !onlineTarget.getWorld().equals(player.getWorld())) {
                lang.sendMessage(
                        player,
                        "messages.error.permissionCrossWorld",
                        new Replacement("%target%", onlineTarget.getDisplayName()));
                return;
            }
        }

        if (!config.doesNoArgsOpenSelf()) {
            // Record the target
            (openinv ? this.openInvHistory : this.openEnderHistory).put(player, target.getUniqueId().toString());
        }

        // Create the inventory
        final ISpecialInventory inv;
        try {
            inv = openinv ? manager.getInventory(onlineTarget) : manager.getEnderChest(onlineTarget);
        } catch (Exception e) {
            lang.sendMessage(player, "messages.error.commandException");
            plugin.getLogger().log(Level.WARNING, "Unable to create ISpecialInventory", e);
            return;
        }

        // Open the inventory
        plugin.openInventory(player, inv);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.testPermissionSilent(sender) || args.length != 1) {
            return Collections.emptyList();
        }

        return TabCompleter.completeOnlinePlayer(sender, args[0]);
    }

}
