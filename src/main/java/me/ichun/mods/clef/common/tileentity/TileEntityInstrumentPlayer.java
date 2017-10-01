package me.ichun.mods.clef.common.tileentity;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.packet.PacketPlayingTracks;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.ichunutil.common.core.util.IOUtil;
import me.ichun.mods.ichunutil.common.iChunUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TileEntityInstrumentPlayer extends TileEntity
        implements ITickable, IInventory
{
    public int tries = 20;
    public ArrayList<TrackFile> tracks = new ArrayList<>();
    public ArrayList<String> pending_md5s = new ArrayList<>();
    public String bandName = "";
    public boolean syncPlay = true;
    public boolean syncTrack = false;
    public int repeat = 0; //0 = none, 1 = all, 2 = one
    public boolean shuffle = true;

    public int playlistIndex = 0;
    public HashSet<String> playedTracks = new HashSet<>(); //for shuffle

    private List<ItemStack> contents = NonNullList.withSize(9, ItemStack.EMPTY);
    public boolean previousRedstoneState;

    public boolean justCreatedInstrument;
    public Track lastTrack;

    public TileEntityInstrumentPlayer()
    {
    }

    @Override
    public void update()
    {
        if(!getWorld().isRemote)
        {
            if(justCreatedInstrument)
            {
                justCreatedInstrument = false;
            }
            if(!pending_md5s.isEmpty() && iChunUtil.eventHandlerServer.ticks % 100 == 0) //Only tries every 5 seconds, pending_md5s is only populated server end.
            {
                tries++;
                for(String s : pending_md5s)
                {
                    TrackFile file = AbcLibrary.getTrack(s);
                    if(file == null && tries < 20)
                    {
                        tracks.clear();
                        return;
                    }
                    else
                    {
                        tracks.add(file);
                    }
                }
                pending_md5s.clear();//It'll only get to this point if it gets all teh tracks.
            }

            if(previousRedstoneState)//aka isPowered
            {
                boolean hasInst = false;
                for(ItemStack is : contents)
                {
                    if(!is.isEmpty() && is.getItem() == Clef.itemInstrument)
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
                        TrackFile file = null;
                        if(shuffle)
                        {
                            int tries = 0;
                            while(tries < 1000)
                            {
                                file = tracks.get(getWorld().rand.nextInt(tracks.size()));
                                if(!playedTracks.contains(file.md5))
                                {
                                    break;
                                }
                                tries++;
                            }
                        }
                        else
                        {
                            file = tracks.get(playlistIndex);
                        }
                        Track track;
                        if(!bandName.isEmpty())
                        {
                            //Find the band
                            track = Clef.eventHandlerServer.findTrackByBand(bandName);
                            Track track1 = track;
                            if(track == null || !syncTrack) //No band
                            {
                                track = new Track(RandomStringUtils.randomAscii(IOUtil.IDENTIFIER_LENGTH), bandName, file.md5, file.track, false);
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
                            track = new Track(RandomStringUtils.randomAscii(IOUtil.IDENTIFIER_LENGTH), bandName, file.md5, file.track, false);
                        }

                        Clef.eventHandlerServer.tracksPlaying.add(track);
                        HashSet<BlockPos> players = track.instrumentPlayers.computeIfAbsent(getWorld().provider.getDimension(), v -> new HashSet<>());
                        if(players.add(getPos()))
                        {
                            Clef.channel.sendToAll(new PacketPlayingTracks(track));
                        }

                        lastTrack = track;

                        if(shuffle && repeat != 1 && file.md5.equals(track.getMd5()))
                        {
                            playedTracks.add(track.getMd5());
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
                            HashSet<BlockPos> players = track.instrumentPlayers.computeIfAbsent(getWorld().provider.getDimension(), v -> new HashSet<>());
                            if(players.add(getPos()))
                            {
                                Clef.channel.sendToAll(new PacketPlayingTracks(track));
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
                HashSet<BlockPos> players = track.instrumentPlayers.get(getWorld().provider.getDimension());
                if(players != null)
                {
                    players.remove(getPos());
                    if(players.isEmpty())
                    {
                        track.instrumentPlayers.remove(getWorld().provider.getDimension());
                    }
                }
                if(!track.hasObjectsPlaying())
                {
                    track.stop();
                }
                Clef.channel.sendToAll(new PacketPlayingTracks(track));
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.getNbtCompound());
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.contents.size(); ++i)
        {
            if (!this.contents.get(i).isEmpty())
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                this.contents.get(i).writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        tag.setTag("Items", nbttaglist);
        tag.setBoolean("powered", this.previousRedstoneState);

        tag.setInteger("trackCount", tracks.size());
        for(int i = 0 ; i < tracks.size(); i++)
        {
            TrackFile file = tracks.get(i);
            tag.setString("track_" + i, file.md5);
        }

        tag.setString("bandName", bandName);
        tag.setBoolean("syncPlay", syncPlay);
        tag.setBoolean("syncTrack", syncTrack);
        tag.setInteger("repeat", repeat);
        tag.setBoolean("shuffle", shuffle);

        tag.setInteger("playedCount", playedTracks.size());
        int i = 0;
        for(String s : playedTracks)
        {
            tag.setString("played_" + i, s);
            i++;
        }

        tag.setInteger("playlistIndex", playlistIndex);

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        NBTTagList nbttaglist = tag.getTagList("Items", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < this.contents.size())
            {
                this.contents.set(j, new ItemStack(nbttagcompound));
            }
        }
        this.previousRedstoneState = tag.getBoolean("powered");

        tracks.clear();
        int size = tag.getInteger("trackCount");
        for(int i = 0; i < size; i++)
        {
            TrackFile file = AbcLibrary.getTrack(tag.getString("track_" + i));
            if(file != null)
            {
                tracks.add(file);
            }
        }
        size = tag.getInteger("playedCount");
        for(int i = 0; i < size; i++)
        {
            playedTracks.add(tag.getString("played_" + i));
        }

        bandName = tag.getString("bandName");
        syncPlay = tag.getBoolean("syncPlay");
        syncTrack = tag.getBoolean("syncTrack");
        repeat = tag.getInteger("repeat");
        shuffle = tag.getBoolean("shuffle");

        playlistIndex = MathHelper.clamp(tag.getInteger("playlistIndex"), 0, tracks.size());
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "clef.instrument_player";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public int getSizeInventory()
    {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        boolean empty = true;
        for (ItemStack stack : this.contents)
        {
            if (!stack.isEmpty())
            {
                empty = false;
            }
        }
        return empty;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int index)
    {
        return this.contents.get(index);
    }

    @Nonnull
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

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return ItemStackHelper.getAndRemove(this.contents, index);
    }

    @Override
    public void setInventorySlotContents(int index, @Nonnull ItemStack stack)
    {
        this.contents.set(index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit())
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
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player)
    {
        return this.world.getTileEntity(this.pos) == this && player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player)
    {
    }

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack)
    {
        return stack.getItem() == Clef.itemInstrument;
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {
        this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
    }
}
