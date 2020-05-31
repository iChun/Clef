package me.ichun.mods.clef.common.packet;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

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
    public void writeTo(PacketBuffer buf)
    {
        buf.writeString(instrumentName);
        buf.writeBlockPos(pos);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        instrumentName = readString(buf);
        pos = buf.readBlockPos();
    }

    @Override
    public void process(NetworkEvent.Context context) //receivingSide() SERVER
    {
        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if(Clef.configCommon.creatableInstruments == 0)
            {
                return;
            }

            TileEntity te = player.world.getTileEntity(pos);
            if(te instanceof TileEntityInstrumentPlayer)
            {
                TileEntityInstrumentPlayer instrumentPlayer = (TileEntityInstrumentPlayer)te;
                boolean full = true;
                for(int i = 0; i < 9 ; i++)
                {
                    if(instrumentPlayer.getStackInSlot(i) == null)
                    {
                        full = false;
                        break;
                    }
                }
                if(full)
                {
                    ItemStack is1 = player.getHeldItemMainhand();
                    if(is1.getItem() == Items.NAME_TAG && is1.hasDisplayName())
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
                            if(Clef.configCommon.creatableInstruments == 1 || Clef.configCommon.creatableInstruments == 3)
                            {
                                InstrumentLibrary.requestInstrument(instrumentName, player);
                            }
                        }
                        else if(Clef.configCommon.creatableInstruments < 2)
                        {
                            return;
                        }

                        for(int i = 0; i < 9; i++)
                        {
                            instrumentPlayer.setInventorySlotContents(i, ItemStack.EMPTY); //TODO change to ItemStack.EMPTY
                        }
                        ItemStack is = new ItemStack(Clef.Items.INSTRUMENT.get());
                        CompoundNBT tag = new CompoundNBT();
                        tag.putString("itemName", instrumentName);
                        is.setTag(tag);
                        InventoryHelper.spawnItemStack(player.world, pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D, is);
                        player.world.playSound(null, pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                        instrumentPlayer.markDirty();
                        player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                        player.inventory.markDirty();
                        instrumentPlayer.justCreatedInstrument = true;
                    }
                }
            }
        });
    }
}
