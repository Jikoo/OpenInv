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

package com.lishid.openinv.commands;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.util.TabCompleter;
import com.lishid.openinv.util.lang.Replacement;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class SearchInvCommand implements TabExecutor {

    private final OpenInv plugin;

    public SearchInvCommand(OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Material material = null;

        if (args.length >= 1) {
            material = Material.getMaterial(args[0].toUpperCase());
        }

        if (material == null) {
            plugin.sendMessage(
                    sender,
                    "messages.error.invalidMaterial",
                    new Replacement("%target%", args.length > 0 ? args[0] : "null"));
            return false;
        }

        int count = 1;

        if (args.length >= 2) {
            try {
                count = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                plugin.sendMessage(
                        sender,
                        "messages.error.invalidNumber",
                        new Replacement("%target%", args[1]));
                return false;
            }
        }

        StringBuilder players = new StringBuilder();
        boolean searchInv = command.getName().equals("searchinv");
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Inventory inventory = searchInv ? player.getInventory() : player.getEnderChest();
            if (inventory.contains(material, count)) {
                players.append(player.getName()).append(", ");
            }
        }

        // Matches found, delete trailing comma and space
        if (players.length() > 0) {
            players.delete(players.length() - 2, players.length());
        } else {
            plugin.sendMessage(
                    sender,
                    "messages.info.player.noMatches",
                    new Replacement("%target%", material.name()));
            return true;
        }

        plugin.sendMessage(
                sender,
                "messages.info.player.matches",
                new Replacement("%target%", material.name()),
                new Replacement("%detail%", players.toString()));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1 || args.length > 2 || !command.testPermissionSilent(sender)) {
            return Collections.emptyList();
        }

        String argument = args[args.length - 1];
        if (args.length == 1) {
            return TabCompleter.completeEnum(argument, Material.class);
        } else {
            return TabCompleter.completeInteger(argument);
        }
    }

}
