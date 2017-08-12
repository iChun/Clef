package me.ichun.mods.clef.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;

public class InstrumentSound extends PositionedSound implements ITickableSound
{
    public float startVolume;
    public int duration;
    public int falloffTime;

    public int playTime;
    public boolean donePlaying;

    public InstrumentSound(SoundEvent soundIn, SoundCategory categoryIn, int duration, int falloffTime, float volume, float pitch)
    {
        super(soundIn, categoryIn);
        this.volume = volume;
        this.pitch = pitch;
        this.repeat = true;
        this.repeatDelay = 0;
        this.xPosF = (float)0;
        this.yPosF = (float)0;
        this.zPosF = (float)0;

        this.startVolume = volume;
        this.duration = duration;
        this.falloffTime = falloffTime;
    }

    @Override
    public void update()
    {
        playTime++;
        if(playTime > duration)
        {
            volume = MathHelper.clamp_float(startVolume * (float)((falloffTime - (playTime - duration)) / (double)falloffTime), 0F, 100F);
            if(playTime > duration + falloffTime + 5)
            {
                donePlaying = true;
            }
        }
    }

    @Override
    public boolean isDonePlaying()
    {
        return donePlaying;
    }
}
