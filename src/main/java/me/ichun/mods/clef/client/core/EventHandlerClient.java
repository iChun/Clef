package me.ichun.mods.clef.client.core;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.ichun.mods.clef.client.render.BakedModelInstrument;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.item.ItemInstrument;
import me.ichun.mods.clef.common.packet.PacketStopPlayingTrack;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

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
        tracksPlaying.remove(track);//Remove old instances
        if(track.hasObjectsPlaying() && track.playing)
        {
            tracksPlaying.add(track); //Add the new instance.
        }
    }

    public Track getTrackPlayedByPlayer(EntityPlayer player)
    {
        for(Track track : tracksPlaying)
        {
            if(track.players.containsKey(player) || track.playersNames.contains(player.getName()))
            {
                return track;
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event)
    {
        ItemStack is = ItemInstrument.getUsableInstrument(event.getEntityPlayer());
        if(is != null && !(event.getEntityPlayer().getHeldItemMainhand() != null && event.getEntityPlayer().getHeldItemOffhand() != null))
        {
            stopPlayingTrack(event.getEntityPlayer());
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        ItemStack is = ItemInstrument.getUsableInstrument(event.getEntityPlayer());
        if(is != null && !(event.getEntityPlayer().getHeldItemMainhand() != null && event.getEntityPlayer().getHeldItemOffhand() != null))
        {
            stopPlayingTrack(event.getEntityPlayer());
        }
    }

    public void stopPlayingTrack(EntityPlayer player)
    {
        Track track = getTrackPlayedByPlayer(player);
        if(track != null)
        {
            Clef.channel.sendToServer(new PacketStopPlayingTrack(track.getId()));
        }
    }
}
