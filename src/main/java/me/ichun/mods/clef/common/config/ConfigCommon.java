package me.ichun.mods.clef.common.config;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ConfigCommon extends ConfigBase
{
    @CategoryDivider
    @Prop
    public boolean zombiesCanUseInstruments = true;

    @Prop(min = 0, max = 10000)
    public int zombieSpawnRate = 100;

    @Prop(min = 0, max = 10000)
    public int mobDropRate = 100;

    @Prop
    public boolean onlyHostileMobSpawn = true;

    @Prop(min = 0, max = 3)
    public int creatableInstruments = 3;

    @Prop
    public List<String> disabledInstruments = new ArrayList<>();

    @Prop(min = 0, max = 20)
    public int lootSpawnRate = 1;

    @Prop
    public List<String> disabledLootChests = new ArrayList<>();

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
