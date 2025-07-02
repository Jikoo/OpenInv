package com.lishid.openinv.internal.paper1_21_4.player;

import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Logger;

public class PlayerManager extends com.lishid.openinv.internal.paper1_21_5.player.PlayerManager {

  public PlayerManager(@NotNull Logger logger) {
    super(logger);
  }

  @Override
  protected void parseWorld(@NotNull ServerPlayer player, @NotNull CompoundTag loadedData) {
    // See PlayerList#placeNewPlayer
    World bukkitWorld;
    if (loadedData.contains("WorldUUIDMost") && loadedData.contains("WorldUUIDLeast")) {
      // Modern Bukkit world.
      bukkitWorld = Bukkit.getServer().getWorld(new UUID(loadedData.getLong("WorldUUIDMost"), loadedData.getLong("WorldUUIDLeast")));
    } else if (loadedData.contains("world", net.minecraft.nbt.Tag.TAG_STRING)) {
      // Legacy Bukkit world.
      bukkitWorld = Bukkit.getServer().getWorld(loadedData.getString("world"));
    } else {
      // Vanilla player data.
      DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, loadedData.get("Dimension")))
          .resultOrPartial(logger::warning)
          .map(player.server::getLevel)
          // If ServerLevel exists, set, otherwise move to spawn.
          .ifPresentOrElse(player::setServerLevel, () -> spawnInDefaultWorld(player.server, player));
      return;
    }
    if (bukkitWorld == null) {
      spawnInDefaultWorld(player.server, player);
      return;
    }
    player.setServerLevel(((CraftWorld) bukkitWorld).getHandle());
  }

  @Override
  protected void spawnInDefaultWorld(@NotNull MinecraftServer server, @NotNull ServerPlayer player) {
    ServerLevel level = server.getLevel(Level.OVERWORLD);
    if (level != null) {
      // Adjust player to default spawn (in keeping with Paper handling) when world not found.
      player.moveTo(player.adjustSpawnLocation(level, level.getSharedSpawnPos()).getBottomCenter(), level.getSharedSpawnAngle(), 0.0F);
      player.spawnIn(level);
    } else {
      logger.warning("Tried to load player with invalid world when no fallback was available!");
    }
  }

  @Override
  protected void injectPlayer(@NotNull MinecraftServer server, @NotNull ServerPlayer player) throws IllegalAccessException {
    if (bukkitEntity == null) {
      return;
    }

    bukkitEntity.setAccessible(true);
    bukkitEntity.set(player, new OpenPlayer(player.server.server, player, this));
  }

}
