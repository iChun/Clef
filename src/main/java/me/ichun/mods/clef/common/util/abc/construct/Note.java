package me.ichun.mods.clef.common.util.abc.construct;

public class Note extends Construct
{
    public int key;

    private Note(int key)
    {
        this.key = key;
    }

    public static Note createFromRawKey(int key)
    {
        return new Note(key);
    }

    public static Construct createFromABC(char c)
    {
        if(c == 'Z' || c == 'X') //Multi Measure rests
        {
            return new RestNote(true);
        }
        else if(c == 'z' || c == 'x')
        {
            return new RestNote(false);
        }
        else
        {
            return new Note(me.ichun.mods.clef.common.util.abc.play.components.Note.NOTE_TO_KEY_MAP.get(c));
        }
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.NOTE;
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof Note && ((Note)o).key == key;
    }
}
