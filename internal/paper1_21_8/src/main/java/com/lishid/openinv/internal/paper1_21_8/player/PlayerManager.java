package com.lishid.openinv.internal.paper1_21_8.player;

import com.lishid.openinv.internal.common.player.BaseOpenPlayer;
import com.lishid.openinv.util.JulLoggerAdapter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class PlayerManager extends com.lishid.openinv.internal.common.player.PlayerManager {

  public PlayerManager(@NotNull Logger logger) {
    super(logger);
  }

  @Override
  protected boolean loadData(@NotNull MinecraftServer server, @NotNull ServerPlayer player) {
    // See CraftPlayer#loadData

    try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(player.problemPath(), new JulLoggerAdapter(logger))) {
      ValueInput loadedData = server.getPlayerList().playerIo.load(player, scopedCollector).orElse(null);

      if (loadedData == null) {
        // Exceptions with loading are logged.
        return false;
      }

      // Read basic data into the player.
      player.load(loadedData);
      // Game type settings are loaded separately.
      player.loadGameTypes(loadedData);

      // World is not loaded by ServerPlayer#load(CompoundTag) on Paper.
      parseWorld(server, player, loadedData);
    }

    return true;
  }

  @Override
  protected void spawnInDefaultWorld(@NotNull MinecraftServer server, @NotNull ServerPlayer player) {
    ServerLevel level = server.getLevel(Level.OVERWORLD);
    if (level != null) {
      // Adjust player to default spawn (in keeping with Paper handling) when world not found.
      player.snapTo(player.adjustSpawnLocation(level, level.getSharedSpawnPos()).getBottomCenter(), level.getSharedSpawnAngle(), 0.0F);
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

    bukkitEntity.set(player, new OpenPlayer(server.server, player, this));
  }

  @Override
  public @NotNull Player inject(@NotNull Player player) {
    try {
      ServerPlayer nmsPlayer = getHandle(player);
      if (nmsPlayer.getBukkitEntity() instanceof BaseOpenPlayer openPlayer) {
        return openPlayer;
      }
      MinecraftServer server = nmsPlayer.getServer();
      if (server == null) {
        if (!(Bukkit.getServer() instanceof CraftServer craftServer)) {
          logger.warning(() ->
              "Unable to inject ServerPlayer, certain player data may be lost when saving! Server is not a CraftServer: "
                  + Bukkit.getServer().getClass().getName());
          return player;
        }
        server = craftServer.getServer();
      }
      injectPlayer(server, nmsPlayer);
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

}
