package me.ichun.mods.clef.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

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
    public void writeTo(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, abc_md5);
        ByteBufUtils.writeUTF8String(buf, bandName);
        buf.writeBoolean(syncPlay);
        buf.writeBoolean(syncTrack);
    }

    @Override
    public void readFrom(ByteBuf buf)
    {
        abc_md5 = ByteBufUtils.readUTF8String(buf);
        bandName = ByteBufUtils.readUTF8String(buf);
        syncPlay = buf.readBoolean();
        syncTrack = buf.readBoolean();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        AbcLibrary.playAbc(abc_md5, bandName, syncPlay, syncTrack, player);
    }

    @Override
    public Side receivingSide()
    {
        return Side.SERVER;
    }
}
