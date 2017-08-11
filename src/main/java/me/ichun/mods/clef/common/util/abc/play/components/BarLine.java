package me.ichun.mods.clef.common.util.abc.play.components;

import me.ichun.mods.clef.common.util.abc.play.PlayedNote;
import me.ichun.mods.clef.common.util.abc.play.Track;

import java.util.ArrayList;
import java.util.HashMap;

public class BarLine extends Note
{
    @Override
    public boolean playNote(Track track, ArrayList<PlayedNote> playedNotes, int currentProg)
    {
        return false;
    }

    @Override
    public boolean setup(double[] info, HashMap<Integer, Integer> keyAccidentals)
    {
        keyAccidentals.clear();
        return false;
    }
}
