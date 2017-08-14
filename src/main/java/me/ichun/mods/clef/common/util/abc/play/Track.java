package me.ichun.mods.clef.common.util.abc.play;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.packet.PacketPlayingTracks;
import me.ichun.mods.clef.common.packet.PacketRequestFile;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;

public class Track
{
    private final String id;
    private final String band;
    private String md5;
    private TrackInfo track;

    public boolean isRemote;

    public int playProg;
    public boolean playing = true;
    public int timeToSilence = 0;

    public HashMap<Integer, HashSet<BlockPos>> instrumentPlayers = new HashMap<>();
    public HashSet<String> playersNames = new HashSet<>();
    public HashMap<EntityPlayer, Integer> players = new HashMap<>();

    public Track(String id, String band, String md5, @Nullable TrackInfo track, boolean isRemote)
    {
        this.md5 = md5;
        this.id = id;
        this.band = band;
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

    public String getBandName()
    {
        return band;
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

    //TODO popup title similarly to records when a track starts.
    public boolean update() //returns false if it's time to stop playing.
    {
        if(track == null)
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

        if(!playing || playProg > track.trackLength)
        {
            return false;
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
                EntityPlayer mcPlayer = iChunUtil.proxy.getMcPlayer();
                if(mcPlayer == null)
                {
                    return false;
                }
                Iterator<Map.Entry<EntityPlayer, Integer>> playerIte = players.entrySet().iterator();
                while(playerIte.hasNext())
                {
                    Map.Entry<EntityPlayer, Integer> e = playerIte.next();
                    EntityPlayer player = e.getKey();
                    if(player.isEntityAlive() && player.getDistanceToEntity(mcPlayer) < 256D)
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
                HashSet<BlockPos> poses = instrumentPlayers.get(mcPlayer.getEntityWorld().provider.getDimension());
                if(poses != null)
                {
                    for(BlockPos pos : poses)
                    {
                        if(mcPlayer.getDistance(pos.getX(), pos.getY(), pos.getZ()) < 256D)
                        {
                            TileEntity te = mcPlayer.worldObj.getTileEntity(pos);
                            if(te instanceof TileEntityInstrumentPlayer)
                            {
                                TileEntityInstrumentPlayer player = (TileEntityInstrumentPlayer)te;
                                for(int i = 0; i < 9; i++)
                                {
                                    ItemStack is = player.getStackInSlot(i);
                                    if(is != null && is.getItem() == Clef.itemInstrument && is.getTagCompound() != null)
                                    {
                                        Instrument instrument = InstrumentLibrary.getInstrumentByName(is.getTagCompound().getString("itemName"));
                                        if(instrument != null)
                                        {
                                            ArrayList<Note> notes = track.notes.get(playProg);
                                            for(Note note : notes)
                                            {
                                                note.playNote(this, playProg, instrument, player.getPos());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else
        {
            boolean update = false;
            Iterator<Map.Entry<EntityPlayer, Integer>> playerIte = players.entrySet().iterator();
            while(playerIte.hasNext())
            {
                Map.Entry<EntityPlayer, Integer> e = playerIte.next();
                EntityPlayer player = e.getKey();
                if(player.isEntityAlive())
                {
                    e.setValue(e.getValue() + 1);

                    ItemStack is = ItemHandler.getUsableDualHandedItem(player);
                    if(is != null && is.getItem() == Clef.itemInstrument)
                    {
                        NBTTagCompound tag = is.getTagCompound();
                        if(tag != null)
                        {
                            Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
                            if(instrument != null)
                            {
                                e.setValue(0);
                            }
                        }
                    }
                }
                else
                {
                    e.setValue(10000000);
                }

                if(e.getValue() > 100) //5 seconds
                {
                    update = true;
                    playerIte.remove();
                }
            }

            if(update)
            {
                Clef.channel.sendToAll(new PacketPlayingTracks(this));
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

    public boolean hasObjectsPlaying()
    {
        for(HashSet<BlockPos> list : instrumentPlayers.values())
        {
            if(!list.isEmpty())
            {
                return true;
            }
        }
        return !playersNames.isEmpty() || !players.isEmpty();
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Track)
        {
            return id.equals(((Track)o).id);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
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
                    players.put(player, 0);
                    ite.remove();
                }
            }
        }
    }
}
