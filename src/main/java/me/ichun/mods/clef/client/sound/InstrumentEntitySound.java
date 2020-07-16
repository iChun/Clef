package me.ichun.mods.clef.client.sound;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;

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
    public double getX()
    {
        Vector3d view = living.getLookVec();
        return living.getPosX() + view.x * 0.3D;
    }

    @Override
    public double getY()
    {
        Vector3d view = living.getLookVec();
        return living.getPosY() + living.getEyeHeight() + view.y * 0.3D;
    }

    @Override
    public double getZ()
    {
        Vector3d view = living.getLookVec();
        return living.getPosZ() + view.z * 0.3D;
    }
}
