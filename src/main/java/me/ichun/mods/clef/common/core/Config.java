package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.ichunutil.common.core.config.ConfigBase;

import java.io.File;

public class Config extends ConfigBase
{
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
