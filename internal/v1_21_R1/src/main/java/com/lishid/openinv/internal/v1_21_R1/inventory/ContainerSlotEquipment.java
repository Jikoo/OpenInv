package com.lishid.openinv.internal.v1_21_R1.inventory;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.bukkit.craftbukkit.v1_21_R1.CraftRegistry;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A slot for equipment that displays placeholders if empty.
 */
class ContainerSlotEquipment extends ContainerSlotList {

  private static final ItemStack HELMET;
  private static final ItemStack CHESTPLATE;
  private static final ItemStack LEGGINGS;
  private static final ItemStack BOOTS;
  private static final ItemStack SHIELD;

  static {
    HELMET = new ItemStack(Items.LEATHER_HELMET);
    // Inventory-background-grey-ish
    DyedItemColor color = new DyedItemColor(0xC8C8C8, false);
    HELMET.set(DataComponents.DYED_COLOR, color);
    HELMET.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);

    CHESTPLATE = new ItemStack(Items.LEATHER_CHESTPLATE);
    CHESTPLATE.set(DataComponents.DYED_COLOR, color);
    CHESTPLATE.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);

    LEGGINGS = new ItemStack(Items.LEATHER_LEGGINGS);
    LEGGINGS.set(DataComponents.DYED_COLOR, color);
    LEGGINGS.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);

    BOOTS = new ItemStack(Items.LEATHER_BOOTS);
    BOOTS.set(DataComponents.DYED_COLOR, color);
    BOOTS.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);

    SHIELD = new ItemStack(Items.SHIELD);
    SHIELD.set(DataComponents.BASE_COLOR, DyeColor.MAGENTA);
    RegistryAccess minecraftRegistry = CraftRegistry.getMinecraftRegistry();
    Registry<BannerPattern> bannerPatterns = minecraftRegistry.registryOrThrow(Registries.BANNER_PATTERN);
    BannerPattern halfLeft = bannerPatterns.getOrThrow(BannerPatterns.HALF_VERTICAL);
    BannerPattern topLeft = bannerPatterns.getOrThrow(BannerPatterns.SQUARE_TOP_LEFT);
    BannerPattern topRight = bannerPatterns.getOrThrow(BannerPatterns.SQUARE_TOP_RIGHT);
    BannerPattern bottomLeft = bannerPatterns.getOrThrow(BannerPatterns.SQUARE_BOTTOM_LEFT);
    BannerPattern bottomRight = bannerPatterns.getOrThrow(BannerPatterns.SQUARE_BOTTOM_RIGHT);
    SHIELD.set(DataComponents.BANNER_PATTERNS,
        new BannerPatternLayers(List.of(
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(halfLeft), DyeColor.BLACK),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(topLeft), DyeColor.MAGENTA),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(bottomLeft), DyeColor.MAGENTA),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(topRight), DyeColor.BLACK),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(bottomRight), DyeColor.BLACK))));
    SHIELD.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
  }

  private final ItemStack placeholder;
  private final EquipmentSlot equipmentSlot;

  ContainerSlotEquipment(ServerPlayer holder, int index, EquipmentSlot equipmentSlot) {
    super(holder, index, InventoryType.SlotType.ARMOR);
    placeholder = switch (equipmentSlot) {
      case HEAD -> HELMET;
      case CHEST -> CHESTPLATE;
      case LEGS -> LEGGINGS;
      case FEET -> BOOTS;
      default -> SHIELD;
    };
    this.equipmentSlot = equipmentSlot;
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.items = holder.getInventory().armor;
  }

  @Override
  public Slot asMenuSlot(Container container, int index, int x, int y) {
    return new SlotEquipment(container, index, x, y);
  }

  class SlotEquipment extends MenuSlotPlaceholder {

    SlotEquipment(Container container, int index, int x, int y) {
      super(container, index, x, y);
    }

    @Override
    ItemStack getOrDefault() {
      ItemStack itemStack = getItem();
      if (!itemStack.isEmpty()) {
        return itemStack;
      }
      return placeholder;
    }

    EquipmentSlot getEquipmentSlot() {
      return equipmentSlot;
    }

  }

}
