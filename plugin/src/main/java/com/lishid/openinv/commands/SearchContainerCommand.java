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
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Command for searching containers in a radius of chunks.
 */
public class SearchContainerCommand implements TabExecutor {

    private final OpenInv plugin;

    public SearchContainerCommand(OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player senderPlayer)) {
            plugin.sendMessage(sender, "messages.error.consoleUnsupported");
            return true;
        }

        if (args.length < 1) {
            // Must supply material
            return false;
        }

        Material material = Material.getMaterial(args[0].toUpperCase());

        if (material == null) {
            plugin.sendMessage(
                    sender,
                    "messages.error.invalidMaterial",
                    new Replacement("%target%", args[0]));
            return false;
        }

        int radius = 5;

        if (args.length > 1) {
            try {
                radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                // Invalid radius supplied
                return false;
            }
        }

        // Clamp radius.
        int configMax = plugin.getConfig().getInt("settings.command.searchcontainer.max-radius", 10);
        radius = Math.max(0, Math.min(radius, configMax));

        World world = senderPlayer.getWorld();
        Chunk centerChunk = senderPlayer.getLocation().getChunk();
        StringBuilder locations = new StringBuilder();

        for (int dX = -radius; dX <= radius; ++dX) {
            for (int dZ = -radius; dZ <= radius; ++dZ) {
                if (!world.loadChunk(centerChunk.getX() + dX, centerChunk.getZ() + dZ, false)) {
                    continue;
                }
                Chunk chunk = world.getChunkAt(centerChunk.getX() + dX, centerChunk.getZ() + dZ);
                for (BlockState tileEntity : chunk.getTileEntities()) {
                    if (!(tileEntity instanceof InventoryHolder holder)) {
                        continue;
                    }
                    if (!holder.getInventory().contains(material)) {
                        continue;
                    }
                    locations.append(holder.getInventory().getType().name().toLowerCase()).append(" (")
                            .append(tileEntity.getX()).append(',').append(tileEntity.getY()).append(',')
                            .append(tileEntity.getZ()).append("), ");
                }
            }
        }

        // Matches found, delete trailing comma and space
        if (locations.length() > 0) {
            locations.delete(locations.length() - 2, locations.length());
        } else {
            plugin.sendMessage(
                    sender,
                    "messages.info.container.noMatches",
                    new Replacement("%target%", material.name()));
            return true;
        }

        plugin.sendMessage(
                sender,
                "messages.info.container.matches",
                new Replacement("%target%", material.name()),
                new Replacement("%detail%", locations.toString()));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
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
