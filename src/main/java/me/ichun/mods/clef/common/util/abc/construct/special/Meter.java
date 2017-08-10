package me.ichun.mods.clef.common.util.abc.construct.special;

import me.ichun.mods.clef.common.util.abc.construct.Construct;

public class Meter extends Construct //apparently not import for PC reading
{
    public String meter; //default 4/4

    public Meter()
    {
        meter = "C";
    }

    public Meter(String s)
    {
        meter = s;
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.SPECIAL;
    }
}
