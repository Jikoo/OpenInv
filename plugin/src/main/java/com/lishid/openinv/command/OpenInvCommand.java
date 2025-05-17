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
import com.lishid.openinv.util.InventoryManager;
import com.lishid.openinv.util.Permissions;
import com.lishid.openinv.util.PlayerLoader;
import com.lishid.openinv.util.config.Config;
import com.lishid.openinv.util.lang.LanguageManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.WeakHashMap;
import java.util.logging.Level;

public class OpenInvCommand extends PlayerLookupCommand {

    private final @NotNull InventoryManager manager;
    private final Map<Player, String> openInvHistory = new WeakHashMap<>();
    private final Map<Player, String> openEnderHistory = new WeakHashMap<>();

    public OpenInvCommand(
            @NotNull OpenInv plugin,
            @NotNull Config config,
            @NotNull InventoryManager manager,
            @NotNull LanguageManager lang,
            @NotNull PlayerLoader playerLoader
    ) {
        super(plugin, lang, config, playerLoader);
        this.manager = manager;
    }

    @Override
    protected boolean isAccessInventory(@NotNull Command command) {
        return command.getName().equals("openinv");
    }

    @Override
    protected @Nullable String getTargetIdentifer(
            @NotNull CommandSender sender,
            @Nullable String argument,
            boolean accessInv
    ) {
        // /openinv help
        if (accessInv && argument != null && (argument.equalsIgnoreCase("help") || argument.equals("?"))) {
            this.showHelp(sender);
            return null;
        }

        // Command is player-only.
        if (!(sender instanceof Player player)) {
            lang.sendMessage(sender, "messages.error.consoleUnsupported");
            return null;
        }

        // Use fallthrough for no name provided.
        if (argument == null) {
            if (config.doesNoArgsOpenSelf()) {
                return player.getUniqueId().toString();
            }
            return (accessInv ? this.openInvHistory : this.openEnderHistory)
                .computeIfAbsent(player, localPlayer -> localPlayer.getUniqueId().toString());
        }

        if (!config.doesNoArgsOpenSelf()) {
            // History management
            (accessInv ? this.openInvHistory : this.openEnderHistory).put(player, argument);
        }

        return argument;
    }

    private void showHelp(@NotNull CommandSender sender) {
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

    @Override
    protected @Nullable OfflinePlayer getTarget(@NotNull String identifier) {
        return playerLoader.match(identifier);
    }

    @Override
    protected boolean deniedCommand(@NotNull CommandSender sender, @NotNull Player onlineTarget, boolean accessInv) {
        if (onlineTarget.equals(sender)) {
            // Permission for opening own inventory.
            if (!(accessInv ? Permissions.INVENTORY_OPEN_SELF : Permissions.ENDERCHEST_OPEN_SELF).hasPermission(sender)) {
                lang.sendMessage(sender, "messages.error.permissionOpenSelf");
                return true;

            }
        } else {
            // Permission for opening others' inventories.
            if (!(accessInv ? Permissions.INVENTORY_OPEN_OTHER : Permissions.ENDERCHEST_OPEN_OTHER).hasPermission(sender)) {
                lang.sendMessage(sender, "messages.error.permissionOpenOther");
                return true;
            }
        }

        return false;
    }

    @Override
    protected void handle(
            @NotNull CommandSender sender,
            @NotNull Player target,
            boolean accessInv,
            @NotNull String @NotNull [] args
    ) {
        Player player = (Player) sender;
        if (!config.doesNoArgsOpenSelf()) {
            // Record the target
            (accessInv ? this.openInvHistory : this.openEnderHistory).put(player, target.getUniqueId().toString());
        }

        // Create the inventory
        final ISpecialInventory inv;
        try {
            inv = accessInv ? manager.getInventory(target) : manager.getEnderChest(target);
        } catch (Exception e) {
            lang.sendMessage(player, "messages.error.commandException");
            plugin.getLogger().log(Level.WARNING, "Unable to create ISpecialInventory", e);
            return;
        }

        // Open the inventory
        plugin.openInventory(player, inv);
    }

}
