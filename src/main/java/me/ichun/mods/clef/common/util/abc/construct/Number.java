package me.ichun.mods.clef.common.util.abc.construct;

public class Number extends Construct
{
    public int number = 0;

    public Number(int c)
    {
        number = c;
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.NUMBER;
    }
}
