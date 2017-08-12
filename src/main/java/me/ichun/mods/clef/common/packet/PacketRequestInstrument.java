package me.ichun.mods.clef.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PacketRequestInstrument extends AbstractPacket
{
    public String instrumentName;

    public PacketRequestInstrument(){}

    public PacketRequestInstrument(String s)
    {
        instrumentName = s;
    }

    @Override
    public void writeTo(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, instrumentName);
    }

    @Override
    public void readFrom(ByteBuf buf)
    {
        instrumentName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        InstrumentLibrary.packageAndSendInstrument(instrumentName, side); //side is the side receiving this request
        return null;
    }

    @Override
    public Side receivingSide() //Received on both sides
    {
        return null;
    }
}
