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

package com.lishid.openinv;

import com.lishid.openinv.command.ContainerSettingCommand;
import com.lishid.openinv.command.OpenInvCommand;
import com.lishid.openinv.command.SearchContainerCommand;
import com.lishid.openinv.command.SearchEnchantCommand;
import com.lishid.openinv.command.SearchInvCommand;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.listener.ContainerListener;
import com.lishid.openinv.listener.ToggleListener;
import com.lishid.openinv.util.AccessEqualMode;
import com.lishid.openinv.util.InternalAccessor;
import com.lishid.openinv.util.InventoryManager;
import com.lishid.openinv.util.Permissions;
import com.lishid.openinv.util.PlayerLoader;
import com.lishid.openinv.util.config.Config;
import com.lishid.openinv.util.config.ConfigUpdater;
import com.lishid.openinv.util.lang.LangMigrator;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.setting.PlayerToggle;
import com.lishid.openinv.util.setting.PlayerToggles;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * The main class for OpenInv.
 */
public class OpenInv extends JavaPlugin implements IOpenInv {

    private InternalAccessor accessor;
    private Config config;
    private InventoryManager inventoryManager;
    private LanguageManager languageManager;
    private PlayerLoader playerLoader;
    private boolean isSpigot = false;

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        config.reload(getConfig());
        languageManager.reload();
        if (accessor != null && accessor.isSupported()) {
            accessor.reload(getConfig());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!isSpigot || !this.accessor.isSupported()) {
            this.sendVersionError(sender::sendMessage);
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        inventoryManager.evictAll();
    }

    @Override
    public void onEnable() {
        // Save default configuration if not present.
        this.saveDefaultConfig();

        // Migrate locale files to a subfolder.
        Path dataFolder = getDataFolder().toPath();
        new LangMigrator(dataFolder, dataFolder.resolve("locale"), getLogger()).migrate();

        // Set up configurable features. Note that #reloadConfig is called on the first call to #getConfig!
        // Configuration values should not be accessed until after all of these have been set up.
        config = new Config();
        languageManager = new LanguageManager(this, "en");
        accessor = new InternalAccessor(getLogger(), languageManager);

        // Perform initial config load.
        reloadConfig();

        inventoryManager = new InventoryManager(this, config, accessor);
        playerLoader = new PlayerLoader(this, config, inventoryManager, accessor, getLogger());

        try {
            Class.forName("org.bukkit.entity.Player$Spigot");
            isSpigot = true;
        } catch (ClassNotFoundException e) {
            isSpigot = false;
        }

        // Version check
        if (isSpigot && this.accessor.isSupported()) {
            reloadConfig();

            // Update existing configuration. May require internal access.
            new ConfigUpdater(this).checkForUpdates();

            // Register relevant event listeners.
            registerEvents();

            // Register commands to their executors.
            registerCommands();

        } else {
            this.sendVersionError(this.getLogger()::warning);
        }

    }

