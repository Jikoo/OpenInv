package com.lishid.openinv.internal.common.container.slot.placeholder;

import com.lishid.openinv.internal.common.player.OpenPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

public final class Placeholders {

  static final @NotNull EnumMap<GameType, ItemStack> BLOCKED_GAME_TYPE = new EnumMap<>(GameType.class);
  public static @NotNull ItemStack craftingOutput = ItemStack.EMPTY;
  public static @NotNull ItemStack cursor = ItemStack.EMPTY;
  public static @NotNull ItemStack drop = ItemStack.EMPTY;
  public static @NotNull ItemStack emptyHelmet = ItemStack.EMPTY;
  public static @NotNull ItemStack emptyChestplate = ItemStack.EMPTY;
  public static @NotNull ItemStack emptyLeggings = ItemStack.EMPTY;
  public static @NotNull ItemStack emptyBoots = ItemStack.EMPTY;
  public static @NotNull ItemStack emptyOffHand = ItemStack.EMPTY;
  public static @NotNull ItemStack notSlot = ItemStack.EMPTY;
  public static @NotNull ItemStack blockedOffline = ItemStack.EMPTY;

  public static ItemStack survivalOnly(@NotNull ServerPlayer serverPlayer) {
    if (!OpenPlayer.isConnected(serverPlayer.connection)) {
      return blockedOffline;
    }

    return BLOCKED_GAME_TYPE.getOrDefault(serverPlayer.gameMode.getGameModeForPlayer(), ItemStack.EMPTY);
  }

  private Placeholders() {
    throw new IllegalStateException("Cannot create instance of utility class.");
  }

}
