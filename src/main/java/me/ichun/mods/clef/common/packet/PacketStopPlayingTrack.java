package me.ichun.mods.clef.common.packet;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketStopPlayingTrack extends AbstractPacket
{
    public String trackId;

    public PacketStopPlayingTrack(){}

    public PacketStopPlayingTrack(String s)
    {
        trackId = s;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeString(trackId);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        trackId = readString(buf);
    }

    @Override
    public void process(NetworkEvent.Context context) //receivingSide() SERVER
    {
        context.enqueueWork(() -> {
            Clef.eventHandlerServer.stopPlayingTrack(context.getSender(), trackId);
        });
    }
}
