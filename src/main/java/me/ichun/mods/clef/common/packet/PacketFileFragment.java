package me.ichun.mods.clef.common.packet;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.network.PacketDataFragment;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketFileFragment extends PacketDataFragment
{
    public PacketFileFragment(){}

    public PacketFileFragment(String fileName, int packetTotal, int packetNumber, byte[] data)
    {
        super(fileName, packetTotal, packetNumber, data);
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            LogicalSide side = context.getDirection().getReceptionSide();
            byte[] data = process(side);
            if(data != null) //we have all the fragments
            {
                if(fileName.endsWith(".cia"))
                {
                    InstrumentLibrary.handleReceivedFile(fileName, data, side);
                }
                else if(fileName.endsWith(".abc"))
                {
                    AbcLibrary.handleReceivedFile(fileName, data, side);
                }
                else
                {
                    Clef.LOGGER.warn("Received unknown file fragment!");
                }
            }
        });
    }
}
