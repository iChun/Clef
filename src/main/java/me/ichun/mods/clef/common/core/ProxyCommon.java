package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.item.ItemInstrument;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
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

        Clef.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(Clef.eventHandlerServer);
    }

    public void initMod(){}
}
