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

import com.lishid.openinv.util.TabCompleter;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.lang.Replacement;
import com.lishid.openinv.util.SearchHelper;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Command adding the ability to search online players' inventories for enchantments of a specific
 * type at or above the level specified.
 *
 * @author Jikoo
 */
public class SearchEnchantCommand implements TabExecutor {

  private final @NotNull LanguageManager lang;

  public SearchEnchantCommand(@NotNull LanguageManager lang) {
    this.lang = lang;
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args
  ) {
    if (args.length == 0) {
      return false;
    }

    Enchantment enchant = null;
    int level = 0;

    for (String argument : args) {
      try {
        level = Integer.parseInt(argument);
        continue;
      } catch (NumberFormatException ignored) {
        // Not a level being specified.
      }

      argument = argument.toLowerCase(Locale.ENGLISH);
      NamespacedKey key = NamespacedKey.fromString(argument);
      if (key == null) {
        continue;
      }

      Enchantment localEnchant = Registry.ENCHANTMENT.get(key);
      if (localEnchant != null) {
        enchant = localEnchant;
      }
    }

    // Arguments not set correctly
    if (level == 0 && enchant == null) {
      return false;
    }

    StringBuilder players = new StringBuilder();
    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
      boolean flagInventory = containsEnchantment(player.getInventory(), enchant, level);
      boolean flagEnder = containsEnchantment(player.getEnderChest(), enchant, level);

      // No matches, continue
      if (!flagInventory && !flagEnder) {
        continue;
      }

      // Matches, append details
      players.append(player.getName()).append(" (");
      if (flagInventory) {
        players.append("inv");
      }
      if (flagEnder) {
        if (flagInventory) {
          players.append(',');
        }
        players.append("ender");
      }
      players.append("), ");
    }

    if (!players.isEmpty()) {
      // Matches found, delete trailing comma and space
      players.delete(players.length() - 2, players.length());
    } else {
      lang.sendMessage(
          sender,
          "messages.info.player.noMatches",
          new Replacement("%target%", (enchant != null ? enchant.getKey().toString() : "") + " >= " + level)
      );
      return true;
    }

    lang.sendMessage(
        sender,
        "messages.info.player.matches",
        new Replacement("%target%", (enchant != null ? enchant.getKey().toString() : "") + " >= " + level),
        new Replacement("%detail%", players.toString())
    );
    return true;
  }

  private boolean containsEnchantment(Inventory inventory, @Nullable Enchantment enchant, int minLevel) {
    return SearchHelper.findMatch(
        inventory,
        itemStack -> {
          // Ensure meta is available and has enchantments.
          if (!itemStack.hasItemMeta()) {
            return false;
          }
          ItemMeta meta = itemStack.getItemMeta();
          if (meta == null || !meta.hasEnchants()) {
            return false;
          }

          // If enchantment is provided, use it.
          if (enchant != null) {
            return meta.getEnchantLevel(enchant) >= minLevel;
          }

          // Otherwise, check all enchantment levels.
          for (int enchLevel : meta.getEnchants().values()) {
            if (enchLevel >= minLevel) {
              return true;
            }
          }

          return false;
        }
    );
  }

  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args
  ) {
    if (!command.testPermissionSilent(sender) || args.length < 1 || args.length > 2) {
      return Collections.emptyList();
    }

    if (args.length == 1) {
      return TabCompleter.completeObject(args[0], enchantment -> enchantment.getKey().toString(), Registry.ENCHANTMENT.stream().toArray(Enchantment[]::new));
    } else {
      return TabCompleter.completeInteger(args[1]);
    }
  }

}
