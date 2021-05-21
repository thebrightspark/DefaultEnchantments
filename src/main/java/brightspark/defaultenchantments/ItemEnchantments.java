package brightspark.defaultenchantments;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.LinkedList;
import java.util.List;

public class ItemEnchantments implements INBTSerializable<NBTTagCompound> {
	private List<SingleItem> items;
	private List<SingleEnchantment> enchantments;

	public ItemEnchantments(List<SingleItem> items, List<SingleEnchantment> enchantments) {
		this.items = items;
		this.enchantments = enchantments;
	}

	public ItemEnchantments(NBTTagCompound nbt) {
		deserializeNBT(nbt);
	}

	public List<SingleItem> getItems() {
		return items;
	}

	public List<SingleEnchantment> getEnchantments() {
		return enchantments;
	}

	public boolean isValid() {
		if (items == null || items.isEmpty())
			return false;
		for (SingleItem item : items)
			if (!item.isValid())
				return false;
		return true;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("items", items)
			.add("enchantments", enchantments)
			.toString();
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList itemList = new NBTTagList();
		items.forEach(singleItem -> itemList.appendTag(singleItem.serializeNBT()));
		nbt.setTag("items", itemList);
		if (enchantments != null && !enchantments.isEmpty()) {
			NBTTagList enchantmentList = new NBTTagList();
			enchantments.forEach(singleEnchantment -> enchantmentList.appendTag(singleEnchantment.serializeNBT()));
			nbt.setTag("enchantments", enchantmentList);
		}
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		if (items == null)
			items = new LinkedList<>();
		else
			items.clear();
		NBTTagList list = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);
		list.forEach(tag -> items.add(new SingleItem((NBTTagCompound) tag)));
		if (enchantments == null)
			enchantments = new LinkedList<>();
		else
			enchantments.clear();
		if (nbt.hasKey("enchantments")) {
			list = nbt.getTagList("enchantments", Constants.NBT.TAG_COMPOUND);
			list.forEach(tag -> enchantments.add(new SingleEnchantment((NBTTagCompound) tag)));
		}
	}

	public static class SingleItem implements INBTSerializable<NBTTagCompound> {
		private String registryName, className;
		private Integer metadata;

		private transient Class<? extends IForgeRegistryEntry.Impl> itemOrBlockClass;
		private transient ItemStack itemStack;

		public SingleItem(String registryName, Integer metadata, String className) {
			this.registryName = registryName;
			this.metadata = metadata;
			this.className = className;
		}

		public SingleItem(NBTTagCompound nbt) {
			deserializeNBT(nbt);
		}

		public ItemStack getItemStack() {
			if (!StringUtils.isNullOrEmpty(registryName) && itemStack == null) {
				Item item = Item.getByNameOrId(registryName);
				if (item != null)
					itemStack = new ItemStack(item, 1, metadata == null ? OreDictionary.WILDCARD_VALUE : metadata);
			}
			return itemStack;
		}

		public Class<? extends IForgeRegistryEntry.Impl> getItemOrBlockClass() {
			if (!StringUtils.isNullOrEmpty(className) && itemOrBlockClass == null) {
				try {
					Class<?> c = Class.forName(className);
					itemOrBlockClass = c.asSubclass(IForgeRegistryEntry.Impl.class);
				} catch (ClassNotFoundException | ClassCastException e) {
					e.printStackTrace();
				}
			}
			return itemOrBlockClass;
		}

		public boolean isValid() {
			if (StringUtils.isNullOrEmpty(registryName) && StringUtils.isNullOrEmpty(className))
				return false;
			if (!StringUtils.isNullOrEmpty(className)) {
				//Make sure the class is an Item or Block
				Class c = getItemOrBlockClass();
				return c != null && (Item.class.isAssignableFrom(c) || Block.class.isAssignableFrom(c));
			}
			return true;
		}

		public boolean matches(ItemStack stack) {
			ItemStack stackToMatch = getItemStack();
			if (stackToMatch != null)
				return OreDictionary.itemMatches(stackToMatch, stack, false);

			Class classToMatch = getItemOrBlockClass();
			if (classToMatch != null) {
				Object stackItem = stack.getItem();
				if (stackItem instanceof ItemBlock) stackItem = ((ItemBlock) stackItem).getBlock();
				return classToMatch.isInstance(stackItem);
			}

			return false;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof SingleItem &&
				Objects.equal(registryName, ((SingleItem) obj).registryName) &&
				Objects.equal(metadata, ((SingleItem) obj).metadata);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
				.add("registryName", registryName)
				.add("metadata", metadata)
				.add("className", className)
				.toString();
		}

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			if (registryName != null) nbt.setString("name", registryName);
			if (metadata != null) nbt.setInteger("meta", metadata);
			if (className != null) nbt.setString("class", className);
			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			registryName = nbt.getString("name");
			metadata = nbt.hasKey("meta") ? nbt.getInteger("meta") : null;
			className = nbt.getString("class");
		}
	}

	public static class SingleEnchantment implements INBTSerializable<NBTTagCompound> {
		private String registryName;
		private int strength;

		private transient Enchantment enchantment;

		public SingleEnchantment(String registryName, int strength) {
			this.registryName = registryName;
			this.strength = strength;
		}

		public SingleEnchantment(NBTTagCompound nbt) {
			deserializeNBT(nbt);
		}

		public Enchantment getEnchantment() {
			if (enchantment == null) {
				ResourceLocation resLoc = new ResourceLocation(registryName);
				enchantment = ForgeRegistries.ENCHANTMENTS.getValue(resLoc);
				if (enchantment == null) {
					DefaultEnchantments.logger.info("Enchantment {} does not exist!", resLoc);
				}
			}
			return enchantment;
		}

		public int getStrength() {
			return strength;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
				.add("registryName", registryName)
				.add("strength", strength)
				.toString();
		}

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("name", registryName);
			nbt.setInteger("strength", strength);
			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			registryName = nbt.getString("name");
			strength = nbt.getInteger("strength");
		}
	}
}
