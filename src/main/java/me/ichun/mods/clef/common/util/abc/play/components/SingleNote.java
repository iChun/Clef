package me.ichun.mods.clef.common.util.abc.play.components;

import me.ichun.mods.clef.common.util.abc.construct.Accidental;
import me.ichun.mods.clef.common.util.abc.construct.Construct;
import me.ichun.mods.clef.common.util.abc.construct.Octave;
import me.ichun.mods.clef.common.util.abc.play.PlayedNote;
import me.ichun.mods.clef.common.util.abc.play.Trackk;

import java.util.ArrayList;

public class SingleNote extends Note
{
    @Override
    public boolean playNote(Trackk track, ArrayList<PlayedNote> playing, int currentProg)
    {
        return true;
    }

    @Override
    public void setup(TrackInfo info)
    {
        int key = 0; //middle
        boolean rest = false;
        for(Construct construct : constructs)
        {
            //TODO take note of rests and BIG rests (multiply the meter?)
            if(construct.getType() == Construct.EnumConstructType.ACCIDENTAL)
            {
                char c = ((Accidental)construct).type;
                switch(c)
                {
                    case '^':
                    {
                        key++;
                        break;
                    }
                    case '=': {break;} //do nothing
                    case '_':
                    {
                        key--;
                    }
                }
            }
            else if(construct.getType() == Construct.EnumConstructType.NOTE)
            {
                char c = ((me.ichun.mods.clef.common.util.abc.construct.Note)construct).type;
                if(c == 'Z' || c == 'X') //Multi Measure rests
                {
                    rest = true;
                    //TODO this
                }
                else if(c == 'z' || c == 'x')
                {
                    rest = true;
                }
                else
                {
                    key += Note.NOTE_TO_KEY_MAP.get(c);
                }
            }
            else if(construct.getType() == Construct.EnumConstructType.OCTAVE)
            {
                char c = ((Octave)construct).type;
                if(c == ',')
                {
                    key -= 12;
                }
                else
                {
                    key += 12;
                }
            }
        }

        if(!rest)
        {
            notePitch = (float)Math.pow(2.0D, (double)(key - 12) / 12.0D);
        }
    }
}
