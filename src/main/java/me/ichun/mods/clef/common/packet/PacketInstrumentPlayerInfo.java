package me.ichun.mods.clef.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

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
    public void writeTo(ByteBuf buf)
    {
        PacketBuffer pb = new PacketBuffer(buf);
        buf.writeInt(abc_md5s.size());
        for(String s : abc_md5s)
        {
            ByteBufUtils.writeUTF8String(buf, s);
        }
        ByteBufUtils.writeUTF8String(buf, bandName);
        buf.writeBoolean(syncPlay);
        buf.writeBoolean(syncTrack);
        buf.writeInt(repeat);
        buf.writeBoolean(shuffle);
        pb.writeBlockPos(pos);
    }

    @Override
    public void readFrom(ByteBuf buf)
    {
        PacketBuffer pb = new PacketBuffer(buf);
        abc_md5s = new ArrayList<>();
        int size = buf.readInt();
        for(int i = 0; i < size; i++)
        {
            abc_md5s.add(ByteBufUtils.readUTF8String(buf));
        }
        bandName = ByteBufUtils.readUTF8String(buf);
        syncPlay = buf.readBoolean();
        syncTrack = buf.readBoolean();
        repeat = buf.readInt();
        shuffle = buf.readBoolean();
        pos = pb.readBlockPos();
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        TileEntity te = player.worldObj.getTileEntity(pos);
        if(te instanceof TileEntityInstrumentPlayer)
        {
            TileEntityInstrumentPlayer instrumentPlayer = (TileEntityInstrumentPlayer)te;
            instrumentPlayer.tracks.clear();
            for(String s : abc_md5s)
            {
                TrackFile track = AbcLibrary.getTrack(s);
                if(track == null)
                {
                    instrumentPlayer.pending_md5s = abc_md5s;
                    Clef.channel.sendTo(new PacketRequestFile(s, false), player);
                }
            }
            instrumentPlayer.bandName = bandName;
            instrumentPlayer.syncPlay = syncPlay;
            instrumentPlayer.syncTrack = syncTrack;
            instrumentPlayer.repeat = repeat;
            instrumentPlayer.shuffle = shuffle;
            instrumentPlayer.markDirty();
            IBlockState state = player.worldObj.getBlockState(pos);
            player.worldObj.notifyBlockUpdate(pos, state, state, 3);
        }
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return Side.SERVER;
    }
}
