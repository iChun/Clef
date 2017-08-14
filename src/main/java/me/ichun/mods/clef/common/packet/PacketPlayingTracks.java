package me.ichun.mods.clef.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PacketPlayingTracks extends AbstractPacket
{
    public Track[] tracks;

    public PacketPlayingTracks(){}

    public PacketPlayingTracks(Track...tracks)
    {
        this.tracks = tracks;
    }

    @Override
    public void writeTo(ByteBuf buf)
    {
        buf.writeInt(tracks.length);
        for(Track track : tracks)
        {
            ByteBufUtils.writeUTF8String(buf, track.getId());
            ByteBufUtils.writeUTF8String(buf, track.getBandName());
            ByteBufUtils.writeUTF8String(buf, track.getMd5());
            buf.writeInt(track.playProg);
            buf.writeInt(track.players.size());
            for(EntityPlayer player : track.players.keySet())
            {
                ByteBufUtils.writeUTF8String(buf, player.getName());
            }
        }
    }

    @Override
    public void readFrom(ByteBuf buf)
    {
        tracks = new Track[buf.readInt()];
        for(int i = 0; i < tracks.length; i++)
        {
            String id = ByteBufUtils.readUTF8String(buf);
            String band = ByteBufUtils.readUTF8String(buf);
            String md5 = ByteBufUtils.readUTF8String(buf);
            TrackFile file = AbcLibrary.getTrack(md5);
            tracks[i] = new Track(id, band, md5, file != null ? file.track : null, true);
            tracks[i].playProg = buf.readInt();
            int playerCount = buf.readInt();
            for(int x = 0; x < playerCount; x++)
            {
                tracks[i].addPlayer(ByteBufUtils.readUTF8String(buf));
            }
        }
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        for(int i = 0; i < tracks.length; i++)
        {
            Clef.eventHandlerClient.addTrack(tracks[i]);
        }
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return Side.CLIENT;
    }
}
