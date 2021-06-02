package brightspark.defaultenchantments;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Mod(modid = DefaultEnchantments.MOD_ID, name = DefaultEnchantments.NAME, version = DefaultEnchantments.VERSION)
public class DefaultEnchantments {
	public static final String MOD_ID = "defaultenchantments";
	public static final String NAME = "Default Enchantments";
	public static final String VERSION = "@VERSION@";
	private static final String FILE_NAME = "defaultEnchantments.json";

	public static Logger logger;
	private static File modConfigDir;
	public static SimpleNetworkWrapper network;
	public static Collection<ItemEnchantments> itemEnchantments;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		modConfigDir = new File(event.getModConfigurationDirectory(), MOD_ID);
		network = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
		network.registerMessage(ConfigSyncMessage.Handler.class, ConfigSyncMessage.class, 0, Side.CLIENT);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) throws IOException {
		logger.info("Starting to read default item enchantments from file {}", FILE_NAME);

		if (!modConfigDir.exists() && !modConfigDir.mkdir())
			throw new RuntimeException("Error creating mod config directory " + MOD_ID);
		File jsonFile = new File(modConfigDir, FILE_NAME);

		if (!jsonFile.exists()) {
			//Can't read a blank file
			logger.error("No {} file found! Creating file...", FILE_NAME);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile))) {
				writer.write("[\n\n]");
			}
		} else {
			//Read item enchantments
			Gson gson = new Gson();
			try (FileReader reader = new FileReader(jsonFile)) {
				itemEnchantments = gson.fromJson(reader, new TypeToken<Collection<ItemEnchantments>>() {}.getType());
			} catch (IOException e) {
				logger.error("Error reading from JSON file {}", jsonFile);
				throw e;
			}
		}

		if (itemEnchantments == null || itemEnchantments.isEmpty()) {
			logger.warn("No default item enchantments loaded from {}!", FILE_NAME);
			return;
		}

		logger.info("Read default item enchantments from file:\n{}", itemEnchantments);

		//Make sure all are valid
		Iterator<ItemEnchantments> iterator = itemEnchantments.iterator();
		while (iterator.hasNext()) {
			ItemEnchantments ie = iterator.next();
			logger.info("Validating {}", ie);
			if (!areItemsValid(ie)) {
				logger.warn("Invalid items -> {}", ie.getItems());
				iterator.remove();
			}
			List<ItemEnchantments.SingleEnchantment> enchantments = ie.getEnchantments();
			if (enchantments != null && !enchantments.isEmpty()) {
				enchantments.removeIf(enchantment -> {
					boolean invalid = enchantment.getEnchantment() == null;
					if (invalid)
						logger.warn("Invalid enchantment -> {}", enchantment);
					return invalid;
				});
				if (enchantments.isEmpty()) {
					logger.warn("Enchantments exist, but none are valid, so removing entry");
					iterator.remove();
				}
			}
		}

		logger.info("Loaded {} default item enchantments from {}", itemEnchantments.size(), FILE_NAME);
	}

	private static boolean areItemsValid(ItemEnchantments itemEnchantments) {
		List<ItemEnchantments.SingleItem> items = itemEnchantments.getItems();
		if (items == null || items.isEmpty())
			return false;
		boolean valid = true;
		for (ItemEnchantments.SingleItem item : items) {
			if (!item.isValid()) {
				valid = false;
				logger.warn("Entry in {} has an invalid item -> {}", FILE_NAME, item);
			}
		}
		return valid;
	}

	/**
	 * Get the ItemEnchantments for the given ItemStack
	 */
	public static LinkedList<ItemEnchantments.SingleEnchantment> getItemEnchantments(ItemStack stack) {
		LinkedList<ItemEnchantments.SingleEnchantment> enchantments = new LinkedList<>();
		if (itemEnchantments == null)
			return enchantments;
		for (ItemEnchantments ie : itemEnchantments)
			for (ItemEnchantments.SingleItem item : ie.getItems())
				if (item.matches(stack)) {
					List<ItemEnchantments.SingleEnchantment> ie_e = ie.getEnchantments();
					if (ie_e == null || ie_e.isEmpty())
						//If no enchantments, then clear what we've collected so far
						enchantments.clear();
					else
						enchantments.addAll(ie_e);
					break;
				}
		return enchantments;
	}
}
