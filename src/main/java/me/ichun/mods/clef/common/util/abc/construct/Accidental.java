package me.ichun.mods.clef.common.util.abc.construct;

public class Accidental extends Construct
{
    public char type = '=';

    public Accidental(char c)
    {
        type = c;
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.ACCIDENTAL;
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof Accidental && ((Accidental)o).type == type;
    }
}
