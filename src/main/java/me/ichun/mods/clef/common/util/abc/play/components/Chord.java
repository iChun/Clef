package me.ichun.mods.clef.common.util.abc.play.components;

import me.ichun.mods.clef.common.util.abc.construct.Construct;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.Instrument;

import java.util.ArrayList;
import java.util.HashMap;

public class Chord extends Note
{
    public ArrayList<Note> notes = new ArrayList<>();

    @Override
    public int playNote(Track track, int currentProg, Instrument instrument, Object noteLocation)
    {
        int longest = 0;
        for(Note note : notes)
        {
            int dur = note.playNote(track, currentProg, instrument, noteLocation);
            if(dur > longest)
            {
                longest = dur;
            }
        }
        return longest;
    }

    @Override
    public boolean setup(double[] info, HashMap<Integer, Integer> keyAccidentals, HashMap<Integer, Integer> keySignature)
    {
        for(Note note : notes)
        {
            note.duration *= duration;
            note.setup(info, keyAccidentals, keySignature);
            if(note.key != NOTE_REST)
            {
                key = note.key;
            }
        }
        double tempDur = 1000D;
        for(Note note : notes) //a rest in a chord denotes the duration of the chord.
        {
            if(note instanceof SingleNote)
            {
                SingleNote note1 = (SingleNote)note;
                for(Construct construct : note1.constructs)
                {
                    if(construct.getType() == Construct.EnumConstructType.NOTE)
                    {
                        if(note1.duration < tempDur)
                        {
                            tempDur = note1.duration;
                        }
                    }
                }
            }
        }
        float scaledDuration = (float) (info[0] * (info[1] / info[4]) * tempDur); //tempo * duration * (unit note length / tempo splits)
        this.durationInTicks = (int) scaledDuration;
        this.durationInPartialTicks = scaledDuration - (int) scaledDuration;
        return true;
    }
}
