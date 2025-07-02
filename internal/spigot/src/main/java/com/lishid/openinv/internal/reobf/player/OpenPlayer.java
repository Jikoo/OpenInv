package com.lishid.openinv.internal.reobf.player;

import com.lishid.openinv.event.OpenEvents;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.craftbukkit.v1_21_R5.CraftServer;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class OpenPlayer extends CraftPlayer {

  /**
   * List of tags to always reset when saving.
   *
   * @see net.minecraft.world.entity.Entity#saveWithoutId(ValueOutput)
   * @see ServerPlayer#addAdditionalSaveData(ValueOutput)
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
      "Passengers",
      // ServerPlayer#addAdditionalSaveData(CompoundTag)
      // Intentional omissions to prevent mount loss: Attach, Entity, and RootVehicle
      "warden_spawn_tracker",
      "entered_nether_pos", // Replaces enteredNetherPosition
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
      // Player#addAdditionalSaveData(CompoundTag)
      "ShoulderEntityLeft",
      "ShoulderEntityRight",
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

  protected OpenPlayer(CraftServer server, ServerPlayer entity, PlayerManager manager) {
    super(server, entity);
    this.manager = manager;
  }

  @Override
  public void loadData() {
    manager.loadData(getHandle());
  }

  @Override
  public void saveData() {
    if (OpenEvents.saveCancelled(this)) {
      return;
    }

    ServerPlayer player = this.getHandle();
    Logger logger = LogUtils.getLogger();
    // See net.minecraft.world.level.storage.PlayerDataStorage#save(EntityHuman)
    try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(player.problemPath(), logger)) {
      PlayerDataStorage worldNBTStorage = server.getServer().getPlayerList().playerIo;

      ValueInput oldDataInput = isOnline()
          ? null
          : worldNBTStorage.load(player.getName().getString(), player.getStringUUID(), scopedCollector, player.registryAccess()).orElse(null);
      // Grab CompoundTag out of TagValueInput - we can't iterate over the fields via TagValueInput.
      CompoundTag oldData = null;
      if (oldDataInput != null) {
        Field tagValueInputInput = TagValueInput.class.getDeclaredField("input");
        tagValueInputInput.setAccessible(true);
        oldData = (CompoundTag) tagValueInputInput.get(oldDataInput);
      }

      CompoundTag playerData = getWritableTag(oldData);

      ValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, player.registryAccess());
      Field tagValueOutputOutput = TagValueOutput.class.getDeclaredField("output");
      tagValueOutputOutput.setAccessible(true);
      CompoundTag newPlayerData = (CompoundTag) tagValueOutputOutput.get(valueOutput);
      // Add existing old data.
      newPlayerData.merge(playerData);
      playerData = newPlayerData;

      player.saveWithoutId(valueOutput);

      if (oldData != null) {
        // Revert certain special data values when offline.
        revertSpecialValues(playerData, oldData);
      }

      Path playerDataDir = worldNBTStorage.getPlayerDir().toPath();
      Path tempFile = Files.createTempFile(playerDataDir, player.getStringUUID() + "-", ".dat");
      NbtIo.writeCompressed(playerData, tempFile);
      Path dataFile = playerDataDir.resolve(player.getStringUUID() + ".dat");
      Path backupFile = playerDataDir.resolve(player.getStringUUID() + ".dat_old");
      Util.safeReplaceFile(dataFile, tempFile, backupFile);
    } catch (Exception e) {
      LogUtils.getLogger().warn("Failed to save player data for {}: {}", player.getScoreboardName(), e);
    }
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

  protected void revertSpecialValues(@NotNull CompoundTag newData, @NotNull CompoundTag oldData) {
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
      container.remove(key);
    } else {
      container.put(key, data);
    }
  }

  public static boolean isConnected(@Nullable ServerGamePacketListenerImpl connection) {
    return connection != null && !connection.isDisconnected();
  }

}
