package me.ichun.mods.clef.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PacketPlayABC extends AbstractPacket
{
    public String abc_md5;

    public PacketPlayABC(){}

    public PacketPlayABC(String abc)
    {
        abc_md5 = abc;
    }

    @Override
    public void writeTo(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, abc_md5);
    }

    @Override
    public void readFrom(ByteBuf buf)
    {
        abc_md5 = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        AbcLibrary.playAbc(abc_md5, player);
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return Side.SERVER;
    }
}
