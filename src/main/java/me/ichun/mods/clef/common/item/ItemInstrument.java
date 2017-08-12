package me.ichun.mods.clef.common.item;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;
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
                stackTag.setString("kind", intrument.info.kind);
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
            Instrument instrument = InstrumentLibrary.getInstrumentByKind(tag.getString("kind"));
            if(instrument != null)
            {
                list.add(I18n.translateToLocal("item.clef.instrument." + instrument.info.kind + ".desc"));
            }
            else
            {
                list.add(I18n.translateToLocal("what"));//TODO localise this?
            }
        }
        else
        {
            list.add(I18n.translateToLocal("what"));//TODO localise this?
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack is)
    {
        NBTTagCompound tag = is.getTagCompound();
        if(tag != null)
        {
            Instrument instrument = InstrumentLibrary.getInstrumentByKind(tag.getString("kind"));
            return "item.clef.instrument." + instrument.info.kind;
        }
        else
        {
            return super.getUnlocalizedName(is);
        }
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

}
