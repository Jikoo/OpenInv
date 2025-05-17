package com.lishid.openinv.command;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.util.InventoryManager;
import com.lishid.openinv.util.Permissions;
import com.lishid.openinv.util.PlayerLoader;
import com.lishid.openinv.util.config.Config;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.lang.Replacement;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class ClearInvCommand extends PlayerLookupCommand {

    private final @NotNull InventoryManager manager;

    public ClearInvCommand(
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
        return command.getName().equals("clearinv");
    }

    @Override
    protected @Nullable String getTargetIdentifer(
        @NotNull CommandSender sender,
        @Nullable String argument,
        boolean accessInv
    ) {
        if (argument != null) {
            return argument;
        }
        if (sender instanceof Player player) {
            return player.getUniqueId().toString();
        }
        return null;
    }

    @Override
    protected @Nullable OfflinePlayer getTarget(@NotNull String identifier) {
        return playerLoader.matchExact(identifier);
    }

    @Override
    protected boolean deniedCommand(@NotNull CommandSender sender, @NotNull Player onlineTarget, boolean accessInv) {
        if (onlineTarget.equals(sender)) {
            return !Permissions.CLEAR_SELF.hasPermission(sender);
        }
        return !Permissions.CLEAR_OTHER.hasPermission(sender);
    }

    @Override
    protected void handle(
            @NotNull CommandSender sender,
            @NotNull Player onlineTarget,
            boolean accessInv,
            @NotNull String @NotNull [] args
    ) {
        // Create the inventory
        final ISpecialInventory inv;
        try {
            inv = accessInv ? manager.getInventory(onlineTarget) : manager.getEnderChest(onlineTarget);
        } catch (Exception e) {
            lang.sendMessage(sender, "messages.error.commandException");
            plugin.getLogger().log(Level.WARNING, "Unable to create ISpecialInventory", e);
            return;
        }

        // Clear the inventory
        inv.getBukkitInventory().clear();
        manager.save(onlineTarget.getUniqueId());
        lang.sendMessage(
                sender,
                "messages.info.inventoryCleared",
                new Replacement("%target%", onlineTarget.getDisplayName())
        );
    }

}
