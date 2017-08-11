package me.ichun.mods.clef.common.util.abc.play.components;

import me.ichun.mods.clef.common.util.abc.construct.Construct;
import me.ichun.mods.clef.common.util.abc.play.PlayedNote;
import me.ichun.mods.clef.common.util.abc.play.Track;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Note
{
    public static final double NOTE_REST = -100000D;
    public static final HashMap<Character, Integer> NOTE_TO_KEY_MAP = new HashMap<Character, Integer>(){{
        put('C', 0);
        put('D', 2);
        put('E', 4);
        put('F', 5);
        put('G', 7);
        put('A', 9);
        put('B', 11);
        put('c', 12);
        put('d', 14);
        put('e', 16);
        put('f', 17);
        put('g', 19);
        put('a', 21);
        put('b', 23);
    }};

    public double notePitch = NOTE_REST; //key - abc....EFG
    public double duration = 1;
    public int durationInTicks = 5;

    public ArrayList<Construct> constructs = new ArrayList<>();

    public abstract boolean playNote(Track track, ArrayList<PlayedNote> playedNotes, int currentProg); //returns false if it's a special "note"

    public abstract boolean setup(double[] info, HashMap<Integer, Integer> keyAccidentals); //returns false if it's a special "note"

}
