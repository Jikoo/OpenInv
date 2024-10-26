package com.lishid.openinv.internal.v1_21_R2.container;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_21_R2.CraftRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

public final class Placeholders {

  private static final CustomModelData DEFAULT_CUSTOM_MODEL_DATA = new CustomModelData(9999);
  public static final @NotNull EnumMap<GameType, ItemStack> BLOCKED_GAME_TYPE = new EnumMap<>(GameType.class);
  public static @NotNull ItemStack craftingOutput = defaultCraftingOutput();
  public static @NotNull ItemStack cursor = defaultCursor();
  public static @NotNull ItemStack drop = defaultDrop();
  public static @NotNull ItemStack emptyHelmet = getEmptyArmor(Items.LEATHER_HELMET);
  public static @NotNull ItemStack emptyChestplate = getEmptyArmor(Items.LEATHER_CHESTPLATE);
  public static @NotNull ItemStack emptyLeggings = getEmptyArmor(Items.LEATHER_LEGGINGS);
  public static @NotNull ItemStack emptyBoots = getEmptyArmor(Items.LEATHER_BOOTS);
  public static @NotNull ItemStack emptyOffHand = getEmptyShield();
  public static @NotNull ItemStack notSlot = defaultNotSlot();
  public static @NotNull ItemStack blockedOffline = defaultBlockedOffline();

  static {
    for (GameType type : GameType.values()) {
      // Barrier: "Not available - Creative" etc.
      ItemStack typeItem = new ItemStack(Items.BARRIER);
      typeItem.set(
          DataComponents.ITEM_NAME,
          Component.translatable("options.narrator.notavailable").append(" - ").append(type.getShortDisplayName()));
      BLOCKED_GAME_TYPE.put(type, typeItem);
    }
  }

  public static void load(@NotNull ConfigurationSection section) throws Exception {
    craftingOutput = parse(section, "crafting-output", craftingOutput);
    cursor = parse(section, "cursor", cursor);
    drop = parse(section, "drop", drop);
    emptyHelmet = parse(section, "empty-helmet", emptyHelmet);
    emptyChestplate = parse(section, "empty-chestplate", emptyChestplate);
    emptyLeggings = parse(section, "empty-leggings", emptyLeggings);
    emptyBoots = parse(section, "empty-boots", emptyBoots);
    emptyOffHand = parse(section, "empty-off-hand", emptyOffHand);
    notSlot = parse(section, "not-a-slot", notSlot);
    blockedOffline = parse(section, "blocked.offline", blockedOffline);
    BLOCKED_GAME_TYPE.put(GameType.CREATIVE, parse(section, "blocked.creative", BLOCKED_GAME_TYPE.get(GameType.CREATIVE)));
    BLOCKED_GAME_TYPE.put(GameType.SPECTATOR, parse(section, "blocked.spectator", BLOCKED_GAME_TYPE.get(GameType.SPECTATOR)));
  }

  private static @NotNull ItemStack parse(
      @NotNull ConfigurationSection section,
      @NotNull String path,
      @NotNull ItemStack defaultStack) throws Exception {
    String itemText = section.getString(path);

    if (itemText == null) {
      return defaultStack;
    }

    CompoundTag compoundTag = TagParser.parseTag(itemText);
    Optional<ItemStack> parsed = ItemStack.parse(CraftRegistry.getMinecraftRegistry(), compoundTag);
    return parsed.filter(itemStack -> !itemStack.isEmpty()).orElse(defaultStack);
  }

  public static ItemStack survivalOnly(@NotNull ServerPlayer serverPlayer) {
    if (serverPlayer.connection == null || serverPlayer.connection.isDisconnected()) {
      return blockedOffline;
    }

    return BLOCKED_GAME_TYPE.getOrDefault(serverPlayer.gameMode.getGameModeForPlayer(), ItemStack.EMPTY);
  }

  private static ItemStack defaultCraftingOutput() {
    // Crafting table: "Crafting"
    ItemStack itemStack = new ItemStack(Items.CRAFTING_TABLE);
    itemStack.set(DataComponents.ITEM_NAME, Component.translatable("container.crafting"));
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
    return itemStack;
  }

