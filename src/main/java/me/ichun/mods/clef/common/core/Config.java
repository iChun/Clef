package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.ichunutil.common.core.config.ConfigBase;
import me.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import me.ichun.mods.ichunutil.common.core.config.annotations.IntBool;
import me.ichun.mods.ichunutil.common.core.config.annotations.IntMinMax;
import me.ichun.mods.ichunutil.common.core.config.annotations.StringValues;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;

public class Config extends ConfigBase
{
    @ConfigProp
    @IntMinMax(min = 0, max = 3)
    public int creatableInstruments = 3;

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public String favoriteBand = "";

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntBool
    public int showFileTitle = 1;

    @ConfigProp(useSession = true)
    @IntBool
    public int allowOneHandedTwoHandedInstrumentUse = 0;

    @ConfigProp
    @IntBool
    public int zombiesCanUseInstruments = 1;

    @ConfigProp
    @StringValues
    public String[] disabledInstruments = new String[0];

    public Config(File file)
    {
        super(file);
    }

    @Override
    public String getModId()
    {
        return Clef.MOD_ID;
    }

    @Override
    public String getModName()
    {
        return Clef.MOD_NAME;
    }
}
