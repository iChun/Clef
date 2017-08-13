package me.ichun.mods.clef.client.core;

import me.ichun.mods.clef.common.Clef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundManager;
import paulscode.sound.SoundSystem;

import java.lang.reflect.Method;
import java.net.URL;

public class SoundSystemReflect
{
    public static Method ssNewSource;
    public static Method ssSetPitch;
    public static Method ssSetVolume;
    public static Method ssPlay;

    public static void init()
    {
        try
        {
            ssNewSource = SoundSystem.class.getDeclaredMethod("newSource", boolean.class, String.class, URL.class, String.class, boolean.class, float.class, float.class, float.class, int.class, float.class);
            ssNewSource.setAccessible(true);
            ssSetPitch = SoundSystem.class.getDeclaredMethod("setPitch", String.class, float.class);
            ssSetPitch.setAccessible(true);
            ssSetVolume = SoundSystem.class.getDeclaredMethod("setVolume", String.class, float.class);
            ssSetVolume.setAccessible(true);
            ssPlay = SoundSystem.class.getDeclaredMethod("play", String.class);
            ssPlay.setAccessible(true);
        }
        catch(NoSuchMethodException exception)
        {
            Clef.LOGGER.warn("Error reflecting into Minecraft's Sound System.");
            exception.printStackTrace();
        }
    }
}
