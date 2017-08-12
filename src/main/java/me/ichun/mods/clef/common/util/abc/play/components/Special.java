package me.ichun.mods.clef.common.util.abc.play.components;

import me.ichun.mods.clef.common.util.abc.construct.Construct;
import me.ichun.mods.clef.common.util.abc.construct.special.Key;
import me.ichun.mods.clef.common.util.abc.construct.special.Meter;
import me.ichun.mods.clef.common.util.abc.construct.special.Tempo;
import me.ichun.mods.clef.common.util.abc.construct.special.UnitNoteLength;
import me.ichun.mods.clef.common.util.abc.play.PlayedNote;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.Instrument;

import java.util.ArrayList;
import java.util.HashMap;

public class Special extends Note
{
    public Special(Construct c)
    {
        constructs.add(c);
    }

    @Override
    public boolean playNote(Track track, int currentProg, Instrument instrument)
    {
        return false;
    }

    @Override
    public boolean setup(double[] info, HashMap<Integer, Integer> keyAccidentals)
    {
        Construct construct = constructs.get(0); //This shouldn't be empty, ever.
        if(construct instanceof Meter)
        {
            info[2] = ((Meter)construct).meter;
        }
        else if(construct instanceof Key)
        {
            //TODO this, too.
        }
        else if(construct instanceof Tempo)
        {
            Tempo tempo = (Tempo)construct;

            //1200 / bpm = ticks between beats.
            info[0] = 1200 / (double)tempo.bpm;
            info[4] = tempo.splits;
            //TODO tempo splits...??????
        }
        else if(construct instanceof UnitNoteLength)
        {
            UnitNoteLength length = (UnitNoteLength)construct;
            info[1] = length.length;
        }
        return false;
    }
}
