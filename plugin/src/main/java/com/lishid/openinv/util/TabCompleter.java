/*
 * Copyright (C) 2011-2021 lishid. All rights reserved.
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

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Utility class for common tab completions.
 */
public final class TabCompleter {

    /**
     * Offer tab completions for whole numbers.
     *
     * @param argument the argument to complete
     * @return integer options
     */
    public static List<String> completeInteger(String argument) {
        // Ensure existing argument is actually a number
        if (!argument.isEmpty()) {
            try {
                Integer.parseInt(argument);
            } catch (NumberFormatException e) {
                return Collections.emptyList();
            }
        }

        List<String> completions = new ArrayList<>(10);
        for (int i = 0; i < 10; ++i) {
            completions.add(argument + i);
        }

        return completions;
    }

    /**
     * Offer tab completions for a given Enum.
     *
     * @param argument the argument to complete
     * @param enumClazz the Enum to complete for
     * @return the matching Enum values
     */
    public static List<String> completeEnum(String argument, Class<? extends Enum<?>> enumClazz) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (Enum<?> enumConstant : enumClazz.getEnumConstants()) {
            String name = enumConstant.name().toLowerCase(Locale.ENGLISH);
            if (name.startsWith(argument)) {
                completions.add(name);
            }
        }

        return completions;
    }

    /**
     * Offer tab completions for a given array of Strings.
     *
     * @param argument the argument to complete
     * @param options the Strings which may be completed
     * @return the matching Strings
     */
    public static List<String> completeString(String argument, String[] options) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (String option : options) {
            if (option.startsWith(argument)) {
                completions.add(option);
            }
        }

        return completions;
    }

    /**
     * Offer tab completions for visible online Players' names.
     *
     * @param sender the command's sender
     * @param argument the argument to complete
     * @return the matching Players' names
     */
    public static List<String> completeOnlinePlayer(CommandSender sender, String argument) {
        List<String> completions = new ArrayList<>();
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (senderPlayer != null && !senderPlayer.canSee(player)) {
                continue;
            }

            if (StringUtil.startsWithIgnoreCase(player.getName(), argument)) {
                completions.add(player.getName());
            }
        }

        return completions;
    }

    /**
     * Offer tab completions for a given array of Objects.
     *
     * @param argument the argument to complete
     * @param converter the Function for converting the Object into a comparable String
     * @param options the Objects which may be completed
     * @return the matching Strings
     */
    public static <T> List<String> completeObject(String argument, Function<T, String> converter, T[] options) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (T option : options) {
            String optionString = converter.apply(option).toLowerCase(Locale.ENGLISH);
            if (optionString.startsWith(argument)) {
                completions.add(optionString);
            }
        }

        return completions;
    }

    private TabCompleter() {
        throw new IllegalStateException("Cannot create instance of utility class.");
    }

}
