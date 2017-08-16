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
    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public String favoriteBand = "";

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntBool
    public int showFileTitle = 1;

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntMinMax(min = 0, max = 100)
    public int instrumentVolume = 100;

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntBool
    public int showRecordPlayingMessageForTracks = 0;

    @ConfigProp(useSession = true)
    @IntBool
    public int allowOneHandedTwoHandedInstrumentUse = 1;

    @ConfigProp
    @IntBool
    public int zombiesCanUseInstruments = 1;

    @ConfigProp
    @IntMinMax(min = 0, max = 10000)
    public int zombieSpawnRate = 50;

    @ConfigProp
    @IntMinMax(min = 0, max = 10000)
    public int mobDropRate = 50;

    @ConfigProp
    @IntBool
    public int onlyHostileMobSpawn = 1;

    @ConfigProp
    @IntMinMax(min = 0, max = 3)
    public int creatableInstruments = 3;

    @ConfigProp
    @StringValues
    public String[] disabledInstruments = new String[0];

    @ConfigProp
    @IntMinMax(min = 0, max = 20)
    public int lootSpawnRate = 1;

    @ConfigProp
    @StringValues
    public String[] disabledLootChests = new String[0];

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
