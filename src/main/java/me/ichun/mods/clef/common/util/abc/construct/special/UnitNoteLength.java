package me.ichun.mods.clef.common.util.abc.construct.special;

import me.ichun.mods.clef.common.util.abc.construct.Construct;

public class UnitNoteLength extends Construct //apparently not import for PC reading
{
    public double length = 0.125D; //default 1/8D

    public UnitNoteLength(){} //L

    public UnitNoteLength(double d)
    {
        length = d;
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.SPECIAL;
    }
}
