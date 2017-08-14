package me.ichun.mods.clef.common.util.abc.play.components;

import me.ichun.mods.clef.common.util.abc.construct.Accidental;
import me.ichun.mods.clef.common.util.abc.construct.Construct;
import me.ichun.mods.clef.common.util.abc.construct.Octave;
import me.ichun.mods.clef.common.util.abc.play.PlayedNote;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;

import java.util.ArrayList;
import java.util.HashMap;

public class SingleNote extends Note
{
    @Override
    public int playNote(Track track, int currentProg, Instrument instrument, Object noteLocation)
    {
        if(key != Note.NOTE_REST)
        {
            new PlayedNote(instrument, currentProg, durationInTicks, key, noteLocation instanceof EntityPlayer ? SoundCategory.PLAYERS : noteLocation instanceof EntityLivingBase ? SoundCategory.HOSTILE : SoundCategory.BLOCKS, noteLocation).start();
        }
        return durationInTicks;
    }

    @Override
    public boolean setup(double[] info, HashMap<Integer, Integer> keyAccidentals)
    {
        int accidental = -2;
        int key = 0; //middle
        boolean rest = false;
        boolean hasNote = false;
        for(Construct construct : constructs)
        {
            if(construct.getType() == Construct.EnumConstructType.ACCIDENTAL)
            {
                char c = ((Accidental)construct).type;
                switch(c)
                {
                    case '^':
                    {
                        accidental = 1;
                        break;
                    }
                    case '=':
                    {
                        accidental = 0;
                        break;
                    } //do nothing
                    case '_':
                    {
                        accidental = -1;
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
                hasNote = true;
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

        this.durationInTicks = (int)Math.round(info[0] * (duration));
        if(hasNote)
        {
            if(!rest)
            {
                if(keyAccidentals.containsKey(key) && accidental == -2)
                {
                    accidental = keyAccidentals.get(key);
                }
                else if(accidental != -2)
                {
                    keyAccidentals.put(key, accidental);
                }
                if(accidental == -2)
                {
                    accidental = 0;
                }
                this.key = (key + accidental) + 54; //MiddleC?
                //notePitch = (float)Math.pow(2.0D, (double)((key + accidental) - 12) / 12.0D);
            }
        }
        else
        {
            this.durationInTicks = 0;
        }
        return true;
    }
}
