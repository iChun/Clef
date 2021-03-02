package me.ichun.mods.clef.common.tileentity;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.inventory.ContainerInstrumentPlayer;
import me.ichun.mods.clef.common.packet.PacketPlayingTracks;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.BaseTrackFile;
import me.ichun.mods.clef.common.util.abc.PendingTrackFile;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TileEntityInstrumentPlayer extends TileEntity
        implements ITickableTileEntity, IInventory, INamedContainerProvider
{
    public ArrayList<BaseTrackFile> tracks = new ArrayList<>();
    public String bandName = "";
    public boolean syncPlay = true;
    public boolean syncTrack = false;
    public int repeat = 0; //0 = none, 1 = all, 2 = one
    public boolean shuffle = true;

    public int playlistIndex = 0;
    public HashSet<String> playedTracks = new HashSet<>(); //for shuffle

    private NonNullList<ItemStack> contents = NonNullList.withSize(9, ItemStack.EMPTY); //TODO properly handle ItemStack.EMPTY
    public boolean previousRedstoneState;

    public boolean justCreatedInstrument;
    public Track lastTrack;

    public TileEntityInstrumentPlayer()
    {
        super(Clef.TileEntityTypes.INSTRUMENT_PLAYER.get());
    }

    @Override
    public void tick()
    {
        if(!getWorld().isRemote)
        {
            if(justCreatedInstrument)
            {
                justCreatedInstrument = false;
            }
            if(iChunUtil.eventHandlerServer.ticks % 100 == 0 && !tracks.stream().allMatch(BaseTrackFile::isSynced)) //Only tries every 5 seconds, pending_md5s is only populated server end.
            {
                Set<BaseTrackFile> toRemove = null;
                int size = tracks.size();
                for (int i = 0; i < size; i++)
                {
                    BaseTrackFile baseTrackFile = tracks.get(i);
                    if (!baseTrackFile.isSynced()) {
                        PendingTrackFile pending = (PendingTrackFile) baseTrackFile;
                        TrackFile file = pending.resolve();
                        if (file != null)
                        {
                            tracks.set(i, file); //replace the pending with the actual file
                        }
                        else if (pending.getResolveTries() > 20)
                        {
                            if (toRemove == null)
                            {
                                toRemove = new HashSet<>();
                            }
                            toRemove.add(pending);
                        }
                    }
                }
                if (toRemove != null)
                {
                    tracks.removeAll(toRemove);
                }
            }

            if(previousRedstoneState)//aka isPowered
            {
                boolean hasInst = false;
                for(ItemStack is : contents)
                {
                    if(is != null && is.getItem() == Clef.Items.INSTRUMENT.get())
                    {
                        hasInst = true;
                        break;
                    }
                }
                if(hasInst)
                {
                    Track track2 = Clef.eventHandlerServer.getTrackPlayedByPlayer(this);
                    if(track2 == null && (shuffle && playedTracks.size() < tracks.size() || !shuffle && playlistIndex < tracks.size()))
                    {
                        //play a new track
                        BaseTrackFile file = null;
                        int index = -1;
                        if(shuffle)
                        {
                            int tries = 0;
                            while(tries < 1000)
                            {
                                index = getWorld().rand.nextInt(tracks.size());
                                file = tracks.get(index);
                                if ((tries < 500 && !file.isSynced()) || playedTracks.contains(file.md5))
                                {
                                    tries++;
                                }
                                else
                                {
                                    break;
                                }
                            }
                        }
                        else
                        {
                            index = playlistIndex;
                            file = tracks.get(playlistIndex);
                        }
                        if (file == null)
                        {
                            //No valid file found! Just return
                            return;
                        }
                        if (!file.isSynced())
                        {
                            //uh oh, that file is not synced or we did not find a valid file...
                            //try and see if it is available now
                            TrackFile newFile = AbcLibrary.getTrack(file.md5);
                            if (newFile != null)
                            {
                                //we got lucky, the track is synced. Replace the entry in the list and play it
                                if (index != -1)
                                {
                                    tracks.set(index, newFile);
                                }
                                file = newFile;
                            }
                            else
                            {
                                //Well shit, not found, just wait until we receive it or the timer runs out
                                return;
                            }
                        }
                        Track track;
                        if(!bandName.isEmpty())
                        {
                            //Find the band
                            track = Clef.eventHandlerServer.findTrackByBand(bandName);
                            Track track1 = track;
                            if(track == null || !syncTrack) //No band
                            {
                                track = new Track(RandomStringUtils.randomAscii(IOUtil.IDENTIFIER_LENGTH), bandName, file, false);
                            }
                            if(syncPlay && track1 != null)
                            {
                                track.playAtProgress(track1.playProg);
                            }

                            if(lastTrack != null && lastTrack.getBandName().equals(bandName))
                            {
                                track.players = lastTrack.players;
                            }
                        }
                        else
                        {
                            track = new Track(RandomStringUtils.randomAscii(IOUtil.IDENTIFIER_LENGTH), bandName, file, false);
                        }

                        Clef.eventHandlerServer.tracksPlaying.add(track);
                        HashSet<BlockPos> players = track.instrumentPlayers.computeIfAbsent(world.getDimensionKey().getLocation(), v -> new HashSet<>());
                        if(players.add(getPos()))
                        {
                            Clef.channel.sendTo(new PacketPlayingTracks(track), PacketDistributor.ALL.noArg());
                        }

                        lastTrack = track;

                        if(shuffle && repeat != 1 && file.md5.equals(track.getTrackFile().md5))
                        {
                            playedTracks.add(track.getTrackFile().md5);
                        }
                        playlistIndex++;
                        if(repeat == 1 && playlistIndex >= tracks.size())
                        {
                            playlistIndex = 0;
                        }
                    }
                    else if(!bandName.isEmpty() && syncTrack)
                    {
                        //Find the band
                        Track track = Clef.eventHandlerServer.findTrackByBand(bandName);
                        if(track != null)
                        {
                            //getRegistryKey().toResourceLocation()
                            HashSet<BlockPos> players = track.instrumentPlayers.computeIfAbsent(world.getDimensionKey().getLocation(), v -> new HashSet<>());
                            if(players.add(getPos()))
                            {
                                Clef.channel.sendTo(new PacketPlayingTracks(track), PacketDistributor.ALL.noArg());
                            }
                        }
                    }
                }
            }
        }
    }

    public void changeRedstoneState(boolean newState)
    {
        if(newState)
        {
            playedTracks.clear();
            if(repeat != 2)
            {
                if(shuffle && !tracks.isEmpty())
                {
                    playlistIndex = getWorld().rand.nextInt(tracks.size());
                }
                else
                {
                    playlistIndex = 0;
                }
            }
        }
        else
        {
            Track track = Clef.eventHandlerServer.getTrackPlayedByPlayer(this);
            if(track != null)
            {
                HashSet<BlockPos> players = track.instrumentPlayers.get(world.getDimensionKey().getLocation());
                if(players != null)
                {
                    players.remove(getPos());
                    if(players.isEmpty())
                    {
                        track.instrumentPlayers.remove(world.getDimensionKey().getLocation());
                    }
                }
                if(!track.hasObjectsPlaying())
                {
                    track.stop();
                }
                Clef.channel.sendTo(new PacketPlayingTracks(track), PacketDistributor.ALL.noArg());
            }
        }
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(getPos(), 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        read(null, pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT write(CompoundNBT tag)
    {
        super.write(tag);

        ItemStackHelper.saveAllItems(tag, this.contents);
        tag.putBoolean("powered", this.previousRedstoneState);

        tag.putInt("trackCount", tracks.size());
        for(int i = 0 ; i < tracks.size(); i++)
        {
            BaseTrackFile file = tracks.get(i);
            tag.putString("track_" + i, file.md5);
            tag.putString("track_title_" + i, file.getTitle());
        }

        tag.putString("bandName", bandName);
        tag.putBoolean("syncPlay", syncPlay);
        tag.putBoolean("syncTrack", syncTrack);
        tag.putInt("repeat", repeat);
        tag.putBoolean("shuffle", shuffle);

        tag.putInt("playedCount", playedTracks.size());
        int i = 0;
        for(String s : playedTracks)
        {
            tag.putString("played_" + i, s);
            i++;
        }

        tag.putInt("playlistIndex", playlistIndex);

        return tag;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag)
    {
        super.read(state, tag);
        this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, this.contents);

        this.previousRedstoneState = tag.getBoolean("powered");

        tracks.clear();
        int size = tag.getInt("trackCount");
        for(int i = 0; i < size; i++)
        {
            String md5 = tag.getString("track_" + i);
            String title = tag.contains("track_title_" + i, Constants.NBT.TAG_STRING) ? tag.getString("track_title_" + i) : null;
            TrackFile file = AbcLibrary.getTrack(md5);
            if(file != null)
            {
                tracks.add(file);
            }
            else
            {
                tracks.add(new PendingTrackFile(md5, title));
            }
        }
        size = tag.getInt("playedCount");
        for(int i = 0; i < size; i++)
        {
            playedTracks.add(tag.getString("played_" + i));
        }

        bandName = tag.getString("bandName");
        syncPlay = tag.getBoolean("syncPlay");
        syncTrack = tag.getBoolean("syncTrack");
        repeat = tag.getInt("repeat");
        shuffle = tag.getBoolean("shuffle");

        playlistIndex = MathHelper.clamp(tag.getInt("playlistIndex"), 0, tracks.size());
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TranslationTextComponent("clef.instrument_player");
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player)
    {
        return new ContainerInstrumentPlayer(id, playerInventory, () -> this);
    }

    @Override
    public int getSizeInventory()
    {
        return 9;
    }

    @Override
    public boolean isEmpty()
    {
        for (ItemStack itemstack : this.contents)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return this.contents.get(index);
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.contents, index, count);

        if (!itemstack.isEmpty())
        {
            this.markDirty();
        }

        return itemstack;
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return ItemStackHelper.getAndRemove(this.contents, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        this.contents.set(index, stack);

        if (stack.getCount() > this.getInventoryStackLimit())
        {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player)
    {
        return this.world.getTileEntity(this.pos) == this && player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return stack.getItem() == Clef.Items.INSTRUMENT.get();
    }

//    @Override
//    public int getField(int id)
//    {
//        return 0;
//    }
//
//    @Override
//    public void setField(int id, int value)
//    {
//    }
//
//    @Override
//    public int getFieldCount()
//    {
//        return 0;
//    }

    @Override
    public void clear()
    {
        this.contents.clear();
    }
}
