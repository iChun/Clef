package me.ichun.mods.clef.client.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.core.ProxyCommon;
import net.minecraftforge.common.MinecraftForge;

public class ProxyClient extends ProxyCommon
{
    @Override
    public void preInitMod()
    {
        super.preInitMod();

        Clef.eventHandlerClient = new EventHandlerClient();
        MinecraftForge.EVENT_BUS.register(Clef.eventHandlerClient);
    }
}
