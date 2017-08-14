package me.ichun.mods.clef.client.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.core.ProxyCommon;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;

public class ProxyClient extends ProxyCommon
{
    @Override
    public void preInitMod()
    {
        super.preInitMod();

        Clef.eventHandlerClient = new EventHandlerClient();
        MinecraftForge.EVENT_BUS.register(Clef.eventHandlerClient);

        ModelLoader.setCustomModelResourceLocation(Clef.itemInstrument, 0, new ModelResourceLocation("clef:instrument", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Clef.blockInstrumentPlayer), 0, new ModelResourceLocation("clef:block_instrument_player", "inventory"));
    }

    @Override
    public void initMod()
    {
        SoundSystemReflect.init();
    }
}
