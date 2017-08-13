package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.iChunUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class EventHandlerServer
{
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.side == Side.SERVER && event.phase == TickEvent.Phase.END)
        {
            if(iChunUtil.eventHandlerServer.ticks + 5 % 10 == 2)
            {
                ItemStack isMain = event.player.getHeldItemMainhand();
                ItemStack isOff = event.player.getHeldItemOffhand();
                if(isMain != null && isMain.getItem() == Clef.itemInstrument)
                {
                    InstrumentLibrary.checkForInstrument(isMain, event.player);
                }
                if(isOff != null && isOff.getItem() == Clef.itemInstrument)
                {
                    InstrumentLibrary.checkForInstrument(isOff, event.player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemDrop(PlayerDropsEvent event)
    {
        if(!event.getEntityPlayer().getEntityWorld().isRemote)
        {
            for(EntityItem item : event.getDrops())
            {
                if(item.getEntityItem().getItem() == Clef.itemInstrument)
                {
                    NBTTagCompound tag = item.getEntityItem().getTagCompound();
                    if(tag != null)
                    {
                        String instName = tag.getString("itemName");
                        Instrument is = InstrumentLibrary.getInstrumentByName(instName);
                        if(is == null) //request the item then?
                        {
                            InstrumentLibrary.requestInstrument(instName, event.getEntityPlayer());
                        }
                    }
                }
            }
        }
    }

    public void removeRequests()
    {
        AbcLibrary.requestedABCFromPlayers.clear();
        InstrumentLibrary.requestsFromPlayers.clear();
        InstrumentLibrary.requestedInstrumentsFromPlayers.clear();
    }
}
