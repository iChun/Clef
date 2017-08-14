package me.ichun.mods.clef.common.item;

import me.ichun.mods.clef.client.gui.GuiPlayTrack;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.item.DualHandedItemCallback;
import me.ichun.mods.ichunutil.common.item.ItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemInstrument extends Item
{
    public ItemInstrument()
    {
        maxStackSize = 1;
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack is, World world, EntityPlayer player, EnumHand hand)
    {
        if(is.getTagCompound() == null && !world.isRemote)
        {
            InstrumentLibrary.assignRandomInstrument(is);
        }
        if(ItemHandler.canItemBeUsed(player, is))
        {
            if(player.worldObj.isRemote)
            {
                Track track = Clef.eventHandlerClient.getTrackPlayedByPlayer(player);
                if(track == null)
                {
                    //Open the GUI
                    openGui();
                }
                else
                {
                    Clef.eventHandlerClient.stopPlayingTrack(player);
                }
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, is);
        }
        return new ActionResult<>(EnumActionResult.FAIL, is);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
    {
        Clef.eventHandlerClient.stopPlayingTrack(player);
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void openGui()
    {
        FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiPlayTrack());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        if(tab == null || tab == Clef.creativeTabInstruments)
        {
            for(Instrument intrument : InstrumentLibrary.instruments)
            {
                ItemStack stack = new ItemStack(itemIn, 1, 0);
                NBTTagCompound stackTag = new NBTTagCompound();
                stackTag.setString("itemName", intrument.info.itemName);
                stack.setTagCompound(stackTag);
                subItems.add(stack);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack is, EntityPlayer player, List<String> list, boolean flag)
    {
        NBTTagCompound tag = is.getTagCompound();
        if(tag != null)
        {
            Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
            if(instrument != null)
            {
                list.add(I18n.translateToLocal("item.clef.instrument." + instrument.info.itemName + ".desc"));
            }
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack is)
    {
        NBTTagCompound tag = is.getTagCompound();
        if(tag != null)
        {
            Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
            if(instrument != null)
            {
                if(I18n.translateToLocal("item.clef.instrument." + instrument.info.itemName + ".name").equalsIgnoreCase("item.clef.instrument." + instrument.info.itemName + ".name"))
                {
                    InstrumentLibrary.injectLocalization(instrument);
                }
                return "item.clef.instrument." + instrument.info.itemName;
            }
        }
        return super.getUnlocalizedName(is);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return Integer.MAX_VALUE;
    }

    public static class DualHandedInstrumentCallback extends DualHandedItemCallback
    {
        @Override
        public boolean shouldItemBeHeldLikeBow(ItemStack is, EntityLivingBase ent)
        {
            if(ent instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer)ent;
                Track track = Clef.eventHandlerClient.getTrackPlayedByPlayer(player);
                if(track != null)
                {
                    return track.timeToSilence > 0;
                }
            }
            return false;
        }
    }
}
