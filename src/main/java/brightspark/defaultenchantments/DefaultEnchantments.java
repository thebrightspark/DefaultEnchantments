package brightspark.defaultenchantments;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

@Mod(modid = DefaultEnchantments.MOD_ID, name = DefaultEnchantments.NAME, version = DefaultEnchantments.VERSION)
public class DefaultEnchantments {
	public static final String MOD_ID = "defaultenchantments";
	public static final String NAME = "Default Enchantments";
	public static final String VERSION = "@VERSION@";
	private static final String FILE_NAME = "defaultEnchantments.json";

	public static Logger logger;
	private static File modConfigDir;
	private static SimpleNetworkWrapper network;
	public static Collection<ItemEnchantments> itemEnchantments;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		modConfigDir = new File(event.getModConfigurationDirectory(), MOD_ID);
		network = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
		network.registerMessage(ConfigSyncMessage.Handler.class, ConfigSyncMessage.class, 0, Side.CLIENT);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) throws IOException {
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
			if (!areItemsValid(ie)) {
				logger.warn("Entry in {} has invalid items -> {}", FILE_NAME, ie);
				iterator.remove();
			}
			checkEnchantments(ie);
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

	private static void checkEnchantments(ItemEnchantments itemEnchantments) {
		List<ItemEnchantments.SingleEnchantment> enchantments = itemEnchantments.getEnchantments();
		if (enchantments == null || enchantments.isEmpty())
			return;
		enchantments.removeIf(enchantment -> {
			boolean invalid = enchantment.getEnchantment() == null;
			if (invalid)
				logger.warn("Entry in {} has an invalid enchantment -> {}", FILE_NAME, enchantment);
			return invalid;
		});
	}

	/**
	 * Get the ItemEnchantments for the given ItemStack
	 */
	private static LinkedList<ItemEnchantments.SingleEnchantment> getItemEnchantments(ItemStack stack) {
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

	@Mod.EventBusSubscriber(modid = MOD_ID)
	public static class DEHandler {
		/**
		 * Attempt to enchant the given ItemStack
		 */
		private static void tryEnchant(ItemStack stack) {
			LinkedList<ItemEnchantments.SingleEnchantment> enchantments = getItemEnchantments(stack);
			if (!enchantments.isEmpty()) {
				Map<Enchantment, Integer> stackEnchantments = EnchantmentHelper.getEnchantments(stack);
				enchantments.forEach(se -> stackEnchantments.put(se.getEnchantment(), se.getStrength()));
				EnchantmentHelper.setEnchantments(stackEnchantments, stack);
			}
		}

		@SubscribeEvent
		public static void itemCrafted(PlayerEvent.ItemCraftedEvent event) {
			tryEnchant(event.crafting);
		}

		@SubscribeEvent
		public static void itemSmelted(PlayerEvent.ItemSmeltedEvent event) {
			tryEnchant(event.smelting);
		}

		@SubscribeEvent
		public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
			logger.info("Sending {} default item enchantments to {}", itemEnchantments.size(), event.player.getName());
			network.sendTo(new ConfigSyncMessage(), (EntityPlayerMP) event.player);
		}

		@SubscribeEvent
		public static void onTooltip(ItemTooltipEvent event) {
			ItemStack stack = event.getItemStack();
			if (GuiScreen.isShiftKeyDown()) {
				List<ItemEnchantments.SingleEnchantment> enchantments = getItemEnchantments(stack);
				if (enchantments.size() == 0)
					return;

				List<String> tooltip = event.getToolTip();
				if (!tooltip.isEmpty())
					tooltip.add("");
				tooltip.add("Default enchantments on craft:");
				enchantments.stream()
					.filter(enchantment -> enchantment.getEnchantment() != null)
					.map(enchantment -> "  - " + enchantment.getEnchantment().getTranslatedName(enchantment.getStrength()))
					.sorted()
					.forEach(tooltip::add);
			} else if (!getItemEnchantments(stack).isEmpty()) {
				List<String> tooltip = event.getToolTip();
				if (!tooltip.isEmpty())
					tooltip.add("");
				tooltip.add("SHIFT for default enchantments");
			}
		}
	}
}
