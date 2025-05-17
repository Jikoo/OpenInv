package com.lishid.openinv.command;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.util.AccessEqualMode;
import com.lishid.openinv.util.Permissions;
import com.lishid.openinv.util.PlayerLoader;
import com.lishid.openinv.util.TabCompleter;
import com.lishid.openinv.util.config.Config;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.lang.Replacement;
import me.nahu.scheduler.wrapper.runnable.WrappedRunnable;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A command abstraction for performing actions after looking up and loading a player.
 */
public abstract class PlayerLookupCommand implements TabExecutor {

  protected final @NotNull OpenInv plugin;
  protected final @NotNull LanguageManager lang;
  protected final @NotNull Config config;
  protected final @NotNull PlayerLoader playerLoader;

  public PlayerLookupCommand(
      @NotNull OpenInv plugin,
      @NotNull LanguageManager lang,
      @NotNull Config config,
      @NotNull PlayerLoader playerLoader) {
    this.plugin = plugin;
    this.lang = lang;
    this.config = config;
    this.playerLoader = playerLoader;
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String @NotNull [] args) {

    // Inventory or ender chest?
    boolean accessInv = isAccessInventory(command);

    // Get target identifier from parameters.
    String targetId = getTargetIdentifer(sender, args.length > 0 ? args[0] : null, accessInv);
    if (targetId == null) {
      return true;
    }

    new WrappedRunnable() {
      @Override
      public void run() {
        // Get target from identifier.
        final OfflinePlayer target = getTarget(targetId);

        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
          lang.sendMessage(sender, "messages.error.invalidPlayer");
          return;
        }

        new WrappedRunnable() {
          @Override
          public void run() {
            // Ensure sender still exists.
            if ((sender instanceof Player player) && !player.isValid()) {
              return;
            }

            // Perform access checks and load target if necessary.
            Player onlineTarget = access(sender, target, accessInv);

            if (onlineTarget != null) {
              handle(sender, onlineTarget, accessInv, args);
            }
          }
        }.runTask(PlayerLookupCommand.this.plugin);

      }
    }.runTaskAsynchronously(this.plugin);

