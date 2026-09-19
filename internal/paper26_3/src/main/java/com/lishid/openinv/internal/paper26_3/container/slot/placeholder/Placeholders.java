package com.lishid.openinv.internal.paper26_3.container.slot.placeholder;

import com.lishid.openinv.internal.paper26_3.player.OpenPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.NullMarked;

import java.util.EnumMap;

@NullMarked
public final class Placeholders {

  // N.B. Slot#getNoItemIcon is still clientside, unfortunately.
  // Worth checking every few updates just in case - it would be nice to
  // get rid of some of the placeholders.

  static final EnumMap<GameType, ItemStack> BLOCKED_GAME_TYPE = new EnumMap<>(GameType.class);
  public static ItemStack craftingOutput = ItemStack.EMPTY;
  public static ItemStack cursor = ItemStack.EMPTY;
  public static ItemStack drop = ItemStack.EMPTY;
  public static ItemStack emptyHelmet = ItemStack.EMPTY;
  public static ItemStack emptyChestplate = ItemStack.EMPTY;
  public static ItemStack emptyLeggings = ItemStack.EMPTY;
  public static ItemStack emptyBoots = ItemStack.EMPTY;
  public static ItemStack emptyOffHand = ItemStack.EMPTY;
  public static ItemStack notSlot = ItemStack.EMPTY;
  public static ItemStack blockedOffline = ItemStack.EMPTY;

  public static ItemStack survivalOnly(ServerPlayer serverPlayer) {
    if (!OpenPlayer.isConnected(serverPlayer.connection)) {
      return blockedOffline;
    }

    return BLOCKED_GAME_TYPE.getOrDefault(serverPlayer.gameMode.getGameModeForPlayer(), ItemStack.EMPTY);
  }

  private Placeholders() {
    throw new IllegalStateException("Cannot create instance of utility class.");
  }

}
