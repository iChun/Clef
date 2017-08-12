package me.ichun.mods.clef.common.util.abc.play.components;

import me.ichun.mods.clef.common.util.abc.construct.Construct;
import me.ichun.mods.clef.common.util.abc.play.PlayedNote;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.Instrument;

import java.util.ArrayList;
import java.util.HashMap;

public class Chord extends Note
{
    public ArrayList<Note> notes = new ArrayList<>();

    @Override
    public boolean playNote(Track track, ArrayList<PlayedNote> playedNotes, int currentProg, Instrument instrument)
    {
        for(Note note : notes)
        {
            note.playNote(track, playedNotes, currentProg, instrument);
        }
        return true;
    }

    @Override
    public boolean setup(double[] info, HashMap<Integer, Integer> keyAccidentals)
    {
        for(Note note : notes)
        {
            note.duration *= duration;
            note.setup(info, keyAccidentals);
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
//                        char c = ((me.ichun.mods.clef.common.util.abc.construct.Note)construct).type;
//                        if(c == 'z' || c == 'x')
//                        {
//                            this.duration = note1.duration;
//                            break;
//                        }
                    }
                }
            }
        }
        this.durationInTicks = (int)Math.round(info[0] * (tempDur));
        return true;
    }
}
