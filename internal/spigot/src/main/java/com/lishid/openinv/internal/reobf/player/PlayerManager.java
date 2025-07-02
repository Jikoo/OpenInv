package com.lishid.openinv.internal.reobf.player;

import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.reobf.container.OpenEnderChest;
import com.lishid.openinv.internal.reobf.container.OpenInventory;
import com.lishid.openinv.util.JulLoggerAdapter;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_21_R5.CraftServer;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R5.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.logging.Logger;

public class PlayerManager implements com.lishid.openinv.internal.PlayerManager {

  private final @NotNull Logger logger;
  private @Nullable Field bukkitEntity;

  public PlayerManager(@NotNull Logger logger) {
    this.logger = logger;
    try {
      bukkitEntity = Entity.class.getDeclaredField("bukkitEntity");
    } catch (NoSuchFieldException e) {
      logger.warning("Unable to obtain field to inject custom save process - certain player data may be lost when saving!");
      logger.log(java.util.logging.Level.WARNING, e.getMessage(), e);
      bukkitEntity = null;
    }
  }

  public static @NotNull ServerPlayer getHandle(final Player player) {
    if (player instanceof CraftPlayer craftPlayer) {
      return craftPlayer.getHandle();
    }

    Server server = player.getServer();
    ServerPlayer nmsPlayer = null;

    if (server instanceof CraftServer craftServer) {
      nmsPlayer = craftServer.getHandle().getPlayer(player.getUniqueId());
    }

    if (nmsPlayer == null) {
      // Could use reflection to examine fields, but it's honestly not worth the bother.
      throw new RuntimeException("Unable to fetch EntityPlayer from Player implementation " + player.getClass().getName());
    }

    return nmsPlayer;
  }

  @Override
  public @Nullable Player loadPlayer(@NotNull final OfflinePlayer offline) {
    if (!(Bukkit.getServer() instanceof CraftServer craftServer)) {
      return null;
    }

    MinecraftServer server = craftServer.getServer();
    ServerLevel worldServer = server.getLevel(Level.OVERWORLD);

    if (worldServer == null) {
      return null;
    }

    // Create a new ServerPlayer.
    ServerPlayer entity = createNewPlayer(server, worldServer, offline);

    // Stop listening for advancement progression - if this is not cleaned up, loading causes a memory leak.
    entity.getAdvancements().stopListening();

    // Try to load the player's data.
    if (loadData(entity)) {
      // If data is loaded successfully, return the Bukkit entity.
      return entity.getBukkitEntity();
    }

    return null;
  }

  private @NotNull ServerPlayer createNewPlayer(
      @NotNull MinecraftServer server,
      @NotNull ServerLevel worldServer,
      @NotNull final OfflinePlayer offline
  ) {
    // See net.minecraft.server.players.PlayerList#canPlayerLogin(ServerLoginPacketListenerImpl, GameProfile)
    // See net.minecraft.server.network.ServerLoginPacketListenerImpl#handleHello(ServerboundHelloPacket)
    GameProfile profile = new GameProfile(offline.getUniqueId(),
        offline.getName() != null ? offline.getName() : offline.getUniqueId().toString()
    );

    ClientInformation dummyInfo = new ClientInformation(
        "en_us",
        1, // Reduce distance just in case.
        ChatVisiblity.HIDDEN, // Don't accept chat.
        false,
        ServerPlayer.DEFAULT_MODEL_CUSTOMIZATION,
        ServerPlayer.DEFAULT_MAIN_HAND,
        true,
        false, // Don't list in player list (not that this player is in the list anyway).
        ParticleStatus.MINIMAL
    );

    ServerPlayer entity = new ServerPlayer(server, worldServer, profile, dummyInfo);

    try {
      injectPlayer(entity);
    } catch (IllegalAccessException e) {
      logger.log(
          java.util.logging.Level.WARNING,
          e,
          () -> "Unable to inject ServerPlayer, certain player data may be lost when saving!"
      );
    }

    return entity;
  }

  boolean loadData(@NotNull ServerPlayer player) {
    // See CraftPlayer#loadData

    try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(player.problemPath(), new JulLoggerAdapter(logger))) {
      ValueInput loadedData = player.server.getPlayerList().playerIo.load(player, scopedCollector).orElse(null);

      if (loadedData == null) {
        // Exceptions with loading are logged.
        return false;
      }

      // Read basic data into the player.
      player.load(loadedData);
      // Game type settings are loaded separately.
      player.loadGameTypes(loadedData);
    }

    return true;
  }

  private void injectPlayer(ServerPlayer player) throws IllegalAccessException {
    if (bukkitEntity == null) {
      return;
    }

    bukkitEntity.setAccessible(true);

    bukkitEntity.set(player, new OpenPlayer(player.server.server, player, this));
  }

  @Override
  public @NotNull Player inject(@NotNull Player player) {
    try {
      ServerPlayer nmsPlayer = getHandle(player);
      if (nmsPlayer.getBukkitEntity() instanceof OpenPlayer openPlayer) {
        return openPlayer;
      }
      injectPlayer(nmsPlayer);
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
  public @Nullable InventoryView openInventory(
      @NotNull Player bukkitPlayer, @NotNull ISpecialInventory inventory,
      boolean viewOnly
  ) {
    ServerPlayer player = getHandle(bukkitPlayer);

    if (!OpenPlayer.isConnected(player.connection)) {
      return null;
    }

    // See net.minecraft.server.level.ServerPlayer#openMenu(MenuProvider)
    AbstractContainerMenu menu;
    Component title;
    if (inventory instanceof OpenInventory playerInv) {
      menu = playerInv.createMenu(player, player.nextContainerCounter(), viewOnly);
      title = playerInv.getTitle(player);
    } else if (inventory instanceof OpenEnderChest enderChest) {
      menu = enderChest.createMenu(player, player.nextContainerCounter(), viewOnly);
      title = enderChest.getTitle();
    } else {
      return null;
    }

    // Should never happen, player is a ServerPlayer with an active connection.
    if (menu == null) {
      return null;
    }

    // Set up title. Title can only be set once for a menu, and is set during the open process.
    // Further title changes are a hack where the client is sent a "new" inventory with the same ID,
    // resulting in a title change but no other state modifications (like cursor position).
    menu.setTitle(title);

    menu = CraftEventFactory.callInventoryOpenEvent(player, menu, false);

    // Menu is null if event is cancelled.
    if (menu == null) {
      return null;
    }

    player.containerMenu = menu;
    player.connection.send(new ClientboundOpenScreenPacket(menu.containerId, menu.getType(), menu.getTitle()));
    player.initMenu(menu);

    return menu.getBukkitView();
  }

}
