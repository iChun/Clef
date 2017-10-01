package me.ichun.mods.clef.common.inventory;

import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;

import javax.annotation.Nonnull;

public class ContainerInstrumentPlayer extends Container
{
    private final TileEntityInstrumentPlayer inventory;

    public ContainerInstrumentPlayer(TileEntityInstrumentPlayer inventory)
    {
        this.inventory = inventory;

        for(int l = 0; l < 3; ++l)
        {
            for(int k = 0; k < 3; ++k)
            {
                this.addSlotToContainer(new Slot(inventory, k + l * 3, 189 + k * 18, l * 18 + 136));
            }
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn)
    {
        return this.inventory.isUsableByPlayer(playerIn);
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
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
}
