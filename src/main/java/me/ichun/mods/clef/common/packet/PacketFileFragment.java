package me.ichun.mods.clef.common.packet;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.packet.PacketDataFragment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;

public class PacketFileFragment extends PacketDataFragment
{
    public PacketFileFragment(){}

    public PacketFileFragment(String fileName, int packetTotal, int packetNumber, int fragmentSize, byte[] data)
    {
        super(fileName, packetTotal, packetNumber, fragmentSize, data);
    }

    @Override
    public Side receivingSide()
    {
        return null;
    }

    @Override
    public void execution(Side side, EntityPlayer player)
    {
        byte[][] packets = partialFileFragments.computeIfAbsent(fileName, v -> new byte[packetTotal][]);
        packets[packetNumber] = data;

        boolean complete = true;
        for(byte[] b : packets)
        {
            if(b == null || b.length == 0)
            {
                complete = false;
                break;
            }
        }
        if(complete)
        {
            if(fileName.endsWith(".cia"))
            {
                InstrumentLibrary.handleReceivedFile(fileName, packets, side);
            }
            else if(fileName.endsWith(".abc"))
            {
                AbcLibrary.handleReceivedFile(fileName, packets);
            }
            else
            {
                Clef.LOGGER.warn("Received unknown file fragment!");
            }
            partialFileFragments.remove(fileName);
        }
    }

    public static HashMap<String, byte[][]> partialFileFragments = new HashMap<>();
}
