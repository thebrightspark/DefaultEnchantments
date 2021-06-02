package brightspark.defaultenchantments;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = DefaultEnchantments.MOD_ID)
public class ClientEventHandler {
	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (GuiScreen.isShiftKeyDown()) {
			List<ItemEnchantments.SingleEnchantment> enchantments = DefaultEnchantments.getItemEnchantments(stack);
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
		} else if (!DefaultEnchantments.getItemEnchantments(stack).isEmpty()) {
			List<String> tooltip = event.getToolTip();
			if (!tooltip.isEmpty())
				tooltip.add("");
			tooltip.add("SHIFT for default enchantments");
		}
	}
}