    private void registerEvents() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(playerLoader, this);
        pluginManager.registerEvents(inventoryManager, this);
        pluginManager.registerEvents(new ContainerListener(accessor, languageManager), this);
        pluginManager.registerEvents(new ToggleListener(), this);
    }

    private void registerCommands() {
        this.setCommandExecutor(new OpenInvCommand(this, config, inventoryManager, languageManager, playerLoader), "openinv", "openender");
        this.setCommandExecutor(new SearchContainerCommand(this, languageManager), "searchcontainer");
        this.setCommandExecutor(new SearchInvCommand(languageManager), "searchinv", "searchender");
        this.setCommandExecutor(new SearchEnchantCommand(languageManager), "searchenchant");

        ContainerSettingCommand settingCommand = new ContainerSettingCommand(languageManager);
        for (PlayerToggle toggle : PlayerToggles.get()) {
            setCommandExecutor(settingCommand, toggle.getName().toLowerCase(Locale.ENGLISH));
        }
    }

    private void setCommandExecutor(@NotNull CommandExecutor executor, String @NotNull ... commands) {
        for (String commandName : commands) {
            PluginCommand command = this.getCommand(commandName);
            if (command != null) {
                command.setExecutor(executor);
            }
        }
    }

    private void sendVersionError(@NotNull Consumer<String> messageMethod) {
        if (!accessor.isSupported()) {
            messageMethod.accept("Your server version (" + accessor.getVersion() + ") is not supported.");
            messageMethod.accept("Please download the correct version of OpenInv here: " + accessor.getReleasesLink());

            // We check this property late so users can use jars that were remapped by Paper already.
            if (Boolean.getBoolean("paper.disable-plugin-rewriting")) {
                messageMethod.accept("OpenInv uses Spigot-mapped internals, but you have disabled plugin rewriting in Paper!");
                messageMethod.accept("Please set system property 'paper.disable-plugin-rewriting' to false.");
            }
        }
        if (!isSpigot) {
            messageMethod.accept("OpenInv requires that you use Spigot or a Spigot fork. Per the 1.14 update thread");
            messageMethod.accept("(https://www.spigotmc.org/threads/369724/ \"A Note on CraftBukkit\"), if you are");
            messageMethod.accept("encountering an inconsistency with vanilla that prevents you from using Spigot,");
            messageMethod.accept("that is considered a Spigot bug and should be reported as such.");
        }
    }

    @Override
    public boolean isSupportedVersion() {
        return this.accessor != null && this.accessor.isSupported();
    }

    @Override
    public boolean disableSaving() {
        return config.isSaveDisabled();
    }

    @Override
    public boolean disableOfflineAccess() {
        return config.isOfflineDisabled();
    }

    @Override
    public boolean noArgsOpensSelf() {
        return config.doesNoArgsOpenSelf();
    }

    @Override
    public @NotNull IAnySilentContainer getAnySilentContainer() {
        return this.accessor.getAnySilentContainer();
    }

    @Override
    public boolean getAnyContainerStatus(@NotNull final OfflinePlayer offline) {
        return PlayerToggles.any().is(offline.getUniqueId());
    }

    @Override
    public void setAnyContainerStatus(@NotNull final OfflinePlayer offline, final boolean status) {
        PlayerToggles.any().set(offline.getUniqueId(), status);
    }

    @Override
    public boolean getSilentContainerStatus(@NotNull final OfflinePlayer offline) {
        return PlayerToggles.silent().is(offline.getUniqueId());
    }

    @Override
    public void setSilentContainerStatus(@NotNull final OfflinePlayer offline, final boolean status) {
        PlayerToggles.silent().set(offline.getUniqueId(), status);
    }

    @Override
    public @NotNull ISpecialEnderChest getSpecialEnderChest(@NotNull final Player player, final boolean online) {
        return inventoryManager.getEnderChest(player);
    }

    @Override
    public @NotNull ISpecialPlayerInventory getSpecialInventory(@NotNull final Player player, final boolean online) {
        return inventoryManager.getInventory(player);
    }

    @Override
    public @Nullable InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory) {
        Permissions edit = null;
        HumanEntity target = inventory.getPlayer();
        boolean ownContainer = player.equals(target);
        if (inventory instanceof ISpecialPlayerInventory) {
            edit = ownContainer ? Permissions.INVENTORY_EDIT_SELF : Permissions.INVENTORY_EDIT_OTHER;
        } else if (inventory instanceof ISpecialEnderChest) {
            edit = ownContainer ? Permissions.ENDERCHEST_EDIT_SELF : Permissions.ENDERCHEST_OPEN_OTHER;
        }

        boolean viewOnly = edit != null && !edit.hasPermission(player);

        if (ownContainer || viewOnly && config.getAccessEqualMode() != AccessEqualMode.DENY) {
            this.accessor.openInventory(player, inventory, viewOnly);
        }

        for (int level = 4; level > 0; --level) {
            String permission = "openinv.access.level." + level;
            // If the target doesn't have this access level...
            if (!target.hasPermission(permission)) {
                // If the viewer does have the access level, all good.
                if (player.hasPermission(permission)) {
                    break;
                }
                // Otherwise check next access level.
                continue;
            }

            // If the viewer doesn't have an equal access level or equal access is a denial, deny.
            if (!player.hasPermission(permission) || config.getAccessEqualMode() == AccessEqualMode.DENY) {
                return null;
            }

            // Since this is a tie, setting decides view state.
            if (config.getAccessEqualMode() == AccessEqualMode.VIEW) {
                viewOnly = true;
            }
            break;
        }

        return this.accessor.openInventory(player, inventory, viewOnly);
    }

    @Override
    public boolean isPlayerLoaded(@NotNull UUID playerUuid) {
        return inventoryManager.getLoadedPlayer(playerUuid) != null;
    }

    @Override
    public @Nullable Player loadPlayer(@NotNull final OfflinePlayer offline) {
        return playerLoader.load(offline);
    }

    @Override
    public @Nullable OfflinePlayer matchPlayer(@NotNull String name) {
        return playerLoader.match(name);
    }

    @Override
    public void unload(@NotNull final OfflinePlayer offline) {
        inventoryManager.unload(offline.getUniqueId());
    }

}
