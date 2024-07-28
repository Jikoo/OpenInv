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

import com.lishid.openinv.event.OpenEvents;
import com.lishid.openinv.util.setting.PlayerToggle;
import com.lishid.openinv.util.TabCompleter;
import com.lishid.openinv.util.setting.PlayerToggles;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.lang.Replacement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ContainerSettingCommand implements TabExecutor {

    private final @NotNull LanguageManager lang;

    public ContainerSettingCommand(@NotNull LanguageManager lang) {
        this.lang = lang;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            lang.sendMessage(sender, "messages.error.consoleUnsupported");
            return true;
        }

        PlayerToggle toggle = PlayerToggles.get(command.getName());

        // Shouldn't be possible.
        if (toggle == null) {
            JavaPlugin.getProvidingPlugin(getClass()).getLogger().warning("Command /" + command.getName() + " registered with no corresponding toggle!");
            return false;
        }

        UUID playerId = player.getUniqueId();

        if (args.length > 0) {
            args[0] = args[0].toLowerCase(Locale.ENGLISH);

            if (args[0].equals("on")) {
                set(toggle, playerId, true);
            } else if (args[0].equals("off")) {
                set(toggle, playerId, false);
            } else if (!args[0].equals("check")) {
                // Invalid argument, show usage.
                return false;
            }

        } else {
            set(toggle, playerId, !toggle.is(playerId));
        }

        String onOff = lang.getLocalizedMessage(player, toggle.is(playerId) ? "messages.info.on" : "messages.info.off");
        if (onOff == null) {
            onOff = String.valueOf(toggle.is(playerId));
        }

        lang.sendMessage(
                sender,
                "messages.info.settingState",
                new Replacement("%setting%", toggle.getName()),
                new Replacement("%state%", onOff));

        return true;
    }

    private void set(@NotNull PlayerToggle toggle, @NotNull UUID uuid, boolean state) {
        if (toggle.set(uuid, state)) {
            OpenEvents.notifyPlayerToggle(toggle, uuid, state);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.testPermissionSilent(sender) || args.length != 1) {
            return Collections.emptyList();
        }

        return TabCompleter.completeString(args[0], new String[] {"check", "on", "off"});
    }

}
