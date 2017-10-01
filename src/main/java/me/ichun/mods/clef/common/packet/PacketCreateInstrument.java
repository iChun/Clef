package me.ichun.mods.clef.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PacketCreateInstrument extends AbstractPacket
{
    public String instrumentName;
    public BlockPos pos;

    public PacketCreateInstrument(){}

    public PacketCreateInstrument(String s, BlockPos pos)
    {
        instrumentName = s;
        this.pos = pos;
    }

    @Override
    public void writeTo(ByteBuf buf)
    {
        PacketBuffer buff = new PacketBuffer(buf);
        ByteBufUtils.writeUTF8String(buf, instrumentName);
        buff.writeBlockPos(pos);
    }

    @Override
    public void readFrom(ByteBuf buf)
    {
        PacketBuffer buff = new PacketBuffer(buf);
        instrumentName = ByteBufUtils.readUTF8String(buf);
        pos = buff.readBlockPos();
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        if(Clef.config.creatableInstruments == 0)
        {
            return null;
        }

        TileEntity te = player.world.getTileEntity(pos);
        if(te instanceof TileEntityInstrumentPlayer)
        {
            TileEntityInstrumentPlayer player1 = (TileEntityInstrumentPlayer)te;
            boolean full = true;
            for(int i = 0; i < 9 ; i++)
            {
                if(player1.getStackInSlot(i).isEmpty())
                {
                    full = false;
                    break;
                }
            }
            if(full)
            {
                ItemStack is1 = player.getHeldItemMainhand();
                if(!is1.isEmpty() && is1.getItem() == Items.NAME_TAG && is1.hasDisplayName())
                {
                    Instrument ins = null;
                    for(Instrument instrument : InstrumentLibrary.instruments)
                    {
                        if(instrument.info.itemName.equals(instrumentName))
                        {
                            ins = instrument;
                            break;
                        }
                    }
                    if(ins == null)
                    {
                        if(Clef.config.creatableInstruments == 1 || Clef.config.creatableInstruments == 3)
                        {
                            InstrumentLibrary.requestInstrument(instrumentName, player);
                        }
                    }
                    else if(Clef.config.creatableInstruments < 2)
                    {
                        return null;
                    }

                    for(int i = 0; i < 9; i++)
                    {
                        player1.setInventorySlotContents(i, ItemStack.EMPTY);
                    }
                    ItemStack is = new ItemStack(Clef.itemInstrument, 1, 0);
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setString("itemName", instrumentName);
                    is.setTagCompound(tag);
                    InventoryHelper.spawnItemStack(player.world, pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D, is);
                    player.world.playSound(null, pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    player1.markDirty();
                    player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                    player.inventory.markDirty();
                    player1.justCreatedInstrument = true;
                }
            }
        }
            return null;
    }

    @Override
    public Side receivingSide()
    {
        return Side.SERVER;
    }
}
