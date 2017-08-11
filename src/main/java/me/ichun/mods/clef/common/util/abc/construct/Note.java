package me.ichun.mods.clef.common.util.abc.construct;

public class Note extends Construct
{
    public char type = 'C';

    public Note(char c)
    {
        type = c;
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.NOTE;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Note)
        {
            return ((Note)o).type == type;
        }
        return false;
    }
}
