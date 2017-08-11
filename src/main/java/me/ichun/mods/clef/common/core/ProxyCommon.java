package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import net.minecraftforge.common.MinecraftForge;

public class ProxyCommon
{
    public void preInitMod()
    {
        AbcLibrary.init();
        InstrumentLibrary.init();

        Clef.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(Clef.eventHandlerServer);
    }

    public void initMod(){}
}
