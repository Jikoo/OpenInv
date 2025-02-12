package com.lishid.openinv.internal.paper1_21_3.container.slot.placeholder;

import com.lishid.openinv.internal.common.container.slot.placeholder.PlaceholderLoaderBase;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.bukkit.craftbukkit.CraftRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NumericDataPlaceholderLoader extends PlaceholderLoaderBase {

  private static final CustomModelData DEFAULT_CUSTOM_MODEL_DATA = new CustomModelData(9999);

  @Override
  protected void addModelData(ItemStack itemStack) {
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
  }

}
