package me.ichun.mods.clef.common.util.abc.play;

import me.ichun.mods.clef.common.util.abc.play.components.Note;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;
import me.ichun.mods.clef.common.util.instrument.Instrument;

import java.util.ArrayList;

public class Track
{
    public int playProg;
    public boolean playing = true;

    public final TrackInfo track;
    public ArrayList<Instrument> instruments = new ArrayList<>();

    public Track(TrackInfo track, Instrument instrument)
    {
        this.track = track;
        this.instruments.add(instrument);
    }

    public void addInstrument(Instrument instrument)
    {
        instruments.add(instrument);
    }

    public boolean update() //returns false if it's time to stop playing.
    {
        if(!playing || playProg > track.trackLength)
        {
            return false;
        }

        for(Instrument i : instruments)
        {
            if(track.notes.containsKey(playProg))
            {
                ArrayList<Note> notes = track.notes.get(playProg);
                for(Note note : notes)
                {
                    note.playNote(this, playProg, i);
                }
            }
        }

        playProg++;
        return true;
    }

    public void stop()
    {
        playing = false;
    }

    public void playAtProgress(int i)
    {
        playProg = i;
    }
}
