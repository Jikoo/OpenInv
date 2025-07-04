package com.lishid.openinv.internal.common.container.slot.placeholder;

import com.mojang.serialization.DataResult;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PlaceholderLoaderBase {

  public void load(@Nullable ConfigurationSection section) throws Exception {
    Placeholders.craftingOutput = parse(section, "crafting-output", defaultCraftingOutput());
    Placeholders.cursor = parse(section, "cursor", defaultCursor());
    Placeholders.drop = parse(section, "drop", defaultDrop());
    Placeholders.emptyHelmet = parse(section, "empty-helmet", getEmptyArmor(Items.LEATHER_HELMET));
    Placeholders.emptyChestplate = parse(section, "empty-chestplate", getEmptyArmor(Items.LEATHER_CHESTPLATE));
    Placeholders.emptyLeggings = parse(section, "empty-leggings", getEmptyArmor(Items.LEATHER_LEGGINGS));
    Placeholders.emptyBoots = parse(section, "empty-boots", getEmptyArmor(Items.LEATHER_BOOTS));
    Placeholders.emptyOffHand = parse(section, "empty-off-hand", defaultShield());
    Placeholders.notSlot = parse(section, "not-a-slot", defaultNotSlot());
    Placeholders.blockedOffline = parse(section, "blocked.offline", defaultBlockedOffline());

    for (GameType type : GameType.values()) {
      // Barrier: "Not available - Creative" etc.
      ItemStack typeItem = new ItemStack(Items.BARRIER);
      typeItem.set(
          DataComponents.ITEM_NAME,
          Component.translatable("options.narrator.notavailable").append(" - ").append(type.getShortDisplayName())
      );
      Placeholders.BLOCKED_GAME_TYPE.put(type, typeItem);
    }

    Placeholders.BLOCKED_GAME_TYPE.put(GameType.CREATIVE, parse(section, "blocked.creative", Placeholders.BLOCKED_GAME_TYPE.get(GameType.CREATIVE)));
    Placeholders.BLOCKED_GAME_TYPE.put(GameType.SPECTATOR, parse(section, "blocked.spectator", Placeholders.BLOCKED_GAME_TYPE.get(GameType.SPECTATOR)));
  }

  protected @NotNull ItemStack parse(
      @Nullable ConfigurationSection section,
      @NotNull String path,
      @NotNull ItemStack defaultStack
  ) throws Exception {
    if (section == null) {
      return defaultStack;
    }

    String itemText = section.getString(path);

    if (itemText == null) {
      return defaultStack;
    }

    CompoundTag compoundTag = parseTag(itemText);
    DataResult<ItemStack> parsed = ItemStack.CODEC.parse(CraftRegistry.getMinecraftRegistry().createSerializationContext(NbtOps.INSTANCE), compoundTag);
    ItemStack itemStack;
    try {
      itemStack = parsed.getOrThrow();
    } catch (Exception e) {
      itemStack = null;
    }
    return itemStack == null ? defaultStack : itemStack;
  }

  protected abstract @NotNull CompoundTag parseTag(@NotNull String itemText) throws Exception;

  protected abstract void addModelData(@NotNull ItemStack itemStack);

  protected abstract void hideTooltip(@NotNull ItemStack itemStack);

  protected abstract DyedItemColor getDye(int rgb);

  protected @NotNull ItemStack defaultCraftingOutput() {
    // Crafting table: "Crafting"
    ItemStack itemStack = new ItemStack(Items.CRAFTING_TABLE);
    itemStack.set(DataComponents.ITEM_NAME, Component.translatable("container.crafting"));
    addModelData(itemStack);
    return itemStack;
  }

  protected @NotNull ItemStack defaultCursor() {
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
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(border), DyeColor.GRAY)
        ))
    );
    addModelData(itemStack);
    hideTooltip(itemStack);
    return itemStack;
  }

  protected @NotNull ItemStack defaultDrop() {
    // Dropper: "Drop Selected Item"
    ItemStack itemStack = new ItemStack(Items.DROPPER);
    // Note: translatable component, not keybind component! We want the text identifying the keybind, not the key.
    itemStack.set(DataComponents.ITEM_NAME, Component.translatable("key.drop"));
    addModelData(itemStack);
    return itemStack;
  }

  protected @NotNull ItemStack getEmptyArmor(@NotNull ItemLike item) {
    // Inventory-background-grey-ish leather armor with no tooltip
    ItemStack itemStack = new ItemStack(item);
    DyedItemColor color = getDye(0xC8C8C8);
    itemStack.set(DataComponents.DYED_COLOR, color);
    hideTooltip(itemStack);
    addModelData(itemStack);
    return itemStack;
  }

  protected @NotNull ItemStack defaultShield() {
    // Shield with "missing texture" pattern, magenta and black squares.
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
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(bottomRight), DyeColor.BLACK)
        ))
    );
    hideTooltip(itemStack);
    addModelData(itemStack);
    return itemStack;
  }

  protected @NotNull ItemStack defaultNotSlot() {
    // White pane with no tooltip
    ItemStack itemStack = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
    hideTooltip(itemStack);
    addModelData(itemStack);
    return itemStack;
  }

  protected @NotNull ItemStack defaultBlockedOffline() {
    // Barrier: "Not available - Offline"
    ItemStack itemStack = new ItemStack(Items.BARRIER);
    itemStack.set(DataComponents.ITEM_NAME,
        Component.translatable("options.narrator.notavailable")
            .append(Component.literal(" - "))
            .append(Component.translatable("gui.socialInteractions.status_offline"))
    );
    return itemStack;
  }

}
