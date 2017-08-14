package me.ichun.mods.clef.common.network;

import me.ichun.mods.clef.client.gui.GuiPlayTrackBlock;
import me.ichun.mods.clef.common.inventory.ContainerInstrumentPlayer;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiPlayTrackBlockHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if(te instanceof TileEntityInstrumentPlayer)
        {
            return new ContainerInstrumentPlayer((TileEntityInstrumentPlayer)te);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if(te instanceof TileEntityInstrumentPlayer)
        {
            return new GuiPlayTrackBlock((TileEntityInstrumentPlayer)te);
        }
        return null;
    }
}
