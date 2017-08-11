package me.ichun.mods.clef.common.util.abc.construct.special;

import me.ichun.mods.clef.common.util.abc.construct.Construct;

public class Meter extends Construct //apparently not import for PC reading
{
    public double meter = 1D; //default 4/4

    public Meter(){}

    public Meter(double d)
    {
        meter = d;
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.SPECIAL;
    }
}
