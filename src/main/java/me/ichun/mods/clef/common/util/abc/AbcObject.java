package me.ichun.mods.clef.common.util.abc;

import me.ichun.mods.clef.common.util.abc.construct.Construct;

import java.util.ArrayList;

public class AbcObject
{
    public int referenceNumber = -1; //X
    public String title = ""; //T
    public String composer = ""; //C
    public String transcriber = ""; //Z

    public ArrayList<Construct> constructs = new ArrayList<>();
}
