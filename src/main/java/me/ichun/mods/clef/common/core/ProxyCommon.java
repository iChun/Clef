package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.block.BlockInstrumentPlayer;
import me.ichun.mods.clef.common.item.ItemInstrument;
import me.ichun.mods.clef.common.network.GuiPlayTrackBlockHandler;
import me.ichun.mods.clef.common.packet.*;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.item.ItemHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProxyCommon
{
    public void preInitMod()
    {
        AbcLibrary.init();
        InstrumentLibrary.init();

        Clef.itemInstrument = GameRegistry.register((new ItemInstrument()).setFull3D().setRegistryName("clef", "instrument").setUnlocalizedName("clef.item.instrument"));

        Clef.creativeTabInstruments = new CreativeTabs("clef") {
            @Override
            public Item getTabIconItem()
            {
                return Clef.itemInstrument;
            }
        };

        Clef.itemInstrument.setCreativeTab(Clef.creativeTabInstruments);

        Clef.blockInstrumentPlayer = GameRegistry.register(new BlockInstrumentPlayer().setRegistryName("clef", "block_instrument_player").setUnlocalizedName("clef.item.instrumentPlayer"));
        GameRegistry.register(new ItemBlock(Clef.blockInstrumentPlayer).setRegistryName(Clef.blockInstrumentPlayer.getRegistryName()));

        GameRegistry.registerTileEntity(TileEntityInstrumentPlayer.class, "Clef:InstrumentPlayer");

        NetworkRegistry.INSTANCE.registerGuiHandler(Clef.instance, new GuiPlayTrackBlockHandler());

        ItemHandler.registerDualHandedItem(ItemInstrument.class, new ItemInstrument.DualHandedInstrumentCallback());

        Clef.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(Clef.eventHandlerServer);

        Clef.channel = new PacketChannel("Clef", PacketRequestFile.class, PacketFileFragment.class, PacketPlayABC.class, PacketPlayingTracks.class, PacketStopPlayingTrack.class, PacketInstrumentPlayerInfo.class, PacketCreateInstrument.class);
    }

    public void initMod(){}

    public void postInitMod()
    {
    }
}
