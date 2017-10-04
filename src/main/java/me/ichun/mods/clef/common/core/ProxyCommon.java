package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.block.BlockInstrumentPlayer;
import me.ichun.mods.clef.common.item.ItemInstrument;
import me.ichun.mods.clef.common.network.GuiPlayTrackBlockHandler;
import me.ichun.mods.clef.common.packet.*;
import me.ichun.mods.clef.common.thread.ThreadReadFiles;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.item.ItemHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProxyCommon
{
    public void preInitMod()
    {
        (new ThreadReadFiles()).start();

        GameRegistry.registerTileEntity(TileEntityInstrumentPlayer.class, "Clef:InstrumentPlayer");

        NetworkRegistry.INSTANCE.registerGuiHandler(Clef.instance, new GuiPlayTrackBlockHandler());

        ItemHandler.registerDualHandedItem(ItemInstrument.class, new ItemInstrument.DualHandedInstrumentCallback());

        GameRegistry.addRecipe(new ItemStack(Clef.blockInstrumentPlayer, 1),
                "WCW", "WNW", "WRW", 'W', Blocks.PLANKS, 'C', Blocks.CHEST, 'N', Blocks.NOTEBLOCK, 'R', Blocks.REDSTONE_TORCH);

        Clef.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(Clef.eventHandlerServer);

        Clef.channel = new PacketChannel("Clef", PacketRequestFile.class, PacketFileFragment.class, PacketPlayABC.class, PacketPlayingTracks.class, PacketStopPlayingTrack.class, PacketInstrumentPlayerInfo.class, PacketCreateInstrument.class);
    }
}
