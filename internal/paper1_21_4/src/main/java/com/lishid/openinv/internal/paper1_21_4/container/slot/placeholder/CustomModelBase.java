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

public class CustomModelBase extends PlaceholderLoaderBase {

  private final @NotNull CustomModelData defaultCustomModelData;

  public CustomModelBase(@NotNull CustomModelData defaultCustomModelData) {
    this.defaultCustomModelData = defaultCustomModelData;
  }

  @Override
  protected @NotNull CompoundTag parseTag(@NotNull String itemText) throws Exception {
    return TagParser.parseTag(itemText);
  }

  @Override
  protected void addModelData(@NotNull ItemStack itemStack) {
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, defaultCustomModelData);
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
