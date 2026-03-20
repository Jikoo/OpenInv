package com.lishid.openinv.internal;

import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class AnySilentContainerBase implements IAnySilentContainer {

  @Override
  public boolean isAnyContainerNeeded(@NotNull Block block) {
    BlockState blockState = getState(block);

    // Barrels do not require AnyContainer.
    if (blockState instanceof Barrel) {
      return false;
    }

    // Enderchests require a non-occluding block on top to open.
    if (blockState instanceof EnderChest) {
      return block.getRelative(0, 1, 0).getType().isOccluding();
    }

    // Shulker boxes require half a block clear in the direction they open.
    if (blockState instanceof ShulkerBox) {
      return isShulkerBlocked(block);
    }

    if (!(blockState instanceof org.bukkit.block.Chest)) {
      return false;
    }

    if (isChestBlocked(block)) {
      return true;
    }

    BlockData blockData = block.getBlockData();
    if (!(blockData instanceof Chest chest) || chest.getType() == Chest.Type.SINGLE) {
      return false;
    }

    BlockFace relativeFace = switch (chest.getFacing()) {
      case NORTH -> chest.getType() == Chest.Type.RIGHT ? BlockFace.WEST : BlockFace.EAST;
      case EAST -> chest.getType() == Chest.Type.RIGHT ? BlockFace.NORTH : BlockFace.SOUTH;
      case SOUTH -> chest.getType() == Chest.Type.RIGHT ? BlockFace.EAST : BlockFace.WEST;
      case WEST -> chest.getType() == Chest.Type.RIGHT ? BlockFace.SOUTH : BlockFace.NORTH;
      default -> BlockFace.SELF;
    };
    Block relative = block.getRelative(relativeFace);

    if (relative.getType() != block.getType()) {
      return false;
    }

    BlockData relativeData = relative.getBlockData();
    if (!(relativeData instanceof Chest relativeChest)) {
      return false;
    }

    if (relativeChest.getFacing() != chest.getFacing()
        || relativeChest.getType() != (chest.getType() == Chest.Type.RIGHT ? Chest.Type.LEFT : Chest.Type.RIGHT)) {
      return false;
    }

    return isChestBlocked(relative);
  }

  @Override
  public boolean isAnySilentContainer(@NotNull Block block) {
    return isAnySilentContainer(getState(block));
  }

  @Override
  public boolean isAnySilentContainer(@NotNull Inventory inventory) {
    return isAnySilentContainer(getHolder(inventory));
  }

  protected abstract BlockState getState(@NotNull Block block);

  protected abstract InventoryHolder getHolder(@NotNull Inventory inventory);

}
