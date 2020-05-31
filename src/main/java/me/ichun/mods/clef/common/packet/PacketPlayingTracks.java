package me.ichun.mods.clef.common.packet;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

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
    public void writeTo(PacketBuffer buf)
    {
        PacketBuffer buff = new PacketBuffer(buf);
        buf.writeInt(tracks.length);
        for(Track track : tracks)
        {
            buf.writeString(track.getId());
            buf.writeString(track.getBandName());
            buf.writeString(track.getMd5());
            buf.writeBoolean(track.playing);
            buf.writeInt(track.playProg);
            buf.writeInt(track.players.size());
            for(PlayerEntity player : track.players.keySet())
            {
                buf.writeString(player.getName().getUnformattedComponentText());
            }
            buf.writeInt(track.instrumentPlayers.size());
            for(Map.Entry<ResourceLocation, HashSet<BlockPos>> e : track.instrumentPlayers.entrySet())
            {
                buf.writeResourceLocation(e.getKey());
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
    public void readFrom(PacketBuffer buf)
    {
        PacketBuffer buff = new PacketBuffer(buf);
        tracks = new Track[buf.readInt()];
        for(int i = 0; i < tracks.length; i++)
        {
            String id = readString(buf);
            String band = readString(buf);
            String md5 = readString(buf);
            TrackFile file = AbcLibrary.getTrack(md5);
            tracks[i] = new Track(id, band, md5, file != null ? file.track : null, true);
            tracks[i].playing = buf.readBoolean();
            tracks[i].playProg = buf.readInt();
            int playerCount = buf.readInt();
            for(int x = 0; x < playerCount; x++)
            {
                tracks[i].addPlayer(readString(buf));
            }
            playerCount = buf.readInt();
            for(int x = 0; x < playerCount; x++)
            {
                ResourceLocation key = buf.readResourceLocation();
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
    public void process(NetworkEvent.Context context) //receivingSide() CLIENT
    {
        context.enqueueWork(() -> {
            for(int i = 0; i < tracks.length; i++)
            {
                Clef.eventHandlerClient.addTrack(tracks[i]);
            }
        });
    }
}
