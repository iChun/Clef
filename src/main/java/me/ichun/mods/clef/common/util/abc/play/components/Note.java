package me.ichun.mods.clef.common.util.abc.play.components;

import me.ichun.mods.clef.common.util.abc.construct.Construct;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.Instrument;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Note
{
    public static final int NOTE_REST = -1;
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

    public int key = NOTE_REST; //key - abc....EFG
    public double duration = 1;
    public int durationInTicks = 5;
    public float durationInPartialTicks = 0F;

    public ArrayList<Construct> constructs = new ArrayList<>();

    /**
     * Plays this note.
     * This may be called on the client or Clef note player thread, so be thread-safe
     * @return false if it's a special "note"
     */
    public abstract int playNote(Track track, int currentProg, Instrument instrument, Object noteLocation);

    public abstract boolean setup(double[] info, HashMap<Integer, Integer> keyAccidentals, HashMap<Integer, Integer> keySignature); //returns false if it's a special "note"

}
