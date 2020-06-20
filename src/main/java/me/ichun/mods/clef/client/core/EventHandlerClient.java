package me.ichun.mods.clef.client.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.item.ItemInstrument;
import me.ichun.mods.clef.common.packet.PacketStopPlayingTrack;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.play.NotePlayThread;
import me.ichun.mods.clef.common.util.abc.play.PlayedNote;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;

public class EventHandlerClient
{
    public HashSet<Track> tracksPlaying = new HashSet<>();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            Minecraft mc = Minecraft.getInstance();
            if(!mc.isGamePaused())
            {
                if (!tracksPlaying.isEmpty())
                {
                    boolean wasLocked = NotePlayThread.INSTANCE.startNewTick();
                    try
                    {
                        tracksPlaying.removeIf(track -> !track.tick());
                    }
                    finally
                    {
                        NotePlayThread.INSTANCE.endTick(wasLocked);
                    }
                }
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

    @SubscribeEvent
    public void onSoundSystemReload(SoundLoadEvent event)
    {
        //Clear the stored buffers (vanilla does that as well)
        boolean locked = NotePlayThread.INSTANCE.acquireLock();
        try
        {
            PlayedNote.clearCache();
        }
        finally
        {
            NotePlayThread.INSTANCE.releaseLock(locked);
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
