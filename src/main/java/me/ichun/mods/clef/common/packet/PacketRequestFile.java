package me.ichun.mods.clef.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PacketRequestFile extends AbstractPacket
{
    public String file; //fileName for instrument, md5 for abc
    public boolean isInstrument;

    public PacketRequestFile(){}

    public PacketRequestFile(String s, boolean isInstrument)
    {
        this.file = s;
        this.isInstrument = isInstrument;
    }

    @Override
    public void writeTo(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, file);
        buf.writeBoolean(isInstrument);
    }

    @Override
    public void readFrom(ByteBuf buf)
    {
        file = ByteBufUtils.readUTF8String(buf);
        isInstrument = buf.readBoolean();
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        if(isInstrument)
        {
            InstrumentLibrary.packageAndSendInstrument(file, side == Side.SERVER ? player : null); //send to player, if null, to server
        }
        else
        {
            AbcLibrary.sendAbc(file, side == Side.SERVER ? player : null);
        }
        return null;
    }

    @Override
    public Side receivingSide() //Received on both sides
    {
        return null;
    }
}
