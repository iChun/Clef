package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.packet.PacketPlayingTracks;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.iChunUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.Iterator;

public class EventHandlerServer
{
    public HashSet<Track> tracksPlaying = new HashSet<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.side == Side.SERVER && event.phase == TickEvent.Phase.END)
        {
            if(iChunUtil.eventHandlerServer.ticks + 5 % 10 == 2)
            {
                ItemStack isMain = event.player.getHeldItemMainhand();
                ItemStack isOff = event.player.getHeldItemOffhand();
                if(isMain != null && isMain.getItem() == Clef.itemInstrument)
                {
                    InstrumentLibrary.checkForInstrument(isMain, event.player);
                }
                if(isOff != null && isOff.getItem() == Clef.itemInstrument)
                {
                    InstrumentLibrary.checkForInstrument(isOff, event.player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemDrop(PlayerDropsEvent event)
    {
        if(!event.getEntityPlayer().getEntityWorld().isRemote)
        {
            for(EntityItem item : event.getDrops())
            {
                if(item.getEntityItem().getItem() == Clef.itemInstrument)
                {
                    NBTTagCompound tag = item.getEntityItem().getTagCompound();
                    if(tag != null)
                    {
                        String instName = tag.getString("itemName");
                        Instrument is = InstrumentLibrary.getInstrumentByName(instName);
                        if(is == null) //request the item then?
                        {
                            InstrumentLibrary.requestInstrument(instName, event.getEntityPlayer());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
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

    public void stopPlayingTrack(EntityPlayer player, String trackId)
    {
        for(Track track : tracksPlaying)
        {
            if(track.getId().equals(trackId))
            {
                track.players.remove(player);
                if(track.players.isEmpty())
                {
                    track.stop();
                }
                Clef.channel.sendToAll(new PacketPlayingTracks(track));
                break;
            }
        }
    }

    public Track getTrackPlayedByPlayer(EntityPlayer player)
    {
        for(Track track : tracksPlaying)
        {
            if(track.players.containsKey(player))
            {
                return track;
            }
        }
        return null;
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

    @SubscribeEvent
    public void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event)
    {
        HashSet<Track> tracks = new HashSet<>();
        for(Track track : tracksPlaying)
        {
            if(track.getTrack() != null) //this means the track is actively played
            {
                tracks.add(track);
            }
        }
        AbcLibrary.startPlayingTrack(event.player, tracks.toArray(new Track[tracks.size()]));
    }

    public void shutdownServer()
    {
        tracksPlaying.clear();

        AbcLibrary.tracksWaitingForTrackInfo.clear();
        AbcLibrary.requestedABCFromPlayers.clear();
        InstrumentLibrary.requestsFromPlayers.clear();
        InstrumentLibrary.requestedInstrumentsFromPlayers.clear();
    }
}
