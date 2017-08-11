package me.ichun.mods.clef.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class InstrumentSound extends PositionedSound implements ITickableSound
{
    public boolean donePlaying;

    public InstrumentSound(SoundEvent soundIn, SoundCategory categoryIn, float volume)
    {
        super(soundIn, categoryIn);
        this.volume = volume;
        this.pitch = 1F;
        this.repeat = true;
        this.repeatDelay = 0;
        this.xPosF = (float)0;
        this.yPosF = (float)0;
        this.zPosF = (float)0;
    }

    @Override
    public void update()
    {
    }

    @Override
    public boolean isDonePlaying()
    {
        return donePlaying;
    }
}
