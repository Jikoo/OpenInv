package com.lishid.openinv.internal.paper1_21_4.container.slot.placeholder;

import com.lishid.openinv.internal.common.container.slot.placeholder.PlaceholderLoaderBase;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaceholderLoader extends PlaceholderLoaderBase {

  private static final CustomModelData DEFAULT_CUSTOM_MODEL_DATA = new CustomModelData(List.of(), List.of(), List.of("openinv:custom"), List.of());

  @Override
  protected @NotNull CompoundTag parseTag(@NotNull String itemText) throws Exception {
    return TagParser.parseTag(itemText);
  }

  @Override
  protected void addModelData(@NotNull ItemStack itemStack) {
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
  }

  @Override
  protected void hideTooltip(@NotNull ItemStack itemStack) {
    itemStack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
  }

  @Override
  protected DyedItemColor getDye(int rgb) {
    return new DyedItemColor(rgb, false);
  }

}
