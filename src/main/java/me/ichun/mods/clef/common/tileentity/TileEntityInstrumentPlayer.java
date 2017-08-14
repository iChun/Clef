package me.ichun.mods.clef.common.tileentity;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.TrackFile;
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

import javax.annotation.Nullable;
import java.util.ArrayList;

public class TileEntityInstrumentPlayer extends TileEntity
        implements ITickable, IInventory
{
    public int tries = 20;
    public ArrayList<TrackFile> tracks = new ArrayList<>();
    public ArrayList<String> pending_md5s = new ArrayList<>();
    public String bandName = "";
    public boolean syncPlay = true;
    public boolean syncTrack = false;
    public int repeat = 0;
    public boolean shuffle = true;

    public int playlistIndex = 0;

    private ItemStack[] contents = new ItemStack[9];
    public boolean previousRedstoneState;

    public TileEntityInstrumentPlayer()
    {
    }

    @Override
    public void update()
    {
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

        //TODO do stuff here.
    }

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

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.contents.length; ++i)
        {
            if (this.contents[i] != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                this.contents[i].writeToNBT(nbttagcompound);
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

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        this.contents = new ItemStack[this.getSizeInventory()];
        NBTTagList nbttaglist = tag.getTagList("Items", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < this.contents.length)
            {
                this.contents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
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

        bandName = tag.getString("bandName");
        syncPlay = tag.getBoolean("syncPlay");
        syncTrack = tag.getBoolean("syncTrack");
        repeat = tag.getInteger("repeat");
        shuffle = tag.getBoolean("shuffle");
    }

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

    @Nullable
    @Override
    public ItemStack getStackInSlot(int index)
    {
        return this.contents[index];
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.contents, index, count);

        if (itemstack != null)
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
    public void setInventorySlotContents(int index, @Nullable ItemStack stack)
    {
        this.contents[index] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
        {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return this.worldObj.getTileEntity(this.pos) == this && player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
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
        for (int i = 0; i < this.contents.length; ++i)
        {
            this.contents[i] = null;
        }
    }
}
