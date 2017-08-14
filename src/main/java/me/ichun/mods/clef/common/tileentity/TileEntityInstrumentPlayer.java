package me.ichun.mods.clef.common.tileentity;

import me.ichun.mods.clef.common.Clef;
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

public class TileEntityInstrumentPlayer extends TileEntity
        implements ITickable, IInventory
{
    private ItemStack[] contents = new ItemStack[9];
    public boolean previousRedstoneState;

    public TileEntityInstrumentPlayer()
    {
        //        for(int i = 0; i < contents.length; i++)
        //        {
        //            contents[i] = new ItemStack(Clef.itemInstrument, 1, 0);
        //            NBTTagCompound tag = new NBTTagCompound();
        //            tag.setString("itemName", "piano");
        //            contents[i].setTagCompound(tag);
        //        }
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
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
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

        compound.setTag("Items", nbttaglist);
        compound.setBoolean("powered", this.previousRedstoneState);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.contents = new ItemStack[this.getSizeInventory()];
        NBTTagList nbttaglist = compound.getTagList("Items", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < this.contents.length)
            {
                this.contents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }
        this.previousRedstoneState = compound.getBoolean("powered");
    }

    @Override
    public void update()
    {

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
