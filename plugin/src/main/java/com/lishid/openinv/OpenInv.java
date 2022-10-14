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

import com.lishid.openinv.commands.ContainerSettingCommand;
import com.lishid.openinv.commands.OpenInvCommand;
import com.lishid.openinv.commands.SearchContainerCommand;
import com.lishid.openinv.commands.SearchEnchantCommand;
import com.lishid.openinv.commands.SearchInvCommand;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.ConfigUpdater;
import com.lishid.openinv.util.LanguageManager;
import com.lishid.openinv.util.Permissions;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Open other player's inventory
 *
 * @author lishid
 */
public class OpenInv extends JavaPlugin implements IOpenInv {

    private final Map<UUID, ISpecialPlayerInventory> inventories = new ConcurrentHashMap<>();
    private final Map<UUID, ISpecialEnderChest> enderChests = new ConcurrentHashMap<>();

    private InternalAccessor accessor;
    private LanguageManager languageManager;
    private boolean isSpigot = false;
    private OfflineHandler offlineHandler;

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.offlineHandler = disableOfflineAccess() ? OfflineHandler.REMOVE_AND_CLOSE : OfflineHandler.REQUIRE_PERMISSIONS;
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
        if (this.disableSaving()) {
            return;
        }

        Stream.concat(inventories.values().stream(), enderChests.values().stream())
                .map(inventory -> {
                    // Cheat a bit - rather than stream twice, evict all viewers during remapping.
                    ejectViewers(inventory, viewer -> true);
                    if (inventory.getPlayer() instanceof Player player) {
                        return player;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .forEach(player -> {
                    if (!player.isOnline()) {
                        player = accessor.getPlayerDataManager().inject(player);
                    }
                    player.saveData();
                });
    }

    @Override
    public void onEnable() {
        // Save default configuration if not present.
        this.saveDefaultConfig();

        // Get plugin manager
        PluginManager pm = this.getServer().getPluginManager();

        this.accessor = new InternalAccessor(this);

        this.languageManager = new LanguageManager(this, "en_us");
        this.offlineHandler = disableOfflineAccess() ? OfflineHandler.REMOVE_AND_CLOSE : OfflineHandler.REQUIRE_PERMISSIONS;

        try {
            Class.forName("org.bukkit.entity.Player$Spigot");
            isSpigot = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            isSpigot = false;
        }

        // Version check
        if (isSpigot && this.accessor.isSupported()) {
            // Update existing configuration. May require internal access.
            new ConfigUpdater(this).checkForUpdates();

            // Register listeners
            pm.registerEvents(new PlayerListener(this), this);
            pm.registerEvents(new InventoryListener(this), this);

            // Register commands to their executors
            this.setCommandExecutor(new OpenInvCommand(this), "openinv", "openender");
            this.setCommandExecutor(new SearchContainerCommand(this), "searchcontainer");
            this.setCommandExecutor(new SearchInvCommand(this), "searchinv", "searchender");
            this.setCommandExecutor(new SearchEnchantCommand(this), "searchenchant");
            this.setCommandExecutor(new ContainerSettingCommand(this), "silentcontainer", "anycontainer");

        } else {
            this.sendVersionError(this.getLogger()::warning);
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
        if (!this.accessor.isSupported()) {
            messageMethod.accept("Your server version (" + this.accessor.getVersion() + ") is not supported.");
            messageMethod.accept("Please download the correct version of OpenInv here: " + this.accessor.getReleasesLink());
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
        return this.getConfig().getBoolean("settings.disable-saving", false);
    }

    @Override
    public boolean disableOfflineAccess() {
        return this.getConfig().getBoolean("settings.disable-offline-access", false);
    }

    @Override
    public @NotNull IAnySilentContainer getAnySilentContainer() {
        return this.accessor.getAnySilentContainer();
    }

    @Override
    public boolean getAnyContainerStatus(@NotNull final OfflinePlayer offline) {
        boolean defaultState = false;

        if (offline.isOnline()) {
            Player onlinePlayer = offline.getPlayer();
            if (onlinePlayer != null) {
                defaultState = Permissions.ANY_DEFAULT.hasPermission(onlinePlayer);
            }
        }

        return this.getConfig().getBoolean("toggles.any-chest." + offline.getUniqueId(), defaultState);
    }

    @Override
    public void setAnyContainerStatus(@NotNull final OfflinePlayer offline, final boolean status) {
        this.getConfig().set("toggles.any-chest." + offline.getUniqueId(), status);
        this.saveConfig();
    }

    @Override
    public boolean getSilentContainerStatus(@NotNull final OfflinePlayer offline) {
        boolean defaultState = false;

        if (offline.isOnline()) {
            Player onlinePlayer = offline.getPlayer();
            if (onlinePlayer != null) {
                defaultState = Permissions.SILENT_DEFAULT.hasPermission(onlinePlayer);
            }
        }

        return this.getConfig().getBoolean("toggles.silent-chest." + offline.getUniqueId(), defaultState);
    }

    @Override
    public void setSilentContainerStatus(@NotNull final OfflinePlayer offline, final boolean status) {
        this.getConfig().set("toggles.silent-chest." + offline.getUniqueId(), status);
        this.saveConfig();
    }

    @Override
    public @NotNull ISpecialEnderChest getSpecialEnderChest(@NotNull final Player player, final boolean online)
            throws InstantiationException {
        UUID key = player.getUniqueId();

        if (this.enderChests.containsKey(key)) {
            return this.enderChests.get(key);
        }

        ISpecialEnderChest inv = this.accessor.newSpecialEnderChest(player, online);
        this.enderChests.put(key, inv);
        return inv;
    }

    @Override
    public @NotNull ISpecialPlayerInventory getSpecialInventory(@NotNull final Player player, final boolean online)
            throws InstantiationException {
        UUID key = player.getUniqueId();

        if (this.inventories.containsKey(key)) {
            return this.inventories.get(key);
        }

        ISpecialPlayerInventory inv = this.accessor.newSpecialPlayerInventory(player, online);
        this.inventories.put(key, inv);
        return inv;
    }

    @Override
    public @Nullable InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory) {
        return this.accessor.getPlayerDataManager().openInventory(player, inventory);
    }

    @Override
    public boolean isPlayerLoaded(@NotNull UUID playerUuid) {
        return this.inventories.containsKey(playerUuid) || this.enderChests.containsKey(playerUuid);
    }

    @Override
    public @Nullable Player loadPlayer(@NotNull final OfflinePlayer offline) {
        UUID key = offline.getUniqueId();

        if (this.inventories.containsKey(key)) {
            return (Player) this.inventories.get(key).getPlayer();
        }

        if (this.enderChests.containsKey(key)) {
            return (Player) this.enderChests.get(key).getPlayer();
        }

        Player player = offline.getPlayer();
        if (player != null) {
            return player;
        }

        if (disableOfflineAccess() || !this.isSupportedVersion()) {
            return null;
        }

        if (Bukkit.isPrimaryThread()) {
            return this.accessor.getPlayerDataManager().loadPlayer(offline);
        }

        Future<Player> future = Bukkit.getScheduler().callSyncMethod(this,
                () -> OpenInv.this.accessor.getPlayerDataManager().loadPlayer(offline));

        try {
            player = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }

        return player;
    }

    @Override
    public void unload(@NotNull final OfflinePlayer offline) {
        setPlayerOffline(offline, OfflineHandler.REMOVE_AND_CLOSE);
    }

    /**
     * Evict all viewers lacking cross-world permissions when a {@link Player} changes worlds.
     *
     * @param player the Player
     */
    void changeWorld(@NotNull Player player) {
        UUID key = player.getUniqueId();

        if (this.inventories.containsKey(key)) {
            kickCrossWorldViewers(player, this.inventories.get(key));
        }

        if (this.enderChests.containsKey(key)) {
            kickCrossWorldViewers(player, this.enderChests.get(key));
        }
    }

    private void kickCrossWorldViewers(@NotNull Player player, @NotNull ISpecialInventory inventory) {
        ejectViewers(
                inventory,
                viewer ->
                        !Permissions.CROSSWORLD.hasPermission(viewer)
                                && !Objects.equals(viewer.getWorld(), player.getWorld()));
    }

    /**
     * Convert a raw slot number into a player inventory slot number.
     *
     * <p>Note that this method is specifically for converting an ISpecialPlayerInventory slot number into a regular
     * player inventory slot number.
     *
     * @param view    the open inventory view
     * @param rawSlot the raw slot in the view
     * @return the converted slot number
     */
    int convertToPlayerSlot(InventoryView view, int rawSlot) {
        return this.accessor.getPlayerDataManager().convertToPlayerSlot(view, rawSlot);
    }

    public @Nullable String getLocalizedMessage(@NotNull CommandSender sender, @NotNull String key) {
        return this.languageManager.getValue(key, getLocale(sender));
    }

    public @Nullable String getLocalizedMessage(
            @NotNull CommandSender sender,
            @NotNull String key,
            String @NotNull ... replacements) {
        return this.languageManager.getValue(key, getLocale(sender), replacements);
    }

    private @NotNull String getLocale(@NotNull CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getLocale();
        } else {
            return this.getConfig().getString("settings.locale", "en_us");
        }
    }

    public void sendMessage(@NotNull CommandSender sender, @NotNull String key) {
        String message = getLocalizedMessage(sender, key);

        if (message != null && !message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    public void sendMessage(@NotNull CommandSender sender, @NotNull String key, String @NotNull... replacements) {
        String message = getLocalizedMessage(sender, key, replacements);

        if (message != null && !message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    public void sendSystemMessage(@NotNull Player player, @NotNull String key) {
        String message = getLocalizedMessage(player, key);

        if (message == null) {
            return;
        }

        int newline = message.indexOf('\n');
        if (newline != -1) {
            // No newlines in action bar chat.
            message = message.substring(0, newline);
        }

        if (message.isEmpty()) {
            return;
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    /**
     * Method for handling a Player going offline.
     *
     * @param player the Player
     */
    void setPlayerOffline(@NotNull Player player) {
        setPlayerOffline(player, offlineHandler);
    }

    private void setPlayerOffline(@NotNull OfflinePlayer player, @NotNull OfflineHandler handler) {
        UUID key = player.getUniqueId();

        setPlayerOffline(inventories, key, handler);
        setPlayerOffline(enderChests, key, handler);
    }

    private void setPlayerOffline(
            @NotNull Map<UUID, ? extends ISpecialInventory> map,
            @NotNull UUID key,
            @NotNull OfflineHandler handler) {
        ISpecialInventory inventory = handler.fetch().apply(map, key);
        if (inventory == null) {
            return;
        }
        inventory.setPlayerOffline();
        if (!inventory.isInUse()) {
            map.remove(key);
        } else {
            handler.handle().accept(inventory);
        }
    }

    void handleCloseInventory(@NotNull HumanEntity exViewer, @NotNull ISpecialInventory inventory) {
        Map<UUID, ? extends ISpecialInventory> map = inventory instanceof ISpecialPlayerInventory ? inventories : enderChests;
        UUID key = inventory.getPlayer().getUniqueId();
        @Nullable ISpecialInventory loaded = map.get(key);

        if (loaded == null) {
            // Loaded inventory has already been removed. Removal will handle saving if necessary.
            return;
        }

        // This should only be possible if a plugin is doing funky things with our inventories.
        if (loaded != inventory) {
            Inventory bukkitInventory = inventory.getBukkitInventory();
            // Just in case, respect contents of the inventory that was just used.
            loaded.getBukkitInventory().setContents(bukkitInventory.getContents());
            // We need to close this inventory to reduce risk of duplication bugs if the user is offline.
            // We don't want to risk recursively closing the same inventory repeatedly, so we schedule dumping viewers.
            // Worst case we schedule a couple redundant tasks if several people had the inventory open.
            if (inventory.isInUse()) {
                getServer().getScheduler().runTask(this, () -> ejectViewers(inventory, viewer -> true));
            }
        }

        // Schedule task to check in use status later this tick. Closing user is still in viewer list.
        getServer().getScheduler().runTask(this, () -> {
            if (loaded.isInUse()) {
                return;
            }

            // Re-fetch from map - prevents duplicate saves on multi-close.
            ISpecialInventory current = map.remove(key);

            if (!disableSaving()
                    && current != null
                    && current.getPlayer() instanceof Player player
                    && !player.isOnline()) {
                    this.accessor.getPlayerDataManager().inject(player).saveData();
            }
        });
    }

    /**
     * Method for handling a Player coming online.
     *
     * @param player the Player
     * @throws IllegalStateException if the server version is unsupported
     */
    void setPlayerOnline(@NotNull Player player) {
        setPlayerOnline(inventories, player, player::updateInventory);
        setPlayerOnline(enderChests, player, null);
    }

    private void setPlayerOnline(
            @NotNull Map<UUID, ? extends ISpecialInventory> map,
            @NotNull Player player,
            @Nullable Runnable task) {
        ISpecialInventory inventory = map.get(player.getUniqueId());

        if (inventory == null) {
            // Inventory not open.
            return;
        }

        inventory.setPlayerOnline(player);

        // Eject viewers lacking permission.
        ejectViewers(
                inventory,
                viewer ->
                        !Permissions.OPENONLINE.hasPermission(viewer)
                                || !Permissions.CROSSWORLD.hasPermission(viewer)
                                && !Objects.equals(viewer.getWorld(), inventory.getPlayer().getWorld()));

        if (task != null) {
            getServer().getScheduler().runTask(this, task);
        }
    }

    static void ejectViewers(@NotNull ISpecialInventory inventory, @NotNull Predicate<@NotNull HumanEntity> predicate) {
        Inventory bukkitInventory = inventory.getBukkitInventory();
        for (HumanEntity viewer : new ArrayList<>(bukkitInventory.getViewers())) {
            if (viewer.getUniqueId().equals(inventory.getPlayer().getUniqueId())
                    && !viewer.getOpenInventory().getTopInventory().equals(bukkitInventory)) {
                // Skip owner with other inventory open. They aren't actually a viewer.
                continue;
            }
            if (predicate.test(viewer)) {
                viewer.closeInventory();
            }
        }
    }

}
