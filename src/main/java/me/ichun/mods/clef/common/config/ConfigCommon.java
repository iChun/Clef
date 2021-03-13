package me.ichun.mods.clef.common.config;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import net.minecraftforge.fml.ModLoadingContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ConfigCommon extends ConfigBase
{
    @CategoryDivider
    public boolean zombiesCanUseInstruments = true;

    @Prop(min = 0, max = 10000)
    public int zombieSpawnRate = 50;

    @Prop(min = 0, max = 10000)
    public int mobDropRate = 50;

    public boolean onlyHostileMobSpawn = true;

    @Prop(min = 0, max = 3)
    public int creatableInstruments = 3;

    public List<String> disabledInstruments = new ArrayList<>();

    @Prop(min = 0, max = 20)
    public int lootSpawnRate = 1;

    public List<String> disabledLootChests = new ArrayList<>();

    public boolean showFileTitle = true;

    public ConfigCommon()
    {
        super(ModLoadingContext.get().getActiveContainer().getModId() + "-common.toml");
    }

    @Nonnull
    @Override
    public String getModId()
    {
        return Clef.MOD_ID;
    }

    @Nonnull
    @Override
    public String getConfigName()
    {
        return Clef.MOD_NAME;
    }

}
