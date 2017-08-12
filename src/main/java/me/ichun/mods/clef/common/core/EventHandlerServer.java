package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
