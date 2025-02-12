package com.lishid.openinv.internal.paper1_21_1.container;

import com.lishid.openinv.internal.common.container.slot.Content;
import com.lishid.openinv.internal.paper1_21_1.container.slot.ContentCraftingResult;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenInventory extends com.lishid.openinv.internal.common.container.OpenInventory {

  public OpenInventory(@NotNull Player bukkitPlayer) {
    super(bukkitPlayer);
  }

  @Override
  protected Content getCraftingResult(@NotNull ServerPlayer serverPlayer) {
    return new ContentCraftingResult(serverPlayer);
  }

}
