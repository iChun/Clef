package me.ichun.mods.clef.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.Map;

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
        PacketBuffer buff = new PacketBuffer(buf);
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
            buf.writeInt(track.instrumentPlayers.size());
            for(Map.Entry<Integer, HashSet<BlockPos>> e : track.instrumentPlayers.entrySet())
            {
                buf.writeInt(e.getKey());
                buf.writeInt(e.getValue().size());
                for(BlockPos pos : e.getValue())
                {
                    buff.writeBlockPos(pos);
                }
            }
            buf.writeInt(track.zombies.size());
            for(Integer i : track.zombies)
            {
                buf.writeInt(i);
            }
        }
    }

    @Override
    public void readFrom(ByteBuf buf)
    {
        PacketBuffer buff = new PacketBuffer(buf);
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
            playerCount = buf.readInt();
            for(int x = 0; x < playerCount; x++)
            {
                int key = buf.readInt();
                int count = buf.readInt();
                HashSet<BlockPos> poses = new HashSet<>();
                for(int k = 0; k < count; k++)
                {
                    poses.add(buff.readBlockPos());
                }
                tracks[i].instrumentPlayers.put(key, poses);
            }
            playerCount = buf.readInt();
            for(int x = 0; x < playerCount; x++)
            {
                tracks[i].zombies.add(buf.readInt());
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
