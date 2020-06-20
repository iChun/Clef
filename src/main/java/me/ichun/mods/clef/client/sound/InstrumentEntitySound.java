package me.ichun.mods.clef.client.sound;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

public class InstrumentEntitySound extends InstrumentSound
{
    private final LivingEntity living;

    public InstrumentEntitySound(SoundEvent soundIn, SoundCategory categoryIn, int duration, int falloffTime, float volume, float pitch, LivingEntity living)
    {
        super(soundIn, categoryIn, duration, falloffTime, volume, pitch, 0, 0, 0);
        this.living = living;
    }

    @Override
    public void tick()
    {
        if (!living.isAlive())
            endTheNote = true;
        super.tick();
    }

    @Override
    public float getX()
    {
        Vec3d view = living.getLookVec();
        return (float) (living.getPosX() + view.x * 0.3D);
    }

    @Override
    public float getY()
    {
        Vec3d view = living.getLookVec();
        return (float) (living.getPosY() + living.getEyeHeight() + view.y * 0.3D);
    }

    @Override
    public float getZ()
    {
        Vec3d view = living.getLookVec();
        return (float) (living.getPosZ() + view.z * 0.3D);
    }
}
