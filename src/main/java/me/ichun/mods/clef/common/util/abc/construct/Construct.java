package me.ichun.mods.clef.common.util.abc.construct;

public abstract class Construct
{
    //<grace notes>, <chord symbols>, <annotations>/<decorations>, <accidentals>, <note>, <octave>, <note length>, i.e. ~^c'3 or even "Gm7"v.=G,2.
    public enum EnumConstructType
    {
        GRACE(0),
        CHORD(1),
        ANNOTATION(2),
        ACCIDENTAL(3),
        NOTE(4),
        OCTAVE(5),
        NUMBER(6),
        OPERATOR(7),
        SPACE(8),
        SPECIAL(9),
        BAR_LINE(10),
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
    public enum EnumConstructOrder
    {
        GRACE_OPEN(0),
        CHORD_START(1),
        ANNOTATION(2), //ignored
        ACCIDENTAL(3),
        NOTE(4),
        OCTAVE(5),
        NUMBER_NUMERATOR(6),
        OPERATOR(7),
        NUMBER_DENOMINATOR(6),
        SPACE(8),
        CHORD_END(1),
        CHORD_NUMBER_NUMERATOR(6),
        CHORD_OPERATOR(7),
        CHORD_NUMBER_DENOMINATOR(6),
        GRACE_CLOSE(0),
        BAR_LINE(10),
        NONE(-1);

        public static final int MAX_ID = 15;
        public static final EnumConstructOrder[] ORDER = new EnumConstructOrder[] { GRACE_OPEN, CHORD_START, ANNOTATION, ACCIDENTAL, NOTE, OCTAVE,
        NUMBER_NUMERATOR, OPERATOR, NUMBER_DENOMINATOR, SPACE, CHORD_END, CHORD_NUMBER_NUMERATOR, CHORD_OPERATOR, CHORD_NUMBER_DENOMINATOR, GRACE_CLOSE, BAR_LINE };

        private final int type;

        EnumConstructOrder(int type)
        {
            this.type = type;
        }

        public int getType()
        {
            return type;
        }
    }
    public abstract EnumConstructType getType();
}
