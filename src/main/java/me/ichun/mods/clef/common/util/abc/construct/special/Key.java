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
        if(key.contains("%"))
        {
            key = key.substring(0, key.indexOf("%"));
        }
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.SPECIAL;
    }
}
