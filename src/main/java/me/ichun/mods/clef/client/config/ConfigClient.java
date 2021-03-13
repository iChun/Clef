package me.ichun.mods.clef.client.config;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;

public class ConfigClient extends ConfigBase
{
    @CategoryDivider(name = "clientOnly")
    public String favoriteBand = "";

    @Prop(min = 0, max = 100)
    public int instrumentVolume = 100;

    @Prop(min = 16, max = Track.MAX_TRACKING_RANGE)
    public int instrumentHearableDistance = 32;

    public boolean showRecordPlayingMessageForTracks = false;

    public ConfigClient()
    {
        super(ModLoadingContext.get().getActiveContainer().getModId() + "-client.toml");
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

    @Nonnull
    @Override
    public ModConfig.Type getConfigType()
    {
        return ModConfig.Type.CLIENT;
    }
}
