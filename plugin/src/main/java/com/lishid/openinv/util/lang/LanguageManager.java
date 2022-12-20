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

package com.lishid.openinv.util.lang;

import com.lishid.openinv.OpenInv;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple language manager supporting both custom and bundled languages.
 *
 * @author Jikoo
 */
public class LanguageManager {

    private final OpenInv plugin;
    private final String defaultLocale;
    private final Map<String, YamlConfiguration> locales;

    public LanguageManager(@NotNull OpenInv plugin, @NotNull String defaultLocale) {
        this.plugin = plugin;
        this.defaultLocale = defaultLocale;
        this.locales = new HashMap<>();
        getOrLoadLocale(defaultLocale);
    }

    private @NotNull YamlConfiguration getOrLoadLocale(@NotNull String locale) {
        YamlConfiguration loaded = locales.get(locale);
        if (loaded != null) {
            return loaded;
        }

        InputStream resourceStream = plugin.getResource("locale/" + locale + ".yml");
        YamlConfiguration localeConfigDefaults;
        if (resourceStream == null) {
            localeConfigDefaults = new YamlConfiguration();
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream))) {
                localeConfigDefaults = YamlConfiguration.loadConfiguration(reader);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "[LanguageManager] Unable to load resource " + locale + ".yml", e);
                localeConfigDefaults = new YamlConfiguration();
            }
        }

        File file = new File(plugin.getDataFolder(), locale + ".yml");
        YamlConfiguration localeConfig;

        if (!file.exists()) {
            localeConfig = localeConfigDefaults;
            try {
                localeConfigDefaults.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "[LanguageManager] Unable to save resource " + locale + ".yml", e);
            }
        } else {
            localeConfig = YamlConfiguration.loadConfiguration(file);

            // Add new language keys
            List<String> newKeys = new ArrayList<>();
            for (String key : localeConfigDefaults.getKeys(true)) {
                if (localeConfigDefaults.isConfigurationSection(key)) {
                    continue;
                }

                if (localeConfig.isSet(key)) {
                    continue;
                }

                localeConfig.set(key, localeConfigDefaults.get(key));
                newKeys.add(key);
            }

            if (!newKeys.isEmpty()) {
                plugin.getLogger().info("[LanguageManager] Added new language keys: " + String.join(", ", newKeys));
                try {
                    localeConfig.save(file);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "[LanguageManager] Unable to save resource " + locale + ".yml", e);
                }
            }
        }

        if (!locale.equals(defaultLocale)) {
            localeConfigDefaults = locales.get(defaultLocale);

            // Check for missing keys
            List<String> newKeys = new ArrayList<>();
            for (String key : localeConfigDefaults.getKeys(true)) {
                if (localeConfigDefaults.isConfigurationSection(key)) {
                    continue;
                }

                if (localeConfig.isSet(key)) {
                    continue;
                }

                newKeys.add(key);
            }

            if (!newKeys.isEmpty()) {
                plugin.getLogger().info("[LanguageManager] Missing translations from " + locale + ".yml: " + String.join(", ", newKeys));
            }

            // Fall through to default locale
            localeConfig.setDefaults(localeConfigDefaults);
        }

        locales.put(locale, localeConfig);
        return localeConfig;
    }

    public @Nullable String getValue(@NotNull String key, @Nullable String locale) {
        String value = getOrLoadLocale(locale == null ? defaultLocale : locale.toLowerCase()).getString(key);
        if (value == null || value.isEmpty()) {
            return null;
        }

        value = ChatColor.translateAlternateColorCodes('&', value);

        return value;
    }

    public @Nullable String getValue(@NotNull String key, @Nullable String locale, Replacement @NotNull ... replacements) {
        String value = getValue(key, locale);

        if (value == null) {
            return null;
        }

        for (Replacement replacement : replacements) {
            value = value.replace(replacement.placeholder(), replacement.value());
        }

        return value;
    }

}
