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

import java.util.HashMap;

public class SingleNote extends Note
{
    @Override
    public int playNote(Track track, int currentProg, Instrument instrument, Object noteLocation)
    {
        if(key != Note.NOTE_REST && instrument.hasAvailableKey(key))
        {
            new PlayedNote(instrument, currentProg, durationInTicks, key, noteLocation instanceof EntityPlayer ? SoundCategory.PLAYERS : noteLocation instanceof EntityLivingBase ? SoundCategory.HOSTILE : SoundCategory.BLOCKS, noteLocation).start();
        }
        if(key == 37)
        {
            System.out.println(key);
        }
        return durationInTicks;
    }

    @Override
    public boolean setup(double[] info, HashMap<Integer, Integer> keyAccidentals, HashMap<Integer, Integer> keySignature)
    {
        int accidental = -20;
        boolean applyAccidental = false;
        int currentAccidental = 0;
        int key = 0; //middle
        boolean rest = false;
        boolean hasNote = false;
        for(Construct construct : constructs)
        {
            if(construct.getType() == Construct.EnumConstructType.ACCIDENTAL)
            {
                applyAccidental = true;
                char c = ((Accidental)construct).type;
                switch(c)
                {
                    case '^':
                    {
                        currentAccidental ++;
                        break;
                    }
                    case '=':
                    {
                        currentAccidental = 0;
                        break;
                    }
                    case '_':
                    {
                        currentAccidental --;
                        break;
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

        this.durationInTicks = (int)Math.round(info[0] * duration * info[1] / info[4]); //tempo * duration * unit note length / tempo splits
        if(hasNote)
        {
            if(!rest)
            {
                if(keySignature.containsKey(key % 12))
                {
                    accidental = keySignature.get(key % 12);
                }
                if(keyAccidentals.containsKey(key))
                {
                    accidental = keyAccidentals.get(key);
                }
                if(applyAccidental)
                {
                    accidental = currentAccidental;
                    keyAccidentals.put(key, accidental);
                }
                if(accidental == -20)
                {
                    accidental = 0;
                }
                this.key = (key + accidental) + (12 * 5); //MiddleC?
            }
        }
        else
        {
            this.durationInTicks = 0;
        }
        return true;
    }
}
