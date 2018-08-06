package brightspark.defaultenchantments;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashSet;

public class ConfigSyncMessage implements IMessage
{
    public ConfigSyncMessage() {}

    @Override
    public void fromBytes(ByteBuf buf)
    {
        int size = buf.readInt();
        DefaultEnchantments.itemEnchantments = new HashSet<>();
        for(int i = 0; i < size; i++)
            DefaultEnchantments.itemEnchantments.add(new ItemEnchantments(ByteBufUtils.readTag(buf)));
        DefaultEnchantments.logger.info("Received default item enchantments from server: {}", DefaultEnchantments.itemEnchantments);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(DefaultEnchantments.itemEnchantments.size());
        DefaultEnchantments.itemEnchantments.forEach(ie -> ByteBufUtils.writeTag(buf, ie.serializeNBT()));
    }

    public static class Handler implements IMessageHandler<ConfigSyncMessage, IMessage>
    {
        @Override
        public IMessage onMessage(ConfigSyncMessage message, MessageContext ctx)
        {
            return null;
        }
    }
}
