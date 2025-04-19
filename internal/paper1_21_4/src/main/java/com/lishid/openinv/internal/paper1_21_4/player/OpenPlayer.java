package com.lishid.openinv.internal.paper1_21_4.player;

import com.lishid.openinv.internal.common.player.PlayerManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.CraftServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenPlayer extends com.lishid.openinv.internal.common.player.OpenPlayer {

  protected OpenPlayer(CraftServer server, ServerPlayer entity,
      PlayerManager manager) {
    super(server, entity, manager);
  }

  @Contract("null -> new")
  @Override
  protected @NotNull CompoundTag getWritableTag(@Nullable CompoundTag oldData) {
    if (oldData == null) {
      return new CompoundTag();
    }

    // Copy old data. This is a deep clone, so operating on it should be safe.
    oldData = oldData.copy();

    // Remove vanilla/server data that is not written every time.
    oldData.getAllKeys()
        .removeIf(key -> RESET_TAGS.contains(key) || key.startsWith("Bukkit"));

    return oldData;
  }

}
