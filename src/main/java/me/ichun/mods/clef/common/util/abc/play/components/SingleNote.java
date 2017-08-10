package me.ichun.mods.clef.common.util.abc.play.components;

import me.ichun.mods.clef.common.util.abc.play.PlayedNote;
import me.ichun.mods.clef.common.util.abc.play.Track;

import java.util.ArrayList;

public class SingleNote extends Note
{
    @Override
    public boolean playNote(Track track, ArrayList<PlayedNote> playing, int currentProg)
    {
        return true;
    }

    @Override
    public void setup()
    {
        //TODO take note of ress
    }
}
