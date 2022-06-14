package me.ichun.mods.clef.common.util.abc;

import me.ichun.mods.clef.common.util.abc.construct.special.Tempo;
import me.ichun.mods.clef.common.util.abc.play.components.Note;
import me.ichun.mods.clef.common.util.abc.play.components.Special;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class TrackBuilder
{
    //This specifies how many "sub ticks" during one tick should be considered.
    public static final int SUB_TICKS = 5;

    public static TrackInfo buildAbcTrack(Collection<Note> trackNotes, TrackInfo abc)
    {
        double[] info = new double[] {
                20D, //ticks per beat
                0.125D, //unit note length
                1D, //meter
                0D, //key.. unused.
                0.25D //tempo split
        };
        HashMap<Integer, Integer> keySignatures = new HashMap<>();
        HashMap<Integer, Integer> keyAccidentals = new HashMap<>();
        float currentParticialTick = 0F; //track particial ticks as well, to make timing more accurate
        int currentTick = 0;
        for(Note note : trackNotes)
        {
            //noinspection unchecked
            Set<Note>[] noteAtTime = abc.notes.computeIfAbsent(currentTick, v -> new Set[SUB_TICKS]);
            if(note.setup(info, keyAccidentals, keySignatures))//if true, not a special note, move to next spot.
            {
                insertNote(currentParticialTick, noteAtTime, note);
                abc.trackLength = currentTick + note.durationInTicks + (int) (note.durationInPartialTicks + 0.99F); // adds to the length of the note.
                currentTick += note.durationInTicks;
                currentParticialTick += note.durationInPartialTicks;
                //Make sure particialTick stays between 0..1
                int currentSubTicksAsInt = (int) currentParticialTick;
                currentParticialTick = currentParticialTick - currentSubTicksAsInt;
                currentTick += currentSubTicksAsInt;
            }
            note.optimizeMemoryUsage();
        }
        return abc;
    }

    public static TrackInfo buildMidiTrack(TreeMap<Long, Set<Note>> trackNotes, TrackInfo abc)
    {
        double[] info = new double[] {
                10D, //ticks per beat
                1D, //unit note length (here: 1 = 1/4 note)
                1D, //meter
                0D, //key.. unused.
                96 //tempo split
        };
        HashMap<Integer, Integer> keySignatures = new HashMap<>();
        HashMap<Integer, Integer> keyAccidentals = new HashMap<>();
        int longestTime = 0;
        long additionalTicksStartTimestamp = 0;
        float additionalTicks = 0;
        for (Map.Entry<Long, Set<Note>> entry : trackNotes.entrySet())
        {
            // timestamp in this context = midi time. Ticks = mc ticks
            long startTimestamp = entry.getKey();
            float startTime = TrackBuilder.convertTimeToTicks(startTimestamp - additionalTicksStartTimestamp, info) + additionalTicks;
            int startTimeInTicks = (int) startTime;
            float startTimeInPartialTicks = startTime - (int) startTime;

            //noinspection unchecked
            Set<Note>[] noteAtTime = abc.notes.computeIfAbsent(startTimeInTicks, v -> new Set[SUB_TICKS]);

            for (Note note : entry.getValue())
            {
                if (note instanceof Special && note.constructs.get(0) instanceof Tempo)
                {
                    // Tempo change - Sum the ticks up until now to keep consistent. Otherwise, we would miscalculate the start, as the time spend in the old tempo would also be multiplied by the new tempo
                    additionalTicks += TrackBuilder.convertTimeToTicks(startTimestamp - additionalTicksStartTimestamp, info);
                    additionalTicksStartTimestamp = startTimestamp;
                }
                if (note.setup(info, keyAccidentals, keySignatures))//if true, not a special note, move to next spot.
                {
                    insertNote(startTimeInPartialTicks, noteAtTime, note);
                }
                note.optimizeMemoryUsage();
                int totalTimeUpToHere = (int) Math.ceil(note.durationInTicks + note.durationInPartialTicks + startTime);
                if (totalTimeUpToHere > longestTime) longestTime = totalTimeUpToHere;
            }
        }
        abc.trackLength = longestTime;
        return abc;
    }

    private static void insertNote(float durationInPartialTicks, Set<Note>[] noteAtTime, Note note)
    {
        int subIndex = Math.min(SUB_TICKS, (int) (durationInPartialTicks * SUB_TICKS));
        // Try to use a singleton instead of a hash set, as most of the time there is one one note per tick/subtick
        // singleton set has a much better memory footprint, this alone saves ~ 10% of memory (after full gc)
        // When having around 1200 abc files
        if (noteAtTime[subIndex] == null)
        {
            noteAtTime[subIndex] = Collections.singleton(note);
        }
        else if (noteAtTime[subIndex] instanceof HashSet)
        {
            noteAtTime[subIndex].add(note); //only add the actual notes. No specials.
        }
        else
        {
            noteAtTime[subIndex] = new HashSet<>(noteAtTime[subIndex]);
            noteAtTime[subIndex].add(note);
        }
    }

    public static float convertTimeToTicks(double duration, double[] info)
    {
        return (float) (info[0] * (info[1] / info[4]) * duration); //tempo * duration * (unit note length / tempo splits)
    }
}
