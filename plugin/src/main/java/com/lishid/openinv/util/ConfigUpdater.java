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

package com.lishid.openinv.util;

import com.lishid.openinv.OpenInv;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

public record ConfigUpdater(OpenInv plugin) {

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

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
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

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.saveConfig();
                plugin.getLogger().info("Configuration update complete!");
            });
        });
    }

    private void updateConfig5To6() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getConfig().set("settings.command.open.no-args-opens-self", false);
            plugin.getConfig().set("config-version", 6);
        });
    }

    private void updateConfig4To5() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getConfig().set("settings.disable-offline-access", false);
            plugin.getConfig().set("config-version", 5);
        });
    }

    private void updateConfig3To4() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getConfig().set("notify", null);
            plugin.getConfig().set("settings.locale", "en_US");
            plugin.getConfig().set("config-version", 4);
        });
    }

    private void updateConfig2To3() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getConfig().set("config-version", 3);
            plugin.getConfig().set("items.open-inv", null);
            plugin.getConfig().set("ItemOpenInv", null);
            plugin.getConfig().set("toggles.items.open-inv", null);
            plugin.getConfig().set("settings.disable-saving",
                    plugin.getConfig().getBoolean("DisableSaving", false));
            plugin.getConfig().set("DisableSaving", null);
        });
    }

    private void updateConfig1To2() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // Get the old config settings
            boolean notifySilentChest = plugin.getConfig().getBoolean("NotifySilentChest", true);
            boolean notifyAnyChest = plugin.getConfig().getBoolean("NotifyAnyChest", true);
            plugin.getConfig().set("ItemOpenInvItemID", null);
            plugin.getConfig().set("NotifySilentChest", null);
            plugin.getConfig().set("NotifyAnyChest", null);
            plugin.getConfig().set("config-version", 2);
            plugin.getConfig().set("notify.any-chest", notifyAnyChest);
            plugin.getConfig().set("notify.silent-chest", notifySilentChest);
        });

        updateToggles("AnyChest", "toggles.any-chest");
        updateToggles("SilentChest", "toggles.silent-chest");
    }

    private void updateToggles(final String sectionName, final String newSectionName) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(sectionName);
        // Ensure section exists
        if (section == null) {
            return;
        }

        Set<String> keys = section.getKeys(false);

        // Ensure section has content
        if (keys.isEmpty()) {
            return;
        }

        final Map<String, Boolean> toggles = new HashMap<>();

        for (String playerName : keys) {
            OfflinePlayer player = plugin.matchPlayer(playerName);
            if (player != null) {
                toggles.put(player.getUniqueId().toString(), section.getBoolean(playerName + ".toggle", false));
            }
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // Wipe old ConfigurationSection
            plugin.getConfig().set(sectionName, null);

            // Prepare new ConfigurationSection
            ConfigurationSection newSection = plugin.getConfig().getConfigurationSection(newSectionName);
            if (newSection == null) {
                newSection = plugin.getConfig().createSection(newSectionName);
            }
            // Set new values
            for (Map.Entry<String, Boolean> entry : toggles.entrySet()) {
                newSection.set(entry.getKey(), entry.getValue());
            }
        });
    }

}
