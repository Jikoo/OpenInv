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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

@NullMarked
public class ClearInvCommand extends PlayerLookupCommand {

  private final InventoryManager manager;

  public ClearInvCommand(
      OpenInv plugin,
      Config config,
      InventoryManager manager,
      LanguageManager lang,
      PlayerLoader playerLoader
  ) {
    super(plugin, lang, config, playerLoader);
    this.manager = manager;
  }

  @Override
  protected boolean isAccessInventory(Command command) {
    return command.getName().equals("clearinv");
  }

  @Override
  protected @Nullable String getTargetIdentifer(
      CommandSender sender,
      Command command,
      @Nullable String argument,
      boolean accessInv
  ) {
    if (!Permissions.CLEAR_OTHER.hasPermission(sender)) {
      // If the sender does not have permissions to clear others, use self.
      if (sender instanceof Player player) {
        return player.getUniqueId().toString();
      }

      // Console can't target itself. Send error.
      lang.sendMessage(sender, "messages.error.permissionOpenOther");
      return null;
    }

    // If argument is provided, use it.
    if (argument != null) {
      return argument;
    }

    // For players, default to self.
    if (sender instanceof Player player) {
      return player.getUniqueId().toString();
    }

    // For console, argument is required. Send usage.
    sender.sendMessage(command.getUsage());
    return null;
  }

  @Override
  protected @Nullable OfflinePlayer getTarget(String identifier) {
    return playerLoader.matchExact(identifier);
  }

  @Override
  protected boolean deniedCommand(CommandSender sender, Player onlineTarget, boolean accessInv) {
    if (onlineTarget.equals(sender)) {
      return !Permissions.CLEAR_SELF.hasPermission(sender);
    }
    return !Permissions.CLEAR_OTHER.hasPermission(sender);
  }

  @Override
  protected void handle(
      CommandSender sender,
      PlayerAccess playerAccess,
      boolean accessInv,
      String[] args
  ) {
    Player onlineTarget = playerAccess.player();
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
        accessInv ? "messages.info.clear.inventory" : "messages.info.clear.enderchest",
        new Replacement("%target%", onlineTarget.getDisplayName())
    );
  }

  @Override
  public @Nullable List<String> onTabComplete(
      CommandSender sender,
      Command command,
      String label,
      String[] args
  ) {
    if (!Permissions.CLEAR_OTHER.hasPermission(sender)) {
      return List.of();
    }

    return super.onTabComplete(sender, command, label, args);
  }

}
