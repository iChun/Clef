package me.ichun.mods.clef.common.util.abc.play;


import me.ichun.mods.clef.common.util.abc.AbcParser;
import me.ichun.mods.clef.common.util.abc.play.components.Note;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks which notes need to be played this tick for a specific track
 */
@OnlyIn(Dist.CLIENT)
public class TrackTracker
{
    private final Track track;
    private final Set<NotesTickInfo> masterSet = new HashSet<>();
    private int playProg;
    /**
     * Used to check if this tracker is still active or can be removed
     */
    private boolean shutdown = false;

    public TrackTracker(Track track)
    {
        this.track = track;
    }

    public void startNewTick(int playProg)
    {
        shutdown = false;
        masterSet.clear();
        this.playProg = playProg;
        NotePlayThread.INSTANCE.ensurePresent(this);
    }

    public void addTickInfo(NotesTickInfo noteInfo)
    {
        if (noteInfo.notes.length != AbcParser.SUB_TICKS)
            throw new IllegalArgumentException("Invalid sized array!" + noteInfo.notes.length);
        masterSet.add(noteInfo);
    }

    /**
     * Runs all the sounds that are scheduled from this track for this subtick
     * @param runTick The subtick to play
     */
    public void runSubTick(int runTick)
    {
        for (NotesTickInfo noteInfo : masterSet)
        {
            Set<Note> forCurrentSubTick = noteInfo.notes[runTick];
            if (forCurrentSubTick != null)
            {
                for (Note toPlay : forCurrentSubTick)
                {
                    int time = toPlay.playNote(track, playProg, noteInfo.instrument, noteInfo.notePos);
                    if (noteInfo.checkRest && time > track.timeToSilence && toPlay.key != Note.NOTE_REST)
                    {
                        track.timeToSilence = time + 1;
                    }
                }
            }
        }
    }

    public boolean didNotStart()
    {
        return shutdown;
    }

    public void reset()
    {
        shutdown = true;
    }
}
