package me.ichun.mods.clef.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.LocatableSound;
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
    public Object noteLocationObject;

    public InstrumentSound(SoundEvent soundIn, SoundCategory categoryIn, int duration, int falloffTime, float volume, float pitch, Object noteLocationObject)
    {
        super(soundIn, categoryIn);
        this.volume = volume;
        this.pitch = pitch;
        this.repeat = true;
        this.repeatDelay = 0;
        this.x = (float)0;
        this.y = (float)0;
        this.z = (float)0;

        this.startVolume = volume;
        this.duration = duration;
        this.falloffTime = falloffTime;
        this.noteLocationObject = noteLocationObject;
        updateNoteLocation();
    }

    @Override
    public void tick()
    {
        updateNoteLocation();
        playTime++;
        if(playTime > duration)
        {
            volume = MathHelper.clamp(startVolume * ((falloffTime - (playTime - duration)) / (float)falloffTime), 0F, 100F);
            if(playTime > (duration + falloffTime + 600))
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

    public void updateNoteLocation()
    {
        if(noteLocationObject instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity)noteLocationObject;
            Vec3d view = living.getLookVec();
            Vec3d motion = living.getMotion();
            this.x = (float)(living.getPosX() + view.x * 0.3D) + (float)motion.x;
            this.y = (float)(living.getPosY() + living.getEyeHeight() + view.y * 0.3D) + (float)motion.y;
            this.z = (float)(living.getPosZ() + view.z * 0.3D) + (float)motion.z;
        }
        else if(noteLocationObject instanceof BlockPos)
        {
            BlockPos pos = (BlockPos)noteLocationObject;
            this.x = pos.getX() + 0.5F;
            this.y = pos.getY() + 0.5F;
            this.z = pos.getZ() + 0.5F;
        }
    }
}
