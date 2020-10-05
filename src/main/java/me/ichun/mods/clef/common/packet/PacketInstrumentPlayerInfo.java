package me.ichun.mods.clef.common.packet;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;

public class PacketInstrumentPlayerInfo extends AbstractPacket
{
    public ArrayList<String> abc_md5s;
    public String bandName;
    public boolean syncPlay;
    public boolean syncTrack;
    public int repeat;
    public boolean shuffle;
    public BlockPos pos;

    public PacketInstrumentPlayerInfo(){}

    public PacketInstrumentPlayerInfo(ArrayList<String> abc, String bandName, boolean syncPlay, boolean syncTrack, int repeat, boolean shuffle, BlockPos pos)
    {
        this.abc_md5s = abc;
        this.bandName = bandName;
        this.syncPlay = syncPlay;
        this.syncTrack = syncTrack;
        this.repeat = repeat;
        this.shuffle = shuffle;
        this.pos = pos;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(abc_md5s.size());
        for(String s : abc_md5s)
        {
            buf.writeString(s);
        }
        buf.writeString(bandName);
        buf.writeBoolean(syncPlay);
        buf.writeBoolean(syncTrack);
        buf.writeInt(repeat);
        buf.writeBoolean(shuffle);
        buf.writeBlockPos(pos);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        abc_md5s = new ArrayList<>();
        int size = buf.readInt();
        for(int i = 0; i < size; i++)
        {
            abc_md5s.add(readString(buf));
        }
        bandName = readString(buf);
        syncPlay = buf.readBoolean();
        syncTrack = buf.readBoolean();
        repeat = buf.readInt();
        shuffle = buf.readBoolean();
        pos = buf.readBlockPos();
    }

    @Override
    public void process(NetworkEvent.Context context) //receivingSide() SERVER
    {
        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            TileEntity te = player.world.getTileEntity(pos);
            if(te instanceof TileEntityInstrumentPlayer)
            {
                TileEntityInstrumentPlayer instrumentPlayer = (TileEntityInstrumentPlayer)te;
                instrumentPlayer.tracks.clear();
                for(String s : abc_md5s)
                {
                    TrackFile track = AbcLibrary.getTrack(s);
                    if(track == null)
                    {
                        instrumentPlayer.pending_md5s.add(s);
                        Clef.channel.sendTo(new PacketRequestFile(s, false), player);
                    }
                    else
                    {
                        instrumentPlayer.tracks.add(track);
                    }
                }
                instrumentPlayer.bandName = bandName;
                instrumentPlayer.syncPlay = syncPlay;
                instrumentPlayer.syncTrack = syncTrack;
                instrumentPlayer.repeat = repeat;
                instrumentPlayer.shuffle = shuffle;
                instrumentPlayer.markDirty();
                BlockState state = player.world.getBlockState(pos);
                player.world.notifyBlockUpdate(pos, state, state, 3);
            }
        });
    }
}
