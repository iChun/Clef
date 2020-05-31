package me.ichun.mods.clef.common.packet;

import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPlayABC extends AbstractPacket
{
    public String abc_md5;
    public String bandName;
    public boolean syncPlay;
    public boolean syncTrack;

    public PacketPlayABC(){}

    public PacketPlayABC(String abc, String bandName, boolean syncPlay, boolean syncTrack)
    {
        this.abc_md5 = abc;
        this.bandName = bandName;
        this.syncPlay = syncPlay;
        this.syncTrack = syncTrack;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeString(abc_md5);
        buf.writeString(bandName);
        buf.writeBoolean(syncPlay);
        buf.writeBoolean(syncTrack);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        abc_md5 = readString(buf);
        bandName = readString(buf);
        syncPlay = buf.readBoolean();
        syncTrack = buf.readBoolean();
    }

    @Override
    public void process(NetworkEvent.Context context) //receivingSide() SERVER
    {
        context.enqueueWork(() -> {
            AbcLibrary.playAbc(abc_md5, bandName, syncPlay, syncTrack, context.getSender());
        });
    }
}
