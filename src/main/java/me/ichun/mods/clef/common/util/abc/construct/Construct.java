package me.ichun.mods.clef.common.util.abc.construct;

public abstract class Construct
{
    //<grace notes>, <chord symbols>, <annotations>/<decorations>, <accidentals>, <note>, <octave>, <note length>, i.e. ~^c'3 or even "Gm7"v.=G,2.
    public enum EnumConstructType
    {
        ACCIDENTAL(3),
        NOTE(4),
        OCTAVE(5),
        SPECIAL(9),
        NONE(-1);

        private final int id;

        EnumConstructType(int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return id;
        }

        public static EnumConstructType getById(int i)
        {
            for(EnumConstructType effect : EnumConstructType.values())
            {
                if(effect.getId() == i)
                {
                    return effect;
                }
            }
            return NONE;
        }
    }
    public abstract EnumConstructType getType();
}
