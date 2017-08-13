package me.ichun.mods.clef.common.util.abc.play;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.packet.PacketPlayingTracks;
import me.ichun.mods.clef.common.packet.PacketRequestFile;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.play.components.Note;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.item.ItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class Track
{
    private final String id;
    private String md5;
    private TrackInfo track;

    public boolean isRemote;

    public int playProg;
    public boolean playing = true;
    public int timeToSilence = 0;

    private HashSet<String> playersNames = new HashSet<>();
    public HashSet<EntityPlayer> players = new HashSet<>();
    //TODO tell server we're trying to stop playing our shit.

    public Track(String id, String md5, @Nullable TrackInfo track, boolean isRemote)
    {
        this.md5 = md5;
        this.id = id;
        this.track = track;
        this.isRemote = isRemote;
    }

    public void addPlayer(String playerName)
    {
        playersNames.add(playerName);
    }

    public String getId()
    {
        return id;
    }

    public String getMd5()
    {
        return md5;
    }

    public void setTrack(String md5, TrackInfo track) //Only called by the server ever.
    {
        this.md5 = md5;
        this.track = track;
        if(!isRemote)
        {
            Clef.channel.sendToAll(new PacketPlayingTracks(this));
        }
    }

    public TrackInfo getTrack()
    {
        return track;
    }

    public boolean update() //returns false if it's time to stop playing.
    {
        if(!playing || playProg > track.trackLength)
        {
            return false;
        }

        if(track == null) //damnit idea yes, track can be null, don't be silly.
        {
            if(!isRemote)
            {
                return true; //We're still waiting for the track before we start playing.
            }
            else
            {
                if(AbcLibrary.requestedABCFromServer.add(md5))
                {
                    Clef.channel.sendToServer(new PacketRequestFile(md5, false));
                }
                playProg++;
                return true;
            }
        }

        if(isRemote)
        {
            if(timeToSilence > 0)
            {
                timeToSilence--;
            }

            findPlayers();

            if(track.notes.containsKey(playProg))
            {
                Iterator<EntityPlayer> playerIte = players.iterator();
                while(playerIte.hasNext())
                {
                    EntityPlayer player = playerIte.next();

                    if(player.isEntityAlive())
                    {
                        ItemStack is = ItemHandler.getUsableDualHandedItem(player);
                        if(is != null && is.getItem() == Clef.itemInstrument)
                        {
                            NBTTagCompound tag = is.getTagCompound();
                            if(tag != null)
                            {
                                Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
                                if(instrument != null)
                                {
                                    ArrayList<Note> notes = track.notes.get(playProg);
                                    for(Note note : notes)
                                    {
                                        int time = note.playNote(this, playProg, instrument, player);
                                        if(time > timeToSilence)
                                        {
                                            timeToSilence = time;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        playersNames.add(player.getName());
                        playerIte.remove();
                    }
                }
            }
        }

        playProg++;
        return true;
    }

    public void stop()
    {
        playing = false;
    }

    public void playAtProgress(int i)
    {
        playProg = i;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Track)
        {
            return ((Track)o).id.equals(id);
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void findPlayers()
    {
        Iterator<String> ite = playersNames.iterator();
        while(ite.hasNext())
        {
            String s = ite.next();
            if(Minecraft.getMinecraft().theWorld != null)
            {
                EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(s);
                if(player != null && player.isEntityAlive())
                {
                    players.add(player);
                    ite.remove();
                }
            }
        }
    }
}
