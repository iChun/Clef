package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.item.ItemInstrument;
import me.ichun.mods.clef.common.packet.PacketFileFragment;
import me.ichun.mods.clef.common.packet.PacketPlayABC;
import me.ichun.mods.clef.common.packet.PacketPlayingTracks;
import me.ichun.mods.clef.common.packet.PacketRequestFile;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.item.ItemHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
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

        ItemHandler.registerDualHandedItem(ItemInstrument.class, new ItemInstrument.DualHandedInstrumentCallback());

        Clef.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(Clef.eventHandlerServer);

        Clef.channel = new PacketChannel("Clef", PacketRequestFile.class, PacketFileFragment.class, PacketPlayABC.class, PacketPlayingTracks.class);
    }

    public void initMod(){}

    public void postInitMod()
    {
    }
}
