package me.ichun.mods.clef.common.util.abc.construct.special;

import me.ichun.mods.clef.common.util.abc.construct.Construct;

public class Key extends Construct
{
    public String key;

    public Key()
    {
        key = "C";
    }

    public Key(String s)
    {
        key = s;
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.SPECIAL;
    }
}
