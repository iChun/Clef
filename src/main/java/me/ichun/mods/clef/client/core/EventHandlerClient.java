package me.ichun.mods.clef.client.core;

import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class EventHandlerClient
{
    public boolean keyDown;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            if(keyDown && !Keyboard.isKeyDown(Keyboard.KEY_TAB))
            {
                AbcLibrary.init();
            }
            keyDown = Keyboard.isKeyDown(Keyboard.KEY_TAB);
        }
    }
}
