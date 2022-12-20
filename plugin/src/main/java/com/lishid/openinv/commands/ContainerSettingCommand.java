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
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ContainerSettingCommand implements TabExecutor {

    private final OpenInv plugin;

    public ContainerSettingCommand(final OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.sendMessage(sender, "messages.error.consoleUnsupported");
            return true;
        }

        boolean any = command.getName().startsWith("any");
        Predicate<Player> getSetting = any ? plugin::getAnyContainerStatus : plugin::getSilentContainerStatus;
        BiConsumer<OfflinePlayer, Boolean> setSetting = any ? plugin::setAnyContainerStatus : plugin::setSilentContainerStatus;

        if (args.length > 0) {
            args[0] = args[0].toLowerCase();

            if (args[0].equals("on")) {
                setSetting.accept(player, true);
            } else if (args[0].equals("off")) {
                setSetting.accept(player, false);
            } else if (!args[0].equals("check")) {
                // Invalid argument, show usage.
                return false;
            }

        } else {
            setSetting.accept(player, !getSetting.test(player));
        }

        String onOff = plugin.getLocalizedMessage(player, getSetting.test(player) ? "messages.info.on" : "messages.info.off");
        if (onOff == null) {
            onOff = String.valueOf(getSetting.test(player));
        }

        plugin.sendMessage(
                sender,
                "messages.info.settingState",
                new Replacement("%setting%", any ? "AnyContainer" : "SilentContainer"),
                new Replacement("%state%", onOff));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.testPermissionSilent(sender) || args.length != 1) {
            return Collections.emptyList();
        }

        return TabCompleter.completeString(args[0], new String[] {"check", "on", "off"});
    }

}