    return false;
  }

  /**
   * Get whether a player inventory or ender chest is accessed by the {@link Command} executed.
   *
   * @param command the {@code Command} being executed
   * @return {@code true} if the command is for inventories, {@code false} for ender chests
   */
  protected abstract boolean isAccessInventory(@NotNull Command command);

  /**
   * Determine the target identifier from the first command argument.
   *
   * <p>Implementation note: a return value of {@code null} will cause the command to cease
   * execution with no feedback. Appropriate feedback should be sent in the implementation.</p>
   *
   * @param sender the sender of the command
   * @param argument the argument, or {@code null} if none provided
   * @param accessInv {@code true} if an inventory is being accessed, {@code false} for ender chest
   * @return an updated target identifier or {@code null} if no target is available
   */
  protected abstract @Nullable String getTargetIdentifer(@NotNull CommandSender sender, @Nullable String argument, boolean accessInv);

  /**
   * Get an {@link OfflinePlayer} by identifier.
   *
   * @param identifier the identifier
   * @return the corresponding player or {@code null} if no match was found
   */
  protected abstract @Nullable OfflinePlayer getTarget(@NotNull String identifier);

  /**
   * Attempt to access the target as an online player. Performs feedback in the event of denial.
   *
   * @param sender the {@link CommandSender} attempting access
   * @param target the {@link OfflinePlayer} being targeted by the command
   * @param invPerms {@code true} to use inventory permissions, {@code false} for ender chest
   * @return the {@link Player} loaded or {@code null} if target is not accessible
   */
  protected @Nullable Player access(@NotNull CommandSender sender, @NotNull OfflinePlayer target, boolean invPerms) {
    // Attempt to load online player dependent on permissions and configuration.
    Player onlineTarget = accessAsPlayer(sender, target);

    if (onlineTarget == null) {
      return null;
    }

    // Permissions checks.
    if (deniedCommand(sender, onlineTarget, invPerms) || deniedAccess(sender, onlineTarget)) {
      return null;
    }

    return onlineTarget;
  }

  /**
   * Helper for accessing target as an online {@link Player}. Performs checks
   * and feedback for configuration and online/offline permissions.
   *
   * @param sender the {@link CommandSender} attempting access
   * @param target the {@link OfflinePlayer} being targeted by the command
   * @return the {@link Player} loaded or {@code null} if target is not accessible
   */
  protected @Nullable Player accessAsPlayer(@NotNull CommandSender sender, @NotNull OfflinePlayer target) {
    Player onlineTarget;

    if (!target.isOnline()) {
      if (!config.isOfflineDisabled() && Permissions.ACCESS_OFFLINE.hasPermission(sender)) {
        // Try loading the player's data.
        onlineTarget = playerLoader.load(target);
      } else {
        lang.sendMessage(sender, "messages.error.permissionPlayerOffline");
        return null;
      }
    } else {
      if (Permissions.ACCESS_ONLINE.hasPermission(sender)) {
        onlineTarget = target.getPlayer();
      } else {
        lang.sendMessage(sender, "messages.error.permissionPlayerOnline");
        return null;
      }
    }

    if (onlineTarget == null) {
      lang.sendMessage(sender, "messages.error.invalidPlayer");
      return null;
    }

    return onlineTarget;
  }

  /**
   * Check for a lack of permissions related to the specific command being executed for the sender.
   * For example, {@link Permissions#INVENTORY_OPEN_OTHER} might be required if the target and sender differ.
   *
   * @param sender the {@link CommandSender} attempting access
   * @param onlineTarget the {@link Player} being targeted by the command
   * @param accessInv {@code true} to use inventory permissions, {@code false} for ender chest
   * @return {@code true} if the sender does not have the correct execution-specific permission
   */
  protected abstract boolean deniedCommand(@NotNull CommandSender sender, @NotNull Player onlineTarget, boolean accessInv);

  /**
   * Check for a lack of generalized permissions for accessing the target.
   * By default, this is access levels and cross-world restrictions.
   *
   * @param sender the {@link CommandSender} attempting access
   * @param onlineTarget the {@link Player} being targeted by the command
   * @return {@code true} if the sender does not have access to the target
   */
  protected boolean deniedAccess(@NotNull CommandSender sender, @NotNull Player onlineTarget) {
    if (sender.equals(onlineTarget)) {
      return false;
    }

    // Protected check
    for (int level = 4; level > 0; --level) {
      String permission = "openinv.access.level." + level;
      if (onlineTarget.hasPermission(permission)
          && (!sender.hasPermission(permission) || config.getAccessEqualMode() == AccessEqualMode.DENY)) {
        lang.sendMessage(
            sender,
            "messages.error.permissionExempt",
            new Replacement("%target%", onlineTarget.getDisplayName()));
        return true;
      }
    }

    // Crossworld check
    if (sender instanceof Player player
        && !Permissions.ACCESS_CROSSWORLD.hasPermission(sender)
        && !onlineTarget.getWorld().equals(player.getWorld())) {
      lang.sendMessage(
          sender,
          "messages.error.permissionCrossWorld",
          new Replacement("%target%", onlineTarget.getDisplayName()));
      return true;
    }

    return false;
  }

  /**
   * Perform main command functionality.
   *
   * @param sender the {@link CommandSender} executing the command
   * @param target the {@link Player} being targeted
   * @param accessInv {@code true} if an inventory is being accessed, {@code false} for ender chest
   * @param args the original command arguments
   */
  protected abstract void handle(
      @NotNull CommandSender sender,
      @NotNull Player target,
      boolean accessInv,
      @NotNull String @NotNull [] args);

  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    if (!command.testPermissionSilent(sender) || args.length != 1) {
      return Collections.emptyList();
    }

    return TabCompleter.completeOnlinePlayer(sender, args[0]);
  }

}
