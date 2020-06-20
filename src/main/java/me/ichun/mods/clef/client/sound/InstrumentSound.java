package me.ichun.mods.clef.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class InstrumentSound extends LocatableSound implements ITickableSound
{
    public float startVolume;
    public int duration;
    public int falloffTime;

    public int playTime;
    public boolean endTheNote;

    public InstrumentSound(SoundEvent soundIn, SoundCategory categoryIn, int duration, int falloffTime, float volume, float pitch, float x, float y, float z)
    {
        super(soundIn, categoryIn);
        this.volume = volume;
        this.pitch = pitch;
        this.repeat = true;
        this.repeatDelay = 0;
        this.x = x;
        this.y = y;
        this.z = z;

        this.startVolume = volume;
        this.duration = duration;
        this.falloffTime = falloffTime;
    }

    @Override
    public void tick()
    {
        playTime++;
        if(playTime > duration)
        {
            volume = MathHelper.clamp(startVolume * ((falloffTime - (playTime - duration)) / (float)falloffTime), 0F, 100F);
            if(playTime > (duration + falloffTime + 1))
            {
                endTheNote = true;
            }
        }
    }

    @Override
    public boolean isDonePlaying()
    {
        return endTheNote;
    }
}
