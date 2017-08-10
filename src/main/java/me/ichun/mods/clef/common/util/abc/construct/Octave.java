package me.ichun.mods.clef.common.util.abc.construct;

public class Octave extends Construct
{
    public char type = ',';

    public Octave(char c)
    {
        type = c;
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.OCTAVE;
    }
}
