package me.ichun.mods.clef.common.packet;

import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

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
    public void writeTo(PacketBuffer buf)
    {
        buf.writeString(file);
        buf.writeBoolean(isInstrument);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        file = readString(buf);
        isInstrument = buf.readBoolean();
    }

    @Override
    public void process(NetworkEvent.Context context) //receivingSide() //Received on both sides
    {
        context.enqueueWork(() -> {
            LogicalSide receivingSide = context.getDirection().getReceptionSide();
            if(isInstrument)
            {
                InstrumentLibrary.packageAndSendInstrument(file, receivingSide.isServer() ? context.getSender() : null); //send to player, if null, to server
            }
            else
            {
                AbcLibrary.sendAbc(file, receivingSide.isServer() ? context.getSender() : null);
            }
        });
    }
}
