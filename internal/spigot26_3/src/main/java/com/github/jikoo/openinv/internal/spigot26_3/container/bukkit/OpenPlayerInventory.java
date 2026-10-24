package com.github.jikoo.openinv.internal.spigot26_3.container.bukkit;

import com.github.jikoo.openinv.internal.spigot26_3.container.OpenInventory;
import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

@NullMarked
public class OpenPlayerInventory extends CraftInventory implements PlayerInventory {

  public OpenPlayerInventory(OpenInventory inventory) {
    super(inventory);
  }

  @Override
  public OpenInventory getInventory() {
    return (OpenInventory) super.getInventory();
  }

  @Override
  public @Nullable ItemStack[] getContents() {
    return asCraftMirror(getInventory().getContents());
  }

  @Override
  public void setContents(@Nullable ItemStack[] items) {
    Container internal = getInventory();
    int size = internal.getContainerSize();
    Preconditions.checkArgument(items.length <= size, "items.length must be <= %s", size);

    if (items.length < size) {
      // If size matches normal player inventory size, redirect to it.
      Container ownerInternal = getInventory().getOwnerHandle().getInventory();
      if (items.length == ownerInternal.getContainerSize()) {
        internal = ownerInternal;
        size = items.length;
      }
    } else {
      // Otherwise, ensure that the display-only slots are not in use.
      for (int index = 0; index < size; ++index) {
        ItemStack itemStack = items[index];
        if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0) {
          continue;
        }
        validateSet(index);
      }
    }

