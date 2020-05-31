package me.ichun.mods.clef.common.inventory;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ContainerInstrumentPlayer extends Container
{
    @Nonnull
    public final TileEntityInstrumentPlayer inventory;

    public ContainerInstrumentPlayer(int id, PlayerInventory inv, PacketBuffer data)
    {
        this(id, inv, () -> {
            BlockPos pos = data.readBlockPos();
            TileEntity te = inv.player.world.getTileEntity(pos);
            if(!(te instanceof TileEntityInstrumentPlayer))
            {
                te = new TileEntityInstrumentPlayer();
                te.setPos(pos); //I think this is the important stuff?
            }
            return (TileEntityInstrumentPlayer)te;
        });
    }

    public ContainerInstrumentPlayer(int id, PlayerInventory inv, Supplier<TileEntityInstrumentPlayer> inventory)
    {
        super(Clef.ContainerTypes.INSTRUMENT_PLAYER.get(), id);
        this.inventory = inventory.get();

        for(int l = 0; l < 3; ++l)
        {
            for(int k = 0; k < 3; ++k)
            {
                this.addSlot(new Slot(this.inventory, k + l * 3, 189 + k * 18, l * 18 + 136));
            }
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return this.inventory.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player)
    {
        if(inventorySlots.get(slotId) != null)
        {
            if(!inventorySlots.get(slotId).getStack().isEmpty())
            {
                if(!player.world.isRemote)
                {
                    InventoryHelper.spawnItemStack(player.world, inventory.getPos().getX() + 0.5D, inventory.getPos().getY() + 1D, inventory.getPos().getZ() + 0.5D, inventorySlots.get(slotId).getStack());
                }
                player.world.playSound(null, inventory.getPos().getX() + 0.5D, inventory.getPos().getY() + 1D, inventory.getPos().getZ() + 0.5D, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                inventorySlots.get(slotId).putStack(ItemStack.EMPTY);
                inventory.setInventorySlotContents(slotId, ItemStack.EMPTY);
                inventory.markDirty();
            }
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.inventory.closeInventory(playerIn);
    }
}
