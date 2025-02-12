package com.lishid.openinv.internal.paper1_21_1.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class PlayerManager extends com.lishid.openinv.internal.common.player.PlayerManager {
  public PlayerManager(@NotNull Logger logger) {
    super(logger);
  }

  @Override
  protected @NotNull ServerPlayer createNewPlayer(
      @NotNull MinecraftServer server,
      @NotNull ServerLevel worldServer,
      @NotNull final OfflinePlayer offline) {
    // See net.minecraft.server.players.PlayerList#canPlayerLogin(ServerLoginPacketListenerImpl, GameProfile)
    // See net.minecraft.server.network.ServerLoginPacketListenerImpl#handleHello(ServerboundHelloPacket)
    GameProfile profile = new GameProfile(offline.getUniqueId(),
        offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());

    ClientInformation dummyInfo = new ClientInformation(
        "en_us",
        1, // Reduce distance just in case.
        ChatVisiblity.HIDDEN, // Don't accept chat.
        false,
        ServerPlayer.DEFAULT_MODEL_CUSTOMIZATION,
        ServerPlayer.DEFAULT_MAIN_HAND,
        true,
        false // Don't list in player list (not that this player is in the list anyway).
    );

    ServerPlayer entity = new ServerPlayer(server, worldServer, profile, dummyInfo);

    try {
      injectPlayer(entity);
    } catch (IllegalAccessException e) {
      logger.log(
          java.util.logging.Level.WARNING,
          e,
          () -> "Unable to inject ServerPlayer, certain player data may be lost when saving!");
    }

    return entity;
  }

}
