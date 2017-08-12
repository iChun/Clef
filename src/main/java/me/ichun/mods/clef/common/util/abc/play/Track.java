package me.ichun.mods.clef.common.util.abc.play;

import me.ichun.mods.clef.common.util.abc.play.components.Note;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;
import me.ichun.mods.clef.common.util.instrument.Instrument;

import java.util.ArrayList;

public class Track
{
    public int beatOffset = 0;
    public int ticksPerBeat;
    public double currentMeter;
    public double unitNoteLength;
    public double key; //musical key

    public int playProg;
    public boolean playing = true;

    public final TrackInfo track;
    public ArrayList<Instrument> instruments = new ArrayList<>();

    public ArrayList<PlayedNote> playingNotes = new ArrayList<>();

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
            stop();
            return false;
        }

        for(Instrument i : instruments)
        {
            if(track.notes.containsKey(playProg))
            {
                ArrayList<Note> notes = track.notes.get(playProg);
                for(Note note : notes)
                {
                    note.playNote(this, playingNotes, playProg, i);
                }
            }
        }

        for(int i = playingNotes.size() - 1; i >= 0; i--)
        {
            PlayedNote note = playingNotes.get(i);
            if(playProg > note.startTick + note.duration + note.instrument.tuning.fadeout * 20D)
            {
                note.stop();
                playingNotes.remove(i);
            }
            else
            {
                note.tick(playProg);
            }
        }

        playProg++;
        return true;
    }

    public void stop()
    {
        for(PlayedNote note : playingNotes)
        {
            note.stop();
        }
        playingNotes.clear();

        playing = false;
    }

    public void playAtProgress(int i)
    {
        playProg = i;
    }
}
