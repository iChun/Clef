package me.ichun.mods.clef.common.util.abc.play;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.item.ItemInstrument;
import me.ichun.mods.clef.common.packet.PacketPlayingTracks;
import me.ichun.mods.clef.common.packet.PacketRequestFile;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.play.components.Note;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;

public class Track
{
    public static final int MAX_TRACKING_RANGE = 48;
    private final String id;
    private final String band;
    private String md5;
    private TrackInfo track;

    public boolean isRemote;

    public int playProg;
    public boolean playing = true;
    public int timeToSilence = 0;

    public HashMap<ResourceLocation, HashSet<BlockPos>> instrumentPlayers = new HashMap<>();
    public HashSet<String> playersNames = new HashSet<>();
    public IdentityHashMap<PlayerEntity, Integer> players = new IdentityHashMap<>();
    public IntOpenHashSet zombies = new IntOpenHashSet();

    @OnlyIn(Dist.CLIENT)
    private TrackTracker trackTracker;

    public Track(String id, String band, String md5, @Nullable TrackInfo track, boolean isRemote)
    {
        this.md5 = md5;
        this.id = id;
        this.band = band;
        this.track = track;
        this.isRemote = isRemote;

        if(isRemote)
        {
            trackTracker = new TrackTracker(this);
        }
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
            Clef.channel.sendTo(new PacketPlayingTracks(this), PacketDistributor.ALL.noArg());
        }
    }

    public TrackInfo getTrack()
    {
        return track;
    }

    public boolean tick() //returns false if it's time to stop playing.
    {
        if(track == null)
        {
            if(!isRemote)
            {
                return true; //We're still waiting for the track before we start playing.
            }
            else
            {
                if(shouldRequestTrack() && AbcLibrary.requestedABCFromServer.add(md5))
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
            trackTracker.startNewTick(playProg);
            if(playProg == 0 && Clef.configClient.showRecordPlayingMessageForTracks)
            {
                showNowPlaying();
            }
            if(timeToSilence > 0)
            {
                timeToSilence--;
            }

            findPlayers();

            if(track.notes.containsKey(playProg))
            {
                PlayerEntity mcPlayer = EntityHelper.getClientPlayer();
                if(mcPlayer == null)
                {
                    return false;
                }
                Iterator<Map.Entry<PlayerEntity, Integer>> playerIte = players.entrySet().iterator();
                while(playerIte.hasNext())
                {
                    Map.Entry<PlayerEntity, Integer> e = playerIte.next();
                    PlayerEntity player = e.getKey();
                    if(player.isAlive() && player.getDistance(mcPlayer) < MAX_TRACKING_RANGE)
                    {
                        ItemStack is = ItemInstrument.getUsableInstrument(player);
                        if(!is.isEmpty())
                        {
                            CompoundNBT tag = is.getTag();
                            if(tag != null)
                            {
                                Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
                                if(instrument != null)
                                {
                                    HashSet<Note>[] notes = track.notes.get(playProg);
                                    trackTracker.addTickInfo(new NotesTickInfo(player, instrument, notes, true));
                                }
                                else
                                {
                                    InstrumentLibrary.requestInstrument(tag.getString("itemName"), null);
                                }
                            }
                        }
                    }
                    else
                    {
                        playersNames.add(player.getName().getUnformattedComponentText());
                        playerIte.remove();
                    }
                }
                HashSet<BlockPos> poses = instrumentPlayers.get(mcPlayer.getEntityWorld().getDimensionKey().getLocation());
                if(poses != null)
                {
                    for(BlockPos pos : poses)
                    {
                        if(mcPlayer.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < (MAX_TRACKING_RANGE * MAX_TRACKING_RANGE))
                        {
                            TileEntity te = mcPlayer.world.getTileEntity(pos);
                            if(te instanceof TileEntityInstrumentPlayer)
                            {
                                TileEntityInstrumentPlayer player = (TileEntityInstrumentPlayer)te;
                                for(int i = 0; i < 9; i++)
                                {
                                    ItemStack is = player.getStackInSlot(i);
                                    if(is.getItem() == Clef.Items.INSTRUMENT.get() && is.getTag() != null)
                                    {
                                        Instrument instrument = InstrumentLibrary.getInstrumentByName(is.getTag().getString("itemName"));
                                        if(instrument != null)
                                        {
                                            HashSet<Note>[] notes = track.notes.get(playProg);
                                            trackTracker.addTickInfo(new NotesTickInfo(player.getPos(), instrument, notes));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Iterator<Integer> ite1 = zombies.iterator();
                while(ite1.hasNext())
                {
                    Integer i = ite1.next();
                    Entity ent = mcPlayer.world.getEntityByID(i);
                    if(ent instanceof ZombieEntity && ent.isAlive() && mcPlayer.getDistance(ent) < MAX_TRACKING_RANGE)
                    {
                        ItemStack is = ItemInstrument.getUsableInstrument((ZombieEntity)ent);
                        if(!is.isEmpty())
                        {
                            CompoundNBT tag = is.getTag();
                            if(tag != null)
                            {
                                Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
                                if(instrument != null)
                                {
                                    HashSet<Note>[] notes = track.notes.get(playProg);
                                    trackTracker.addTickInfo(new NotesTickInfo(ent, instrument, notes, false));
                                }
                                else
                                {
                                    InstrumentLibrary.requestInstrument(tag.getString("itemName"), null);
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
            Iterator<Map.Entry<PlayerEntity, Integer>> playerIte = players.entrySet().iterator();
            while(playerIte.hasNext())
            {
                Map.Entry<PlayerEntity, Integer> e = playerIte.next();
                PlayerEntity player = e.getKey();
                if(player.isAlive())
                {
                    e.setValue(e.getValue() + 1);

                    ItemStack is = ItemInstrument.getUsableInstrument(player);
                    if(!is.isEmpty())
                    {
                        Instrument instrument = InstrumentLibrary.getInstrumentByName(is.getTag().getString("itemName"));
                        if(instrument != null)
                        {
                            e.setValue(0);
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

            Iterator<Integer> z = zombies.iterator();
            while(z.hasNext())
            {
                Integer i = z.next();
                if(Math.random() < 0.001F)
                {
                    z.remove();
                    update = true;
                }
            }

            if(update)
            {
                if(!hasObjectsPlaying())
                {
                    stop();
                }
                Clef.channel.sendTo(new PacketPlayingTracks(this), PacketDistributor.ALL.noArg());
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
        return !playersNames.isEmpty() || !players.isEmpty() || !zombies.isEmpty();
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof Track && id.equals(((Track)o).id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @OnlyIn(Dist.CLIENT)
    public void findPlayers()
    {
        Iterator<String> ite = playersNames.iterator();
        while(ite.hasNext())
        {
            String s = ite.next();
            if(Minecraft.getInstance().world != null)
            {
                for(AbstractClientPlayerEntity player : Minecraft.getInstance().world.getPlayers())
                {
                    if(player.getName().getUnformattedComponentText().equals(s) && player.isAlive())
                    {
                        players.put(player, 0);
                        ite.remove();
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void showNowPlaying()
    {
        Minecraft.getInstance().ingameGUI./*setRecordPlayingMessage*/func_238451_a_(new StringTextComponent(track.getTitle()));
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRequestTrack()
    {
        ClientWorld world = Minecraft.getInstance().world;
        if (world == null)
        {
            return false;
        }
        HashSet<BlockPos> poses = instrumentPlayers.get(world.getDimensionKey().getLocation());
        if(poses != null)
        {
            for(BlockPos pos : poses)
            {
                if(Minecraft.getInstance().player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < (MAX_TRACKING_RANGE * MAX_TRACKING_RANGE))
                {
                    return true;
                }
            }
        }

        for(String s : playersNames)
        {
            if(Minecraft.getInstance().world != null)
            {
                for(AbstractClientPlayerEntity player : Minecraft.getInstance().world.getPlayers())
                {
                    if(player.getName().getUnformattedComponentText().equals(s) && player.isAlive())
                    {
                        return true;
                    }
                }
            }
        }

        for(Integer i : zombies)
        {
            if(Minecraft.getInstance().world != null)
            {
                Entity ent = Minecraft.getInstance().world.getEntityByID(i);
                if(ent != null && ent.isAlive())
                {
                    return true;
                }
            }
        }

        return false;
    }
}
