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

package com.lishid.openinv.util.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public record ConfigUpdater(@NotNull Plugin plugin) {

    public void checkForUpdates() {
        final int version = plugin.getConfig().getInt("config-version", 1);
        ConfigurationSection defaults = plugin.getConfig().getDefaults();
        if (defaults == null || version >= defaults.getInt("config-version")) {
            return;
        }

        plugin.getLogger().info("Configuration update found! Performing update...");

        // Backup the old config file
        try {
            plugin.getConfig().save(new File(plugin.getDataFolder(), "config_old.yml"));
            plugin.getLogger().info("Backed up config.yml to config_old.yml before updating.");
        } catch (IOException e) {
            plugin.getLogger().warning("Could not back up config.yml before updating!");
        }

        if (version < 2) {
            updateConfig1To2();
        }
        if (version < 3) {
            updateConfig2To3();
        }
        if (version < 4) {
            updateConfig3To4();
        }
        if (version < 5) {
            updateConfig4To5();
        }
        if (version < 6) {
            updateConfig5To6();
        }
        if (version < 7) {
            updateConfig6To7();
        }
        if (version < 8) {
            updateConfig7To8();
        }

        plugin.saveConfig();
        plugin.getLogger().info("Configuration update complete!");
    }

    private void updateConfig7To8() {
        FileConfiguration config = plugin.getConfig();
        config.set("settings.equal-access", "view");
        config.set("config-version", 8);
    }

    private void updateConfig6To7() {
        FileConfiguration config = plugin.getConfig();
        config.set("toggles", null);
        String consoleLocale = config.getString("settings.locale", "en");
        if (consoleLocale.isBlank() || consoleLocale.equalsIgnoreCase("en_us")) {
            consoleLocale = "en";
        }
        config.set("settings.console-locale", consoleLocale);
        config.set("settings.locale", null);
        config.set("config-version", 7);
    }

    private void updateConfig5To6() {
        FileConfiguration config = plugin.getConfig();
        config.set("settings.command.open.no-args-opens-self", false);
        config.set("settings.command.searchcontainer.max-radius", 10);
        config.set("config-version", 6);
    }

    private void updateConfig4To5() {
        FileConfiguration config = plugin.getConfig();
        config.set("settings.disable-offline-access", false);
        config.set("config-version", 5);
    }

    private void updateConfig3To4() {
        FileConfiguration config = plugin.getConfig();
        config.set("notify", null);
        config.set("config-version", 4);
    }

    private void updateConfig2To3() {
        FileConfiguration config = plugin.getConfig();
        config.set("items", null);
        config.set("ItemOpenInv", null);
        config.set("toggles", null);
        config.set("settings.disable-saving", config.getBoolean("DisableSaving", false));
        config.set("DisableSaving", null);
        config.set("config-version", 3);
    }

    private void updateConfig1To2() {
        FileConfiguration config = plugin.getConfig();
        config.set("ItemOpenInvItemID", null);
        config.set("NotifySilentChest", null);
        config.set("NotifyAnyChest", null);
        config.set("AnyChest", null);
        config.set("SilentChest", null);
        config.set("config-version", 2);
    }

}
