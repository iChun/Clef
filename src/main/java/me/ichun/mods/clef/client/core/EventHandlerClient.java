package me.ichun.mods.clef.client.core;

import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
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
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                    {
                        if(!tracksPlaying.isEmpty())
                        {
                            Instrument instrument = InstrumentLibrary.instruments.get((int)Math.floor(Math.random() * InstrumentLibrary.instruments.size()));
                            System.out.println("ADDING RANDOM INSTRUMENT: " + instrument.info.kind);
                            tracksPlaying.get(0).addInstrument(instrument);
                        }
                    }
                    else
                    {
                        for(int i = tracksPlaying.size() - 1; i >= 0; i--)
                        {
                            Track track = tracksPlaying.get(i);
                            track.stop();
                        }
                        tracksPlaying.clear();

                        TrackInfo trackInfo = AbcLibrary.tracks.get((int)(Math.floor(Math.random() * AbcLibrary.tracks.size()))).track;
                        tracksPlaying.add(new Track(trackInfo, InstrumentLibrary.instruments.get(8)));
                        System.out.println("PLAYING: " + trackInfo.title);
                    }
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
                    InstrumentLibrary.instruments.clear();
                    InstrumentLibrary.init();
                }
            }
            keyDown = Keyboard.isKeyDown(Keyboard.KEY_TAB);
        }
    }
}
