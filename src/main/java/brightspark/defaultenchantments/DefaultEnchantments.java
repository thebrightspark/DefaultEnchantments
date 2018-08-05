package brightspark.defaultenchantments;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@Mod(modid = DefaultEnchantments.MOD_ID, name = DefaultEnchantments.NAME, version = DefaultEnchantments.VERSION)
public class DefaultEnchantments
{
    public static final String MOD_ID = "defaultenchantments";
    public static final String NAME = "Default Enchantments";
    public static final String VERSION = "@VERSION@";
    private static final String FILE_NAME = "defaultEnchantments.json";

    private static Logger logger;
    private static Collection<ItemEnchantments> itemEnchantments;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) throws IOException
    {
        logger = event.getModLog();

        logger.info("Starting to read {}", FILE_NAME);

        File modConfigDir = new File(event.getModConfigurationDirectory(), MOD_ID);
        if(!modConfigDir.exists() && !modConfigDir.mkdir())
            throw new RuntimeException("Error creating mod config directory " + MOD_ID);
        File jsonFile = new File(modConfigDir, FILE_NAME);
        if(!jsonFile.exists())
        {
            logger.error("No {} file found! Creating blank file...");
            if(!jsonFile.createNewFile())
                logger.error("File already existed!?");
            //Can't read a blank file
            return;
        }

        //Read item enchantments
        Gson gson = new Gson();
        try(FileReader reader = new FileReader(jsonFile))
        {
            itemEnchantments = gson.fromJson(reader, new TypeToken<Collection<ItemEnchantments>>(){}.getType());
        }
        catch(IOException e)
        {
            logger.error("Error reading from JSON file ", jsonFile);
            throw e;
        }

        if(itemEnchantments == null || itemEnchantments.isEmpty())
        {
            logger.warn("No item enchantments loaded from {}!", FILE_NAME);
            return;
        }

        logger.info("Read enchantments:\n{}", itemEnchantments);

        //Validate item enchantments
        Iterator<ItemEnchantments> iterator = itemEnchantments.iterator();
        while(iterator.hasNext())
        {
            ItemEnchantments ie = iterator.next();
            if(ie.getItemStack() == null || ie.getEnchantments() == null || ie.getEnchantments().isEmpty())
            {
                logger.warn("Invalid entry in {} -> {}", FILE_NAME, ie);
                iterator.remove();
            }
        }

        logger.info("Loaded {} item enchantments from {}", itemEnchantments.size(), FILE_NAME);
    }

    /**
     * Get the ItemEnchantments for the given ItemStack
     */
    private static ItemEnchantments getItemEnchantments(ItemStack stack)
    {
        for(ItemEnchantments ie : itemEnchantments)
            if(OreDictionary.itemMatches(ie.getItemStack(), stack, false))
                return ie;
        return null;
    }

    @Mod.EventBusSubscriber
    public static class DEHandler
    {
        /**
         * Attempt to enchant the given ItemStack
         */
        private static void tryEnchant(ItemStack stack)
        {
            ItemEnchantments ie = getItemEnchantments(stack);
            if(ie != null)
            {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
                logger.info("{} enchantments before: {}", stack, enchantments);
                ie.getEnchantments().forEach(se -> enchantments.put(se.getEnchantment(), se.getStrength()));
                logger.info("{} enchantments after: {}", stack, enchantments);
                EnchantmentHelper.setEnchantments(enchantments, stack);
            }
        }

        @SubscribeEvent
        public static void itemCrafted(PlayerEvent.ItemCraftedEvent event)
        {
            tryEnchant(event.crafting);
        }

        @SubscribeEvent
        public static void itemSmelted(PlayerEvent.ItemSmeltedEvent event)
        {
            tryEnchant(event.smelting);
        }
    }
}
