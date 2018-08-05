package brightspark.defaultenchantments;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = DefaultEnchantments.MOD_ID, name = DefaultEnchantments.NAME, version = DefaultEnchantments.VERSION)
public class DefaultEnchantments
{
    public static final String MOD_ID = "defaultenchantments";
    public static final String NAME = "Default Enchantments";
    public static final String VERSION = "@VERSION@";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }
}
