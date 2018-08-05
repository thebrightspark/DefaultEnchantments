package brightspark.defaultenchantments;

import com.google.common.base.MoreObjects;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Set;

public class ItemEnchantments
{
    private String itemRegistryName;
    private int itemMetadata;
    private Set<SingleEnchantment> enchantments;

    private transient ItemStack itemStack;

    public ItemEnchantments(String itemRegistryName, int itemMetadata)
    {
        this.itemRegistryName = itemRegistryName;
        this.itemMetadata = itemMetadata;
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
                itemStack = new ItemStack(item, 1, itemMetadata);
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

    public static class SingleEnchantment
    {
        private String registryName;
        private int strength;

        private transient Enchantment enchantment;

        public SingleEnchantment(String registryName, int strength)
        {
            this.registryName = registryName;
            this.strength = strength;
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
    }
}
