package me.ichun.mods.clef.common.util.abc.construct;

public class Chord extends Construct
{
    public char type = ',';

    public Chord(char c)
    {
        type = c;
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.CHORD;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Chord)
        {
            return ((Chord)o).type == type;
        }
        return false;
    }

}
