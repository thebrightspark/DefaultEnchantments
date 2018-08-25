package brightspark.defaultenchantments;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.oredict.OreDictionary;

import java.util.LinkedList;
import java.util.List;

public class ItemEnchantments implements INBTSerializable<NBTTagCompound>
{
    private List<SingleItem> items;
    private List<SingleEnchantment> enchantments;

    public ItemEnchantments(List<SingleItem> items, List<SingleEnchantment> enchantments)
    {
        this.items = items;
        this.enchantments = enchantments;
    }

    public ItemEnchantments(NBTTagCompound nbt)
    {
        deserializeNBT(nbt);
    }

    public List<SingleItem> getItems()
    {
        return items;
    }

    public List<SingleEnchantment> getEnchantments()
    {
        return enchantments;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("items", items)
                .add("enchantments", enchantments)
                .toString();
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList itemList = new NBTTagList();
        items.forEach(singleItem -> itemList.appendTag(singleItem.serializeNBT()));
        nbt.setTag("items", itemList);
        NBTTagList enchantmentList = new NBTTagList();
        enchantments.forEach(singleEnchantment -> enchantmentList.appendTag(singleEnchantment.serializeNBT()));
        nbt.setTag("enchantments", enchantmentList);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        if(items == null)
            items = new LinkedList<>();
        else
            items.clear();
        NBTTagList list = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);
        list.forEach(tag -> items.add(new SingleItem((NBTTagCompound) tag)));
        if(enchantments == null)
            enchantments = new LinkedList<>();
        else
            enchantments.clear();
        list = nbt.getTagList("enchantments", Constants.NBT.TAG_COMPOUND);
        list.forEach(tag -> enchantments.add(new SingleEnchantment((NBTTagCompound) tag)));
    }

    public static class SingleItem implements INBTSerializable<NBTTagCompound>
    {
        private String registryName;
        private Integer metadata;

        private transient ItemStack itemStack;

        public SingleItem(String registryName, Integer metadata)
        {
            this.registryName = registryName;
            this.metadata = metadata;
        }

        public SingleItem(NBTTagCompound nbt)
        {
            deserializeNBT(nbt);
        }

        public ItemStack getItemStack()
        {
            if(itemStack == null)
            {
                Item item = Item.getByNameOrId(registryName);
                if(item != null)
                    itemStack = new ItemStack(item, 1, metadata == null ? OreDictionary.WILDCARD_VALUE : metadata);
            }
            return itemStack;
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof SingleItem &&
                    Objects.equal(registryName, ((SingleItem) obj).registryName) &&
                    Objects.equal(metadata, ((SingleItem) obj).metadata);
        }

        @Override
        public String toString()
        {
            return MoreObjects.toStringHelper(this)
                    .add("registryName", registryName)
                    .add("metadata", metadata)
                    .toString();
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("name", registryName);
            nbt.setInteger("meta", metadata);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            registryName = nbt.getString("name");
            metadata = nbt.getInteger("meta");
        }
    }

    public static class SingleEnchantment implements INBTSerializable<NBTTagCompound>
    {
        private String registryName;
        private int strength;

        private transient Enchantment enchantment;

        public SingleEnchantment(String registryName, int strength)
        {
            this.registryName = registryName;
            this.strength = strength;
        }

        public SingleEnchantment(NBTTagCompound nbt)
        {
            deserializeNBT(nbt);
        }

        public Enchantment getEnchantment()
        {
            if(enchantment == null)
                enchantment = Enchantment.getEnchantmentByLocation(registryName);
            return enchantment;
        }

        public int getStrength()
        {
            return strength;
        }

        @Override
        public String toString()
        {
            return MoreObjects.toStringHelper(this)
                    .add("registryName", registryName)
                    .add("strength", strength)
                    .toString();
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("name", registryName);
            nbt.setInteger("strength", strength);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            registryName = nbt.getString("name");
            strength = nbt.getInteger("strength");
        }
    }
}
