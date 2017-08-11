package me.ichun.mods.clef.client.core;

import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.play.Track;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class EventHandlerClient
{
    public ArrayList<Track> tracksPlaying = new ArrayList<>();

    public boolean keyDown;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            for(int i = tracksPlaying.size() - 1; i >= 0; i--)
            {
                Track track = tracksPlaying.get(i);
                if(!track.update())
                {
                    tracksPlaying.remove(i);
                }
            }

            if(keyDown && !Keyboard.isKeyDown(Keyboard.KEY_TAB))
            {
                if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
                {
                    tracksPlaying.add(new Track(AbcLibrary.tracks.get((int)(Math.floor(Math.random() * AbcLibrary.tracks.size()))).track));
                    System.out.println("PLAYING!");
                }
                else if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                {
                    for(int i = tracksPlaying.size() - 1; i >= 0; i--)
                    {
                        Track track = tracksPlaying.get(i);
                        track.stop();
                    }
                    tracksPlaying.clear();
                    System.out.println("CLEARING");
                }
                else
                {
                    AbcLibrary.init();
                }
            }
            keyDown = Keyboard.isKeyDown(Keyboard.KEY_TAB);
        }
    }
}
