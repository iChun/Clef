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
}
