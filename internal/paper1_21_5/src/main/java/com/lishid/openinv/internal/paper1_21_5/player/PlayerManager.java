package com.lishid.openinv.internal.paper1_21_5.player;

import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.dimension.DimensionType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class PlayerManager extends com.lishid.openinv.internal.paper1_21_8.player.PlayerManager {

  public PlayerManager(@NotNull Logger logger) {
    super(logger);
  }

  @Override
  protected boolean loadData(@NotNull MinecraftServer server, @NotNull ServerPlayer player) {
    // See CraftPlayer#loadData
    CompoundTag loadedData = server.getPlayerList().playerIo.load(player).orElse(null);

    if (loadedData == null) {
      // Exceptions with loading are logged.
      return false;
    }

    // Read basic data into the player.
    player.load(loadedData);
    // Game type settings are also loaded separately.
    player.loadGameTypes(loadedData);

    // World is not loaded by ServerPlayer#load(CompoundTag) on Paper.
    parseWorld(player, loadedData);

    return true;
  }

  protected void parseWorld(@NotNull ServerPlayer player, @NotNull CompoundTag loadedData) {
    // See PlayerList#placeNewPlayer
    World bukkitWorld;
    Optional<Long> msbs = loadedData.getLong("WorldUUIDMost");
    Optional<Long> lsbs = loadedData.getLong("WorldUUIDLeast");
    if (msbs.isPresent() && lsbs.isPresent()) {
      // Modern Bukkit world.
      bukkitWorld = Bukkit.getServer().getWorld(new UUID(msbs.get(), lsbs.get()));
    } else {
      Optional<String> worldName = loadedData.getString("world");
      if (worldName.isPresent()) {
        // Legacy Bukkit world.
        bukkitWorld = Bukkit.getServer().getWorld(worldName.get());
      } else {
        // Vanilla player data.
        DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, loadedData.get("Dimension")))
            .resultOrPartial(logger::warning)
            .map(player.server::getLevel)
            // If ServerLevel exists, set, otherwise move to spawn.
            .ifPresentOrElse(player::setServerLevel, () -> spawnInDefaultWorld(player.server, player));
        return;
      }
    }
    if (bukkitWorld == null) {
      spawnInDefaultWorld(player.server, player);
      return;
    }
    player.setServerLevel(((CraftWorld) bukkitWorld).getHandle());
  }

  @Override
  public @NotNull Player inject(@NotNull Player player) {
    try {
      ServerPlayer nmsPlayer = getHandle(player);
      if (nmsPlayer.getBukkitEntity() instanceof OpenPlayer openPlayer) {
        return openPlayer;
      }
      injectPlayer(nmsPlayer.server, nmsPlayer);
      return nmsPlayer.getBukkitEntity();
    } catch (IllegalAccessException e) {
      logger.log(
          java.util.logging.Level.WARNING,
          e,
          () -> "Unable to inject ServerPlayer, certain player data may be lost when saving!"
      );
      return player;
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
