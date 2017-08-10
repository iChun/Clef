package me.ichun.mods.clef.common.util.abc.play.components;

import me.ichun.mods.clef.common.util.abc.construct.Construct;
import me.ichun.mods.clef.common.util.abc.play.PlayedNote;
import me.ichun.mods.clef.common.util.abc.play.Track;

import java.util.ArrayList;

public abstract class Note
{
    public double note = -10000D; //key - abc....EFG
    public int octave = 0;
    public double duration = 1;

    public ArrayList<Construct> constructs = new ArrayList<>();

    public abstract boolean playNote(Track track, ArrayList<PlayedNote> playedNotes, int currentProg); //returns false if it's a special "note"

    public abstract void setup();

}
