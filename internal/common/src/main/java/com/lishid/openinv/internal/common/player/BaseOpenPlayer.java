package com.lishid.openinv.internal.common.player;

import com.lishid.openinv.event.OpenEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public abstract class BaseOpenPlayer extends CraftPlayer {

  /**
   * List of tags to always reset when saving. These are items that do not get written
   * if unset or empty, resulting in older values not being clobbered appropriately.
   *
   * @see net.minecraft.world.entity.Entity#saveWithoutId(ValueOutput, boolean, boolean, boolean)
   * @see net.minecraft.server.level.ServerPlayer#addAdditionalSaveData(ValueOutput)
   * @see net.minecraft.world.entity.player.Player#addAdditionalSaveData(ValueOutput)
   * @see net.minecraft.world.entity.LivingEntity#addAdditionalSaveData(ValueOutput)
   */
  @Unmodifiable
  protected static final Set<String> RESET_TAGS = Set.of(
      // Entity#saveWithoutId(CompoundTag)
      "CustomName",
      "CustomNameVisible",
      "Silent",
      "NoGravity",
      "Glowing",
      "TicksFrozen",
      "HasVisualFire",
      "Tags",
      "data",
      "Passengers",
      // ServerPlayer#addAdditionalSaveData(CompoundTag)
      // Intentional omissions to prevent mount loss: Attach, Entity, and RootVehicle
      "warden_spawn_tracker", // No longer needed as of 1.21.11
      "entered_nether_pos", // Replaces enteredNetherPosition as of 1.21.6
      "enteredNetherPosition",
      "respawn", // Replaces SpawnXyz fields as of 1.21.6
      "SpawnX",
      "SpawnY",
      "SpawnZ",
      "SpawnForced",
      "SpawnAngle",
      "SpawnDimension",
      "raid_omen_position",
      "ender_pearls",
      "ShoulderEntityLeft",
      "ShoulderEntityRight",
      // Player#addAdditionalSaveData(CompoundTag)
      "LastDeathLocation",
      "current_explosion_impact_pos",
      // LivingEntity#addAdditionalSaveData(CompoundTag)
      "active_effects",
      "sleeping_pos", // Replaces SleepingXyz fields as of 1.21.6
      "SleepingX",
      "SleepingY",
      "SleepingZ",
      "Brain",
      "last_hurt_by_player",
      "last_hurt_by_player_memory_time",
      "last_hurt_by_mob",
      "ticks_since_last_hurt_by_mob",
      "equipment",
      "locator_bar_icon"
  );

  private final PlayerManager manager;

  protected BaseOpenPlayer(CraftServer server, ServerPlayer entity, PlayerManager manager) {
    super(server, entity);
    this.manager = manager;
  }

  @Override
  public void loadData() {
    manager.loadData(server.getServer(), getHandle());
  }

  @Override
  public void saveData() {
    if (OpenEvents.saveCancelled(this)) {
      return;
    }

    trySave(this.getHandle());
  }

  protected abstract void trySave(ServerPlayer player);

  protected void saveSafe(
      @NotNull ServerPlayer player,
      @Nullable CompoundTag oldData,
      @NotNull CompoundTag playerData,
      @NotNull PlayerDataStorage worldNbtStorage
  ) throws IOException {
    // Revert certain special data values when offline.
    revertSpecialValues(playerData, oldData);

    Path playerDataDir = worldNbtStorage.getPlayerDir().toPath();
    Path tempFile = Files.createTempFile(playerDataDir, player.getStringUUID() + "-", ".dat");
    NbtIo.writeCompressed(playerData, tempFile);
    Path dataFile = playerDataDir.resolve(player.getStringUUID() + ".dat");
    Path backupFile = playerDataDir.resolve(player.getStringUUID() + ".dat_old");
    safeReplaceFile(dataFile, tempFile, backupFile);
  }

  protected void safeReplaceFile(
      @NotNull Path dataFile,
      @NotNull Path tempFile,
      @NotNull Path backupFile
  ) {
    net.minecraft.util.Util.safeReplaceFile(dataFile, tempFile, backupFile);
  }

  @Contract("null -> new")
  protected @NotNull CompoundTag getWritableTag(@Nullable CompoundTag oldData) {
    if (oldData == null) {
      return new CompoundTag();
    }

    // Copy old data. This is a deep clone, so operating on it should be safe.
    oldData = oldData.copy();

    // Remove vanilla/server data that is not written every time.
    oldData.keySet().removeIf(
        key -> RESET_TAGS.contains(key)
            || key.startsWith("Bukkit")
            || (key.startsWith("Paper") && key.length() > 5)
    );

    return oldData;
  }

  protected void revertSpecialValues(@NotNull CompoundTag newData, @Nullable CompoundTag oldData) {
    if (oldData == null) {
      return;
    }

    // Revert automatic updates to play timestamps.
    copyValue(oldData, newData, "bukkit", "lastPlayed", NumericTag.class);
    copyValue(oldData, newData, "Paper", "LastSeen", NumericTag.class);
    copyValue(oldData, newData, "Paper", "LastLogin", NumericTag.class);
  }

  private <T extends Tag> void copyValue(
      @NotNull CompoundTag source,
      @NotNull CompoundTag target,
      @NotNull String container,
      @NotNull String key,
      @SuppressWarnings("SameParameterValue") @NotNull Class<T> tagType
  ) {
    CompoundTag oldContainer = getTag(source, container, CompoundTag.class);
    CompoundTag newContainer = getTag(target, container, CompoundTag.class);

    // New container being null means the server implementation doesn't store this data.
    if (newContainer == null) {
      return;
    }

    // If old tag exists, copy it to new location, removing otherwise.
    setTag(newContainer, key, getTag(oldContainer, key, tagType));
  }

  private <T extends Tag> @Nullable T getTag(
      @Nullable CompoundTag container,
      @NotNull String key,
      @NotNull Class<T> dataType
  ) {
    if (container == null) {
      return null;
    }
    Tag value = container.get(key);
    if (value == null || !dataType.isAssignableFrom(value.getClass())) {
      return null;
    }
    return dataType.cast(value);
  }

  private <T extends Tag> void setTag(
      @NotNull CompoundTag container,
      @NotNull String key,
      @Nullable T data
  ) {
    if (data == null) {
      remove(container, key);
    } else {
      container.put(key, data);
    }
  }

  protected abstract void remove(@NotNull CompoundTag tag, @NotNull String key);

  public static boolean isConnected(@Nullable ServerGamePacketListenerImpl connection) {
    return connection != null && !connection.isDisconnected();
  }

}