    for (int index = 0; index < size; ++index) {
      if (index < items.length) {
        internal.setItem(index, CraftItemStack.asNMSCopy(items[index]));
      } else {
        internal.setItem(index, net.minecraft.world.item.ItemStack.EMPTY);
      }
    }
  }

  protected void validateSet(int index) {
    InventoryType.SlotType slotType = getInventory().getSlotType(index);
    if (slotType == InventoryType.SlotType.OUTSIDE || slotType == InventoryType.SlotType.RESULT) {
      throw new IllegalArgumentException("A plugin tried to put an item in a display slot. That would delete it!");
    }
  }

  @Override
  public void setItem(int index, @Nullable ItemStack item) {
    validateSet(index);
    super.setItem(index, item);
  }

  @Override
  public @Nullable ItemStack[] getStorageContents() {
    return asCraftMirror(getInventory().getOwnerHandle().getInventory().getNonEquipmentItems());
  }

  @Override
  public void setStorageContents(@Nullable ItemStack[] items) throws IllegalArgumentException {
    NonNullList<net.minecraft.world.item.ItemStack> list = getInventory().getOwnerHandle().getInventory().getNonEquipmentItems();
    int size = list.size();
    Preconditions.checkArgument(items.length <= size, "items.length must be <= %s", size);
    for (int index = 0; index < items.length; ++index) {
      list.set(index, CraftItemStack.asNMSCopy(items[index]));
    }
  }

  @SuppressWarnings("NonApiType")
  @Override
  public HashMap<Integer, ItemStack> all(Material material) {
    Preconditions.checkNotNull(material, "Material cannot be null");
    Item item = CraftItemType.bukkitToMinecraft(material);
    Preconditions.checkNotNull(item, "Material must be an item");

    OpenInventory inventory = getInventory();
    int size = inventory.getOwnerHandle().getInventory().getNonEquipmentItems().size();

    HashMap<Integer, ItemStack> slots = new HashMap<>();
    for (int index = 0; index < size; ++index) {
      net.minecraft.world.item.ItemStack itemStack = inventory.getItem(index);
      if (!itemStack.isEmpty() && item.equals(itemStack.getItem())) {
        slots.put(index, CraftItemStack.asCraftMirror(itemStack));
      }
    }

    return slots;
  }

  @SuppressWarnings("NonApiType")
  @Override
  public HashMap<Integer, ItemStack> all(@Nullable ItemStack item) {
    HashMap<Integer, ItemStack> slots = new HashMap<>();

    if (item == null) {
      return slots;
    }

    net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
    OpenInventory inventory = getInventory();
    int size = inventory.getOwnerHandle().getInventory().getNonEquipmentItems().size();

    for (int index = 0; index < size; ++index) {
      net.minecraft.world.item.ItemStack slotItemStack = inventory.getItem(index);
      if (ItemMatcher.EXACT.predicate.test(itemStack, slotItemStack)) {
        slots.put(index, CraftItemStack.asCraftMirror(slotItemStack));
      }
    }

    return slots;
  }

  @Override
  public int first(Material material) {
    Item item = CraftItemType.bukkitToMinecraft(material);
    Preconditions.checkNotNull(item, "Material must be an item");
    net.minecraft.world.item.ItemStack itemStack = new net.minecraft.world.item.ItemStack(item);
    int size = getInventory().getOwnerHandle().getInventory().getNonEquipmentItems().size();
    return first(itemStack, ItemMatcher.TYPE, 0, size);
  }

  @Override
  public int first(ItemStack item) {
    net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
    int size = getInventory().getOwnerHandle().getInventory().getNonEquipmentItems().size();
    return first(itemStack, ItemMatcher.EXACT, 0, size);
  }

  @Override
  public int firstEmpty() {
    OpenInventory inventory = getInventory();
    int size = inventory.getOwnerHandle().getInventory().getNonEquipmentItems().size();
    return first(net.minecraft.world.item.ItemStack.EMPTY, ItemMatcher.EMPTY, 0, size);
  }

  private int first(net.minecraft.world.item.ItemStack itemStack, ItemMatcher matcher, int startIndex, int endIndex) {
    for (int index = startIndex; index < endIndex; ++index) {
      net.minecraft.world.item.ItemStack inventoryStack = inventory.getItem(index);
      if (matcher.predicate.test(itemStack, inventoryStack)) {
        return index;
      }
    }
    return -1;
  }

  @SuppressWarnings("ImmutableEnumChecker") // Ignore Google's style preference for predicate-like enums
  private enum ItemMatcher {
    EMPTY((ignored, b) -> b.isEmpty()),
    TYPE(net.minecraft.world.item.ItemStack::isSameItem),
    SIMILAR(net.minecraft.world.item.ItemStack::isSameItemSameComponents),
    EXACT((a, b) -> net.minecraft.world.item.ItemStack.isSameItemSameComponents(a, b) && a.count() == b.count());

    private final BiPredicate<net.minecraft.world.item.ItemStack, net.minecraft.world.item.ItemStack> predicate;

    ItemMatcher(BiPredicate<net.minecraft.world.item.ItemStack, net.minecraft.world.item.ItemStack> predicate) {
      this.predicate = predicate;
    }
  }

  @SuppressWarnings("NonApiType")
  @Override
  public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
    Preconditions.checkNotNull(items, "items cannot be null");
    HashMap<Integer, ItemStack> leftover = new HashMap<>();

    OpenInventory inventory = getInventory();
    int storageSize = inventory.getOwnerHandle().getInventory().getNonEquipmentItems().size();

    for (int index = 0; index < items.length; ++index) {
      addItem(items, index, storageSize, leftover);
    }

    return leftover;
  }

  private void addItem(
      ItemStack[] items,
      int leftoverIndex,
      int storageSize,
      Map<Integer, ItemStack> leftover
  ) {
    ItemStack bukkitStack = items[leftoverIndex];
    Preconditions.checkNotNull(bukkitStack, "ItemStack cannot be null");
    if (bukkitStack.getType() == Material.AIR || bukkitStack.getAmount() <= 0) {
      return;
    }
    net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(bukkitStack);
    Preconditions.checkNotNull(nmsStack, "ItemStack cannot be empty");

    int maxStackSize = inventory.getMaxStackSize(nmsStack);
    int stackCount = nmsStack.count();

    // Try to fill existing stacks first. Waste of time for items that don't stack.
    if (maxStackSize > 1) {
      stackCount = addPartial(storageSize, nmsStack, maxStackSize, stackCount);
      if (nmsStack.count() < 1) {
        // All done adding, next item.
        return;
      }
    }

    // Add to empty slots.
    stackCount = addEmpty(storageSize, stackCount, maxStackSize, nmsStack);

    if (stackCount > 0) {
      bukkitStack.setAmount(stackCount);
      leftover.put(leftoverIndex, bukkitStack);
    }
  }

  private int addPartial(
      int storageSize,
      net.minecraft.world.item.ItemStack nmsStack,
      int maxStackSize,
      int stackCount
  ) {
    for (
        int firstPartial = first(nmsStack, ItemMatcher.SIMILAR, 0, storageSize);
        firstPartial < storageSize && firstPartial > -1;
        firstPartial = first(nmsStack, ItemMatcher.SIMILAR, firstPartial + 1, storageSize)
    ) {
      net.minecraft.world.item.ItemStack existing = inventory.getItem(firstPartial);
      int existingCount = existing.count();
      if (existingCount >= maxStackSize) {
        continue;
      }

      int fit = maxStackSize - existingCount;

      if (fit >= stackCount) {
        nmsStack.setCount(0);
        existing.setCount(existingCount + stackCount);
        return 0;
      }

      stackCount -= fit;
      nmsStack.setCount(stackCount);
      existing.setCount(maxStackSize);
    }
    return stackCount;
  }

  private int addEmpty(
      int storageSize,
      int stackCount,
      int maxStackSize,
      net.minecraft.world.item.ItemStack nmsStack
  ) {
    for (
        int firstEmpty = firstEmpty();
        firstEmpty < storageSize && firstEmpty > -1;
        firstEmpty = first(net.minecraft.world.item.ItemStack.EMPTY, ItemMatcher.EMPTY, firstEmpty, storageSize)
    ) {
      if (stackCount <= maxStackSize) {
        inventory.setItem(firstEmpty, nmsStack.copy());
        return 0;
      }

      var newStack = nmsStack.copy();
      stackCount -= maxStackSize;
      nmsStack.setCount(stackCount);
      newStack.setCount(maxStackSize);
      inventory.setItem(firstEmpty, newStack);
    }

    return stackCount;
  }

  @SuppressWarnings("NonApiType")
  @Override
  public HashMap<Integer, ItemStack> removeItem(ItemStack... items) {
    Preconditions.checkNotNull(items, "items cannot be null");
    HashMap<Integer, ItemStack> leftover = new HashMap<>();

    OpenInventory inventory = getInventory();
    int storageSize = inventory.getOwnerHandle().getInventory().getNonEquipmentItems().size();

    for (int index = 0; index < items.length; ++index) {
      removeItem(items[index], index, storageSize, leftover);
    }

    return leftover;
  }

  private void removeItem(
      ItemStack bukkitStack,
      int leftoverIndex,
      int storageSize,
      Map<Integer, ItemStack> leftover
  ) {
    Preconditions.checkNotNull(bukkitStack, "ItemStack cannot be null");
    if (bukkitStack.getType() == Material.AIR || bukkitStack.getAmount() <= 0) {
      return;
    }
    net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(bukkitStack);
    Preconditions.checkNotNull(nmsStack, "ItemStack cannot be empty");

    int stackCount = nmsStack.count();

    for (
        int first = first(nmsStack, ItemMatcher.SIMILAR, 0, storageSize);
        first < storageSize && first > -1;
        first = first(nmsStack, ItemMatcher.SIMILAR, first + 1, storageSize)
    ) {
      net.minecraft.world.item.ItemStack existing = inventory.getItem(first);
      int existingCount = existing.count();
      if (existingCount > stackCount) {
        existing.setCount(existingCount - stackCount);
        bukkitStack.setAmount(0);
        return;
      }

      stackCount -= existingCount;
      inventory.setItem(first, net.minecraft.world.item.ItemStack.EMPTY);

      if (stackCount <= 0) {
        break;
      }
    }

    bukkitStack.setAmount(stackCount);

    if (stackCount > 0) {
      leftover.put(leftoverIndex, bukkitStack);
    }
  }

  @Override
  public void remove(Material material) {
    Item item = CraftItemType.bukkitToMinecraft(material);
    Preconditions.checkNotNull(item, "Material must be an item");

    if (!item.equals(Items.AIR)) {
      removeAll(new net.minecraft.world.item.ItemStack(item), ItemMatcher.SIMILAR);
    }
  }

  @Override
  public void remove(ItemStack bukkitStack) {
    net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(bukkitStack);

    if (!nmsStack.isEmpty()) {
      removeAll(nmsStack, ItemMatcher.EXACT);
    }
  }

  private void removeAll(net.minecraft.world.item.ItemStack itemStack, ItemMatcher matcher) {
    int storageSize = getInventory().getOwnerHandle().getInventory().getNonEquipmentItems().size();
    for (int index = 0; index < storageSize; ++index) {
      net.minecraft.world.item.ItemStack content = inventory.getItem(index);
      if (matcher.predicate.test(itemStack, content)) {
        inventory.setItem(index, net.minecraft.world.item.ItemStack.EMPTY);
      }
    }
  }

  @Override
  public InventoryType getType() {
    return InventoryType.PLAYER;
  }

  @Override
  public Player getHolder() {
    return getInventory().getOwner();
  }

  @Override
  public ItemStack[] getArmorContents() {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getArmorContents();
  }

  @Override
  public void setArmorContents(@Nullable ItemStack[] items) {
    getInventory().getOwnerHandle().getBukkitEntity().getInventory().setArmorContents(items);
  }

  @Override
  public ItemStack[] getExtraContents() {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getExtraContents();
  }

  @Override
  public void setExtraContents(@Nullable ItemStack[] items) {
    getInventory().getOwnerHandle().getBukkitEntity().getInventory().setExtraContents(items);
  }

  @Override
  public @Nullable ItemStack getHelmet() {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getHelmet();
  }

  @Override
  public void setHelmet(@Nullable ItemStack helmet) {
    getInventory().getOwnerHandle().getBukkitEntity().getInventory().setHelmet(helmet);
  }

  @Override
  public @Nullable ItemStack getChestplate() {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getChestplate();
  }

  @Override
  public void setChestplate(@Nullable ItemStack chestplate) {
    getInventory().getOwnerHandle().getBukkitEntity().getInventory().setChestplate(chestplate);
  }

  @Override
  public @Nullable ItemStack getLeggings() {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getLeggings();
  }

  @Override
  public void setLeggings(@Nullable ItemStack leggings) {
    getInventory().getOwner().getInventory().setLeggings(leggings);
  }

  @Override
  public @Nullable ItemStack getBoots() {
    return getInventory().getOwner().getInventory().getBoots();
  }

  @Override
  public void setBoots(@Nullable ItemStack boots) {
    getInventory().getOwner().getInventory().setBoots(boots);
  }

  @Override
  public ItemStack getItemInMainHand() {
    return getInventory().getOwner().getInventory().getItemInMainHand();
  }

  @Override
  public void setItemInMainHand(@Nullable ItemStack item) {
    getInventory().getOwner().getInventory().setItemInMainHand(item);
  }

  @Override
  public ItemStack getItemInOffHand() {
    return getInventory().getOwner().getInventory().getItemInOffHand();
  }

  @Override
  public void setItemInOffHand(@Nullable ItemStack item) {
    getInventory().getOwner().getInventory().setItemInOffHand(item);
  }

  @SuppressWarnings("InlineMeSuggester")
  @Deprecated
  @Override
  public ItemStack getItemInHand() {
    return getItemInMainHand();
  }

  @SuppressWarnings("InlineMeSuggester")
  @Deprecated
  @Override
  public void setItemInHand(@Nullable ItemStack stack) {
    setItemInMainHand(stack);
  }

  @Override
  public int getHeldItemSlot() {
    Inventory internal = getInventory().getOwnerHandle().getInventory();
    return internal.getNonEquipmentItems().size() - 9 + internal.getSelectedSlot();
  }

  @Override
  public void setHeldItemSlot(int slot) {
    slot %= 9;
    getInventory().getOwnerHandle().getInventory().setSelectedSlot(slot);
  }

  @Override
  public @Nullable ItemStack getItem(org.bukkit.inventory.EquipmentSlot slot) {
    return switch (slot) {
      case HAND -> getItemInMainHand();
      case OFF_HAND -> getItemInOffHand();
      case FEET -> getBoots();
      case LEGS -> getLeggings();
      case CHEST -> getChestplate();
      case HEAD -> getHelmet();
      default -> throw new IllegalArgumentException("Unsupported EquipmentSlot " + slot);
    };
  }

  @Override
  public void setItem(org.bukkit.inventory.EquipmentSlot slot, @Nullable ItemStack item) {
    switch (slot) {
      case HAND -> setItemInMainHand(item);
      case OFF_HAND -> setItemInOffHand(item);
      case FEET -> setBoots(item);
      case LEGS -> setLeggings(item);
      case CHEST -> setChestplate(item);
      case HEAD -> setHelmet(item);
      default -> throw new IllegalArgumentException("Unsupported EquipmentSlot " + slot);
    }
  }

}
