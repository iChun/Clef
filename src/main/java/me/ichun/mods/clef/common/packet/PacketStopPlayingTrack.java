package me.ichun.mods.clef.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PacketStopPlayingTrack extends AbstractPacket
{
    public String trackId;

    public PacketStopPlayingTrack(){}

    public PacketStopPlayingTrack(String s)
    {
        trackId = s;
    }

    @Override
    public void writeTo(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, trackId);
    }

    @Override
    public void readFrom(ByteBuf buf)
    {
        trackId = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        Clef.eventHandlerServer.stopPlayingTrack(player, trackId);
    }

    @Override
    public Side receivingSide()
    {
        return Side.SERVER;
    }
}
