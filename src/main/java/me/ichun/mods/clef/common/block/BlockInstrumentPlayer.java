package me.ichun.mods.clef.common.block;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.packet.PacketCreateInstrument;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class BlockInstrumentPlayer extends ContainerBlock
{
    public BlockInstrumentPlayer()
    {
        super(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(0.8F)); //from Note Block
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader world)
    {
        return new TileEntityInstrumentPlayer();
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return createNewTileEntity(world);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
    {
        if(!worldIn.isRemote)
        {
            boolean flag = worldIn.isBlockPowered(pos);
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if(tileentity instanceof TileEntityInstrumentPlayer)
            {
                TileEntityInstrumentPlayer player = (TileEntityInstrumentPlayer)tileentity;

                if(player.previousRedstoneState != flag)
                {
                    player.changeRedstoneState(flag);
                    player.previousRedstoneState = flag;
                }
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof TileEntityInstrumentPlayer)
        {
            TileEntityInstrumentPlayer player = (TileEntityInstrumentPlayer)te;
            boolean hasSlot = false;
            ItemStack is = playerIn.getHeldItemMainhand();
            if(is.getItem() == Clef.Items.INSTRUMENT.get() && !playerIn.isSneaking())
            {
                if(is.getTag() == null && !worldIn.isRemote)
                {
                    InstrumentLibrary.assignRandomInstrument(is);
                }
                //Find a free slot
                for(int i = 0; i < player.getSizeInventory(); i++)
                {
                    ItemStack is1 = player.getStackInSlot(i);
                    if(is1.isEmpty())
                    {
                        hasSlot = true;
                        worldIn.playSound(null, pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                        if(!worldIn.isRemote)
                        {
                            player.setInventorySlotContents(i, is);
                            player.markDirty();
                            playerIn.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                            playerIn.inventory.markDirty();
                            worldIn.notifyBlockUpdate(pos, state, state, 3);
                        }
                        break;
                    }
                }
            }
            else if(is.getItem() == Items.NAME_TAG && is.hasDisplayName())
            {
                boolean full = true;
                for(int i = 0; i < 9 ; i++)
                {
                    if(player.getStackInSlot(i).isEmpty())
                    {
                        full = false;
                        break;
                    }
                }
                if(!full)
                {
                    return ActionResultType.PASS;
                }
                if(worldIn.isRemote)
                {
                    for(Instrument instrument : InstrumentLibrary.instruments)
                    {
                        if(instrument.info.itemName.equalsIgnoreCase(is.getDisplayName().getUnformattedComponentText()))
                        {
                            Clef.channel.sendToServer(new PacketCreateInstrument(instrument.info.itemName, pos));
                            break;
                        }
                    }
                }
                return ActionResultType.SUCCESS;
            }
            if(!playerIn.world.isRemote && !hasSlot && !player.justCreatedInstrument)
            {
                NetworkHooks.openGui((ServerPlayerEntity)playerIn, player, pos);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof TileEntityInstrumentPlayer)
            {
                InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityInstrumentPlayer)tileentity);
                worldIn.updateComparatorOutputLevel(pos, this);
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.MODEL;
    }
}
