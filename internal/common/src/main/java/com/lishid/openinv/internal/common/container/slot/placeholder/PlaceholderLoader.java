package com.lishid.openinv.internal.common.container.slot.placeholder;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;

public class PlaceholderLoader extends PlaceholderLoaderBase {

  private static final CustomModelData DEFAULT_CUSTOM_MODEL_DATA = new CustomModelData(List.of(), List.of(), List.of("openinv:custom"), List.of());
  private static final TooltipDisplay HIDE_TOOLTIP = new TooltipDisplay(true, new LinkedHashSet<>());

  @Override
  protected @NotNull CompoundTag parseTag(@NotNull String itemText) throws Exception {
    return TagParser.parseCompoundFully(itemText);
  }

  @Override
  protected void addModelData(@NotNull ItemStack itemStack) {
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
  }

  @Override
  protected void hideTooltip(@NotNull ItemStack itemStack) {
    itemStack.set(DataComponents.TOOLTIP_DISPLAY, HIDE_TOOLTIP);
  }

  @Override
  protected DyedItemColor getDye(int rgb) {
    return new DyedItemColor(rgb);
  }

}
