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
    public int playNote(Track track, int currentProg, Instrument instrument, Object noteLocation)
    {
        return 0;
    }

    @Override
    public boolean setup(double[] info, HashMap<Integer, Integer> keyAccidentals, HashMap<Integer, Integer> keySignature)
    {
        Construct construct = constructs.get(0); //This shouldn't be empty, ever.
        if(construct instanceof Meter)
        {
            info[2] = ((Meter)construct).meter;
        }
        else if(construct instanceof Key)
        {
            keySignature.clear();
            switch(((Key)construct).key)
            {
                case "C#":
                    keySignature.put(NOTE_TO_KEY_MAP.get('B') % 12, 1);
                case "F#":
                    keySignature.put(NOTE_TO_KEY_MAP.get('E') % 12, 1);
                case "B":
                    keySignature.put(NOTE_TO_KEY_MAP.get('A') % 12, 1);
                case "E":
                    keySignature.put(NOTE_TO_KEY_MAP.get('D') % 12, 1);
                case "A":
                    keySignature.put(NOTE_TO_KEY_MAP.get('G') % 12, 1);
                case "D":
                    keySignature.put(NOTE_TO_KEY_MAP.get('C') % 12, 1);
                case "G":
                    keySignature.put(NOTE_TO_KEY_MAP.get('F') % 12, 1);
                    break;
                case "Cb":
                    keySignature.put(NOTE_TO_KEY_MAP.get('F') % 12, -1);
                case "Gb":
                    keySignature.put(NOTE_TO_KEY_MAP.get('C') % 12, -1);
                case "Db":
                    keySignature.put(NOTE_TO_KEY_MAP.get('G') % 12, -1);
                case "Ab":
                    keySignature.put(NOTE_TO_KEY_MAP.get('D') % 12, -1);
                case "Eb":
                    keySignature.put(NOTE_TO_KEY_MAP.get('A') % 12, -1);
                case "Bb":
                    keySignature.put(NOTE_TO_KEY_MAP.get('E') % 12, -1);
                case "F":
                    keySignature.put(NOTE_TO_KEY_MAP.get('B') % 12, -1);
                    break;
                case "C":
                default:
                    break;
            }
        }
        else if(construct instanceof Tempo)
        {
            Tempo tempo = (Tempo)construct;

            //1200 / bpm = ticks between beats.
            info[0] = 1200 / (double)tempo.bpm;
            info[4] = tempo.splits;
        }
        else if(construct instanceof UnitNoteLength)
        {
            UnitNoteLength length = (UnitNoteLength)construct;
            info[1] = length.length;
        }
        return false;
    }
}
