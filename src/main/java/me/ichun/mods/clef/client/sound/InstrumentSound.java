package me.ichun.mods.clef.client.sound;

import me.ichun.mods.clef.client.core.SoundSystemReflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.lang.reflect.InvocationTargetException;

public class InstrumentSound extends PositionedSound implements ITickableSound
{
    public String id;
    public float startVolume;
    public int duration;
    public int falloffTime;

    public int playTime;
    public boolean donePlaying;
    public Object noteLocationObject;

    public InstrumentSound(String id, SoundEvent soundIn, SoundCategory categoryIn, int duration, int falloffTime, float volume, float pitch, Object noteLocationObject)
    {
        super(soundIn, categoryIn);
        this.id = id;
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
        this.noteLocationObject = noteLocationObject;
        updateNoteLocation();
    }

    @Override
    public void update()
    {
        updateNoteLocation();
        playTime++;
        if(playTime > duration)
        {
            volume = MathHelper.clamp_float(startVolume * ((falloffTime - (playTime - duration)) / (float)falloffTime), 0F, 100F);
            if(playTime > (duration + falloffTime + 200))
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

    public void updateNoteLocation()
    {
        if(noteLocationObject instanceof EntityLivingBase)
        {
            EntityLivingBase living = (EntityLivingBase)noteLocationObject;
            this.xPosF = (float)living.posX;
            this.yPosF = (float)living.posY + living.getEyeHeight();
            this.zPosF = (float)living.posZ;
        }
    }
}
