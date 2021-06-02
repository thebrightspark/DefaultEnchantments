package brightspark.defaultenchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.LinkedList;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DefaultEnchantments.MOD_ID)
public class CommonEventHandler {
	/**
	 * Attempt to enchant the given ItemStack
	 */
	private static void tryEnchant(ItemStack stack) {
		LinkedList<ItemEnchantments.SingleEnchantment> enchantments = DefaultEnchantments.getItemEnchantments(stack);
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
		DefaultEnchantments.logger.info("Sending {} default item enchantments to {}", DefaultEnchantments.itemEnchantments.size(), event.player.getName());
		DefaultEnchantments.network.sendTo(new ConfigSyncMessage(), (EntityPlayerMP) event.player);
	}
}
