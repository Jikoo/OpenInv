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
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchInvCommand implements TabExecutor {

  private final @NotNull LanguageManager lang;

  public SearchInvCommand(@NotNull LanguageManager lang) {
    this.lang = lang;
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args
  ) {

    Material material = null;

    if (args.length >= 1) {
      material = Material.matchMaterial(args[0]);
    }

    if (material == null) {
      lang.sendMessage(
          sender,
          "messages.error.invalidMaterial",
          new Replacement("%target%", args.length > 0 ? args[0] : "null")
      );
      return false;
    }

    int count = 1;

    if (args.length >= 2) {
      try {
        count = Integer.parseInt(args[1]);
      } catch (NumberFormatException ex) {
        lang.sendMessage(
            sender,
            "messages.error.invalidNumber",
            new Replacement("%target%", args[1])
        );
        return false;
      }
    }

    StringBuilder players = new StringBuilder();
    boolean searchInv = command.getName().equals("searchinv");
    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
      Inventory inventory = searchInv ? player.getInventory() : player.getEnderChest();
      if (findMatch(inventory, material, count)) {
        players.append(player.getName()).append(", ");
        break;
      }
    }

    // Matches found, delete trailing comma and space
    if (!players.isEmpty()) {
      players.delete(players.length() - 2, players.length());
    } else {
      lang.sendMessage(
          sender,
          "messages.info.player.noMatches",
          new Replacement("%target%", material.name())
      );
      return true;
    }

    lang.sendMessage(
        sender,
        "messages.info.player.matches",
        new Replacement("%target%", material.name()),
        new Replacement("%detail%", players.toString())
    );
    return true;
  }

  private boolean findMatch(@NotNull Inventory inventory, @NotNull Material material, int count) {
    AtomicInteger total = new AtomicInteger();
    return SearchHelper.findMatch(
        inventory,
        itemStack -> {
          if (itemStack.getType() == material && itemStack.getAmount() > 0) {
            return total.addAndGet(itemStack.getAmount()) >= count;
          }
          return false;
        }
    );
  }

  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
      @NotNull String[] args
  ) {
    if (args.length < 1 || args.length > 2 || !command.testPermissionSilent(sender)) {
      return Collections.emptyList();
    }

    String argument = args[args.length - 1];
    if (args.length == 1) {
      return TabCompleter.completeEnum(argument, Material.class);
    } else {
      return TabCompleter.completeInteger(argument);
    }
  }

}
