package me.ichun.mods.clef.common.util.abc.play.components;

import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.Instrument;

import java.util.HashMap;

public class BarLine extends Note
{
    @Override
    public int playNote(Track track, int currentProg, Instrument instrument, Object noteLocation)
    {
        return 0;
    }

    @Override
    public boolean setup(double[] info, HashMap<Integer, Integer> keyAccidentals, HashMap<Integer, Integer> keySignature)
    {
        keyAccidentals.clear();
        keyAccidentals.putAll(keySignature);
        return false;
    }
}
