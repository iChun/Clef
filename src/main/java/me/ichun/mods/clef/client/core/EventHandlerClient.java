package me.ichun.mods.clef.client.core;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.ichun.mods.clef.client.render.BakedModelInstrument;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class EventHandlerClient
{
    public HashSet<Track> tracksPlaying = new HashSet<>();

    public TextureAtlasSprite txInstrument;

    @SubscribeEvent
    public void onTextureStitchedPre(TextureStitchEvent.Pre event)
    {
        txInstrument = Minecraft.getMinecraft().getTextureMapBlocks().registerSprite(new ResourceLocation("clef", "items/instrument"));
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event)
    {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
        builder.addAll(ItemLayerModel.getQuadsForSprite(0, txInstrument, DefaultVertexFormats.ITEM, Optional.absent()));
        event.getModelRegistry().putObject(new ModelResourceLocation("clef:instrument", "inventory"), new BakedModelInstrument(builder.build(), txInstrument, ImmutableMap.copyOf(new HashMap<>()), null, null));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if(!mc.isGamePaused())
            {
                Iterator<Track> ite = tracksPlaying.iterator();
                while(ite.hasNext())
                {
                    Track track = ite.next();
                    if(!track.update())
                    {
                        ite.remove();
                        continue;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        Minecraft.getMinecraft().addScheduledTask(this::disconnectFromServer);
    }

    public void disconnectFromServer()
    {
        tracksPlaying.clear();

        AbcLibrary.requestedABCFromServer.clear();
        InstrumentLibrary.requestedInstrumentsFromServer.clear();
    }

    public void addTrack(Track track)
    {
        tracksPlaying.remove(track); //Remove old instances
        tracksPlaying.add(track); //Add the new instance.
    }
}
