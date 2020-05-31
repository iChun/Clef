package me.ichun.mods.clef.client.core;

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
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

public class EventHandlerClient
{
    public HashSet<Track> tracksPlaying = new HashSet<>();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Minecraft mc = Minecraft.getInstance();
            if(!mc.isGamePaused())
            {
                tracksPlaying.removeIf(track -> !track.update());
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        Minecraft.getInstance().execute(this::disconnectFromServer);
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

    public Track findTrackByBand(String bandName)
    {
        for(Track track : tracksPlaying)
        {
            if(track.getBandName().equalsIgnoreCase(bandName))
            {
                return track;
            }
        }
        return null;
    }

    public Track getTrackPlayedByPlayer(PlayerEntity player)
    {
        for(Track track : tracksPlaying)
        {
            if(track.players.containsKey(player) || track.playersNames.contains(player.getName().getUnformattedComponentText()))
            {
                return track;
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event)
    {
        ItemStack is = ItemInstrument.getUsableInstrument(event.getPlayer());
        if(!is.isEmpty() && !(!event.getPlayer().getHeldItemMainhand().isEmpty() && !event.getPlayer().getHeldItemOffhand().isEmpty()))
        {
            stopPlayingTrack(event.getPlayer());
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        ItemStack is = ItemInstrument.getUsableInstrument(event.getPlayer());
        if(!is.isEmpty() && !(!event.getPlayer().getHeldItemMainhand().isEmpty() && !event.getPlayer().getHeldItemOffhand().isEmpty()))
        {
            stopPlayingTrack(event.getPlayer());
        }
    }

    public void stopPlayingTrack(PlayerEntity player)
    {
        Track track = getTrackPlayedByPlayer(player);
        if(track != null)
        {
            Clef.channel.sendToServer(new PacketStopPlayingTrack(track.getId()));
        }
    }
}
