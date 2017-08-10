package me.ichun.mods.clef.common.util.abc.play.components;

import me.ichun.mods.clef.common.util.abc.construct.Construct;
import me.ichun.mods.clef.common.util.abc.construct.special.Key;
import me.ichun.mods.clef.common.util.abc.construct.special.Meter;
import me.ichun.mods.clef.common.util.abc.construct.special.Tempo;
import me.ichun.mods.clef.common.util.abc.construct.special.UnitNoteLength;
import me.ichun.mods.clef.common.util.abc.play.PlayedNote;
import me.ichun.mods.clef.common.util.abc.play.Trackk;

import java.util.ArrayList;

public class Special extends Note
{
    public Special(Construct c)
    {
        constructs.add(c);
    }

    @Override
    public boolean playNote(Trackk track, ArrayList<PlayedNote> playedNotes, int currentProg)
    {
        Construct construct = constructs.get(0); //This shouldn't be empty, ever.
        if(construct instanceof Meter)
        {
            //TODO this
        }
        else if(construct instanceof Key)
        {
            //TODO this, too.
        }
        else if(construct instanceof Tempo)
        {
            Tempo tempo = (Tempo)construct;
            track.ticksPerBeat = 1200 / tempo.bpm;
            //TODO tempo splits...??????
        }
        else if(construct instanceof UnitNoteLength)
        {
            UnitNoteLength length = (UnitNoteLength)construct;
            track.unitNoteLength = length.length;
        }
        return false;
    }

    @Override
    public void setup(TrackInfo info){} //No setup required.
}
