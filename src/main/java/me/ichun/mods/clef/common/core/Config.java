package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.ichunutil.common.core.config.ConfigBase;
import me.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import me.ichun.mods.ichunutil.common.core.config.annotations.IntMinMax;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;

public class Config extends ConfigBase
{
    @ConfigProp(useSession = true)
    @IntMinMax(min = 0, max = 3)
    public int creatableInstruments = 3;

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public String favoriteBand = "";

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
