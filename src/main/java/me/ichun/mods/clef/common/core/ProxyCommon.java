package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.item.ItemInstrument;
import me.ichun.mods.clef.common.network.GuiPlayTrackBlockHandler;
import me.ichun.mods.clef.common.packet.*;
import me.ichun.mods.clef.common.thread.ThreadReadFiles;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.item.ItemHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProxyCommon
{
    private ThreadReadFiles threadReadFiles;

    public void preInitMod()
    {
        threadReadFiles  = new ThreadReadFiles();
        threadReadFiles.start();

        GameRegistry.registerTileEntity(TileEntityInstrumentPlayer.class, "Clef:InstrumentPlayer");

        NetworkRegistry.INSTANCE.registerGuiHandler(Clef.instance, new GuiPlayTrackBlockHandler());

        ItemHandler.registerDualHandedItem(ItemInstrument.class, new ItemInstrument.DualHandedInstrumentCallback());

        Clef.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(Clef.eventHandlerServer);

        Clef.channel = new PacketChannel("Clef", PacketRequestFile.class, PacketFileFragment.class, PacketPlayABC.class, PacketPlayingTracks.class, PacketStopPlayingTrack.class, PacketInstrumentPlayerInfo.class, PacketCreateInstrument.class);
    }

    public void loadComplete()
    {
        Clef.LOGGER.info("Waiting for file reader thread to finish");
        try
        {
            threadReadFiles.latch.await();
        }
        catch (InterruptedException e)
        {
            Clef.LOGGER.error("Got interrupted while waiting for FileReaderThread to finish");
            e.printStackTrace();
        }
    }
}
