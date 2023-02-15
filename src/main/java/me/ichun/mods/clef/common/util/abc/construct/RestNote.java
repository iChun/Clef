package me.ichun.mods.clef.common.util.abc.construct;

public class RestNote extends Construct
{
    public boolean multiMeasureRest;

    public RestNote(boolean multiMeasureRest)
    {
        this.multiMeasureRest = multiMeasureRest;
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.NOTE;
    }
}
