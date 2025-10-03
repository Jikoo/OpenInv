package com.lishid.openinv.internal.paper1_21_8.container;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenInventory extends com.lishid.openinv.internal.common.container.OpenInventory {

  public OpenInventory(@NotNull Player bukkitPlayer) {
    super(bukkitPlayer);
  }

  @Override
  public @NotNull Component getTitle(@Nullable ServerPlayer viewer) {
    MutableComponent component = Component.empty();
    // Prefix for use with custom bitmap image fonts.
    if (owner.equals(viewer)) {
      component.append(
          Component.translatableWithFallback("openinv.container.inventory.self", "")
              .withStyle(style -> style
                  .withFont(ResourceLocation.parse("openinv:font/inventory"))
                  .withColor(ChatFormatting.WHITE)));
    } else {
      component.append(
          Component.translatableWithFallback("openinv.container.inventory.other", "")
              .withStyle(style -> style
                  .withFont(ResourceLocation.parse("openinv:font/inventory"))
                  .withColor(ChatFormatting.WHITE)));
    }
    // Normal title: "Inventory - OwnerName"
    component.append(Component.translatableWithFallback("openinv.container.inventory.prefix", "", owner.getName()))
        .append(Component.translatable("container.inventory"))
        .append(Component.translatableWithFallback("openinv.container.inventory.suffix", " - %s", owner.getName()));
    return component;
  }

}
