package brightspark.defaultenchantments;

import com.google.common.base.MoreObjects;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashSet;
import java.util.Set;

public class ItemEnchantments implements INBTSerializable<NBTTagCompound>
{
    private String itemRegistryName;
    private Integer itemMetadata;
    private Set<SingleEnchantment> enchantments;

    private transient ItemStack itemStack;

    public ItemEnchantments(String itemRegistryName, int itemMetadata)
    {
        this.itemRegistryName = itemRegistryName;
        this.itemMetadata = itemMetadata;
    }

    public ItemEnchantments(NBTTagCompound nbt)
    {
        deserializeNBT(nbt);
    }

    public Set<SingleEnchantment> getEnchantments()
    {
        return enchantments;
    }

    public ItemStack getItemStack()
    {
        if(itemStack == null)
        {
            Item item = Item.getByNameOrId(itemRegistryName);
            if(item != null)
                itemStack = new ItemStack(item, 1, itemMetadata == null ? OreDictionary.WILDCARD_VALUE : itemMetadata);
        }
        return itemStack;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("itemRegistryName", itemRegistryName)
                .add("itemMetadata", itemMetadata)
                .add("enchantments", enchantments)
                .toString();
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("name", itemRegistryName);
        nbt.setInteger("meta", itemMetadata);
        NBTTagList list = new NBTTagList();
        enchantments.forEach(singleEnchantment -> list.appendTag(singleEnchantment.serializeNBT()));
        nbt.setTag("enchantments", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        itemRegistryName = nbt.getString("name");
        itemMetadata = nbt.getInteger("meta");
        if(enchantments == null)
            enchantments = new HashSet<>();
        else
            enchantments.clear();
        NBTTagList list = nbt.getTagList("enchantments", Constants.NBT.TAG_COMPOUND);
        list.forEach(tag -> enchantments.add(new SingleEnchantment((NBTTagCompound) tag)));
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
