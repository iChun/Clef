package me.ichun.mods.clef.common.util.abc.play;

import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;

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

    public ArrayList<PlayedNote> playingNotes = new ArrayList<>();

    public Track(TrackInfo track)
    {
        this.track = track;
    }

    public boolean update() //returns false if it's time to stop playing.
    {
        if(!playing || playProg > track.trackLength)
        {
            return false;
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
