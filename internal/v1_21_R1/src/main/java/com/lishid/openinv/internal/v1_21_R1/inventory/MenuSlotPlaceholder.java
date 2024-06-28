package com.lishid.openinv.internal.v1_21_R1.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

abstract class MenuSlotPlaceholder extends Slot {

  private static final ItemStack OFFLINE;
  private static final ItemStack CREATIVE;
  private static final ItemStack SPECTATOR;

  static {
    // Barrier: "Not available - Offline"
    OFFLINE = new ItemStack(Items.BARRIER);
    OFFLINE.set(DataComponents.CUSTOM_NAME,
        Component.translatable("options.narrator.notavailable")
            .withStyle(style -> style.withItalic(false))
            .append(Component.literal(" - "))
            .append(Component.translatable("gui.socialInteractions.status_offline")));
    // Barrier: "Not available - Creative Mode"
    CREATIVE = new ItemStack(Items.BARRIER);
    CREATIVE.set(
        DataComponents.CUSTOM_NAME,
        Component.translatable("options.narrator.notavailable")
            .withStyle(style -> style.withItalic(false))
            .append(" - ")
            .append(GameType.CREATIVE.getLongDisplayName()));
    // Barrier: "Not available - Spectator Mode"
    SPECTATOR = new ItemStack(Items.BARRIER);
    SPECTATOR.set(
        DataComponents.CUSTOM_NAME,
        Component.translatable("options.narrator.notavailable")
            .withStyle(style -> style.withItalic(false))
            .append(" - ")
            .append(GameType.SPECTATOR.getLongDisplayName()));
  }

  MenuSlotPlaceholder(Container container, int index, int x, int y) {
    super(container, index, x, y);
  }

  public static ItemStack onlineOnly(@NotNull ServerPlayer serverPlayer, Supplier<ItemStack> whenValid) {
    if (serverPlayer.connection != null && !serverPlayer.connection.isDisconnected()) {
      return whenValid.get();
    }
    return OFFLINE;
  }

  public static ItemStack survivalOnly(@NotNull ServerPlayer serverPlayer, Supplier<ItemStack> whenValid) {
    return onlineOnly(serverPlayer, () -> {
      if (serverPlayer.gameMode.isSurvival()) {
        return whenValid.get();
      }
      GameType gameType = serverPlayer.gameMode.getGameModeForPlayer();
      return switch (gameType) {
        case CREATIVE -> CREATIVE;
        case SPECTATOR -> SPECTATOR;
        // Just in case, fall through to creating new items.
        // This is a lot less good - inventory syncher will create copies frequently.
        default -> {
          ItemStack itemStack = new ItemStack(Items.BARRIER);
          itemStack.set(
              DataComponents.CUSTOM_NAME,
              Component.translatable("options.narrator.notavailable")
                  .withStyle(style -> style.withItalic(false))
                  .append(" - ")
                  .append(gameType.getLongDisplayName()));
          yield itemStack;
        }
      };
    });
  }

  abstract ItemStack getPlaceholder();

  public void onTake(Player var0, ItemStack var1) {
    this.setChanged();
  }

  public boolean mayPlace(ItemStack var0) {
    return true;
  }

  public ItemStack getItem() {
    ItemStack current = this.container.getItem(this.slot);
    return current.isEmpty() ? getPlaceholder() : current;
  }

  public boolean hasItem() {
    return !this.container.getItem(this.slot).isEmpty();
  }

  public ItemStack remove(int amount) {
    return this.container.removeItem(this.slot, amount);
  }

}