  private static ItemStack defaultCursor() {
    // Cursor-like banner with no tooltip
    ItemStack itemStack = new ItemStack(Items.WHITE_BANNER);
    RegistryAccess minecraftRegistry = CraftRegistry.getMinecraftRegistry();
    Registry<BannerPattern> bannerPatterns = minecraftRegistry.lookupOrThrow(Registries.BANNER_PATTERN);
    BannerPattern halfDiagBottomRight = bannerPatterns.getOrThrow(BannerPatterns.DIAGONAL_RIGHT).value();
    BannerPattern downRight = bannerPatterns.getOrThrow(BannerPatterns.STRIPE_DOWNRIGHT).value();
    BannerPattern border = bannerPatterns.getOrThrow(BannerPatterns.BORDER).value();
    itemStack.set(DataComponents.BANNER_PATTERNS,
        new BannerPatternLayers(List.of(
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(halfDiagBottomRight), DyeColor.GRAY),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(downRight), DyeColor.WHITE),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(border), DyeColor.GRAY))));
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
    itemStack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
    return itemStack;
  }

  private static ItemStack defaultDrop() {
    // Dropper: "Drop Selected Item"
    ItemStack itemStack = new ItemStack(Items.DROPPER);
    // Note: translatable component, not keybind component! We want the text identifying the keybind, not the key.
    itemStack.set(DataComponents.ITEM_NAME, Component.translatable("key.drop"));
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
    return itemStack;
  }

  private static ItemStack getEmptyArmor(ItemLike item) {
    // Inventory-background-grey-ish leather armor with no tooltip
    ItemStack itemStack = new ItemStack(item);
    DyedItemColor color = new DyedItemColor(0xC8C8C8, false);
    itemStack.set(DataComponents.DYED_COLOR, color);
    itemStack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
    return itemStack;
  }

  private static ItemStack getEmptyShield() {
    ItemStack itemStack = new ItemStack(Items.SHIELD);
    itemStack.set(DataComponents.BASE_COLOR, DyeColor.MAGENTA);
    RegistryAccess minecraftRegistry = CraftRegistry.getMinecraftRegistry();
    Registry<BannerPattern> bannerPatterns = minecraftRegistry.lookupOrThrow(Registries.BANNER_PATTERN);
    BannerPattern halfLeft = bannerPatterns.getOrThrow(BannerPatterns.HALF_VERTICAL).value();
    BannerPattern topLeft = bannerPatterns.getOrThrow(BannerPatterns.SQUARE_TOP_LEFT).value();
    BannerPattern topRight = bannerPatterns.getOrThrow(BannerPatterns.SQUARE_TOP_RIGHT).value();
    BannerPattern bottomLeft = bannerPatterns.getOrThrow(BannerPatterns.SQUARE_BOTTOM_LEFT).value();
    BannerPattern bottomRight = bannerPatterns.getOrThrow(BannerPatterns.SQUARE_BOTTOM_RIGHT).value();
    itemStack.set(DataComponents.BANNER_PATTERNS,
        new BannerPatternLayers(List.of(
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(halfLeft), DyeColor.BLACK),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(topLeft), DyeColor.MAGENTA),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(bottomLeft), DyeColor.MAGENTA),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(topRight), DyeColor.BLACK),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(bottomRight), DyeColor.BLACK))));
    itemStack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
    return itemStack;
  }

  private static ItemStack defaultNotSlot() {
    // White pane with no tooltip
    ItemStack itemStack = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
    itemStack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
    return itemStack;
  }

  private static ItemStack defaultBlockedOffline() {
    // Barrier: "Not available - Offline"
    ItemStack itemStack = new ItemStack(Items.BARRIER);
    itemStack.set(DataComponents.ITEM_NAME,
        Component.translatable("options.narrator.notavailable")
            .append(Component.literal(" - "))
            .append(Component.translatable("gui.socialInteractions.status_offline")));
    return itemStack;
  }

  private Placeholders() {
    throw new IllegalStateException("Cannot create instance of utility class.");
  }

}
