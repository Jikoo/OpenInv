package com.lishid.openinv.internal.paper1_21_1.container.slot.placeholder;

import com.lishid.openinv.internal.paper1_21_3.container.slot.placeholder.NumericDataPlaceholderLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.bukkit.craftbukkit.CraftRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaceholderLoader extends NumericDataPlaceholderLoader {

  @Override
  protected @NotNull ItemStack defaultCursor() {
    // Cursor-like banner with no tooltip
    ItemStack itemStack = new ItemStack(Items.WHITE_BANNER);
    RegistryAccess minecraftRegistry = CraftRegistry.getMinecraftRegistry();
    Registry<BannerPattern> bannerPatterns = minecraftRegistry.registryOrThrow(Registries.BANNER_PATTERN);
    BannerPattern halfDiagBottomRight = bannerPatterns.getOrThrow(BannerPatterns.DIAGONAL_RIGHT);
    BannerPattern downRight = bannerPatterns.getOrThrow(BannerPatterns.STRIPE_DOWNRIGHT);
    BannerPattern border = bannerPatterns.getOrThrow(BannerPatterns.BORDER);
    itemStack.set(DataComponents.BANNER_PATTERNS,
        new BannerPatternLayers(List.of(
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(halfDiagBottomRight), DyeColor.GRAY),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(downRight), DyeColor.WHITE),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(border), DyeColor.GRAY))));
    addModelData(itemStack);
    itemStack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
    return itemStack;
  }

  @Override
  protected @NotNull ItemStack defaultShield() {
    // Shield with "missing texture" pattern, magenta and black squares.
    ItemStack itemStack = new ItemStack(Items.SHIELD);
    itemStack.set(DataComponents.BASE_COLOR, DyeColor.MAGENTA);
    RegistryAccess minecraftRegistry = CraftRegistry.getMinecraftRegistry();
    Registry<BannerPattern> bannerPatterns = minecraftRegistry.registryOrThrow(Registries.BANNER_PATTERN);
    BannerPattern halfLeft = bannerPatterns.getOrThrow(BannerPatterns.HALF_VERTICAL);
    BannerPattern topLeft = bannerPatterns.getOrThrow(BannerPatterns.SQUARE_TOP_LEFT);
    BannerPattern topRight = bannerPatterns.getOrThrow(BannerPatterns.SQUARE_TOP_RIGHT);
    BannerPattern bottomLeft = bannerPatterns.getOrThrow(BannerPatterns.SQUARE_BOTTOM_LEFT);
    BannerPattern bottomRight = bannerPatterns.getOrThrow(BannerPatterns.SQUARE_BOTTOM_RIGHT);
    itemStack.set(DataComponents.BANNER_PATTERNS,
        new BannerPatternLayers(List.of(
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(halfLeft), DyeColor.BLACK),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(topLeft), DyeColor.MAGENTA),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(bottomLeft), DyeColor.MAGENTA),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(topRight), DyeColor.BLACK),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(bottomRight), DyeColor.BLACK))));
    itemStack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
    addModelData(itemStack);
    return itemStack;
  }

}
