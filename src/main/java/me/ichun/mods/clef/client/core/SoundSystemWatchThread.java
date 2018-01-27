package me.ichun.mods.clef.client.core;

import me.ichun.mods.clef.common.Clef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.CommandThread;
import paulscode.sound.SoundSystem;

import java.lang.reflect.Field;

/**
 * This is essentially a watchdog for the paulscode soundsystem.
 * It appears to have some issues in it's orbis codec that cause infinite hangs.
 * This happens off thread, but JOrbis acquires the global sound lock {@link paulscode.sound.SoundSystemConfig#THREAD_SYNC}
 * and the sound manager needs to lock it to queue another sound
 * We can't fix it, but we can shutdown the command queue if it is detected and reload it.
 * Some notes won't be played because we void the existing command queue, but at least the game does continue.
 */
@SideOnly(Side.CLIENT)
public class SoundSystemWatchThread extends Thread
{
    private static final int NOTE_TIMEOUT = 2000; //2 seconds to play a single note is very certain a freeze, restart
    private static Field commandThreadField;

    private static long noteStartTime = -1;
    private static long tickStartTime;

    public SoundSystemWatchThread()
    {
        setName("Clef SoundSystem Watcher Thread");
        setDaemon(true);
        setUncaughtExceptionHandler((t, e) ->
        {
            Clef.LOGGER.error("Exception in SoundSystem Watcher Thread: " + e.getMessage());
            e.printStackTrace();
        });
        try
        {
            commandThreadField = SoundSystem.class.getDeclaredField("commandThread");
            commandThreadField.setAccessible(true);
        }
        catch (ReflectiveOperationException e)
        {
            throw new RuntimeException("Could not setup SoundSystem reflection", e);
        }
    }

    @Override
    public void run()
    {
        Clef.LOGGER.info("Starting up sound system watch thread");

        while (true)
        {
            try
            {
                Thread.sleep(1000L);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            int tickTimeout = Clef.config.maxClientTickTime * 1000;

            if (noteStartTime != -1 && (noteStartTime + NOTE_TIMEOUT) < System.currentTimeMillis()) //Most of the time, clef code is in a deadlock
            {
                Clef.LOGGER.warn("Hanging note detected. Restarting sound system command thread");
                restartSoundManager();
            }
            else if (tickStartTime != -1 && tickTimeout != 0 && (tickStartTime + tickTimeout) < System.currentTimeMillis()) //Sometimes vanilla deadlocks because it can't get the sound lock because the bugged JOrbis codec is blocking it
            {
                Clef.LOGGER.warn("Long client tick time: " + tickTimeout  + "ms for one tick! Restarting sound system command thread as a precaution");
                restartSoundManager();
            }
        }
    }

    private static void restartSoundManager()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null)
        {
            Clef.LOGGER.error("Sound System Restart useless: No world present, so the sound system did not cause this hang");
            return;
        }

        SoundManager soundManager = mc.getSoundHandler().sndManager;
        try
        {
            Thread thread = (Thread) commandThreadField.get(soundManager.sndSystem); // grab the existing command thread
            thread.stop(); // hard stop it - we need to free the global lock
            CommandThread newThread = new CommandThread(soundManager.sndSystem); //rebuild a new one
            commandThreadField.set(soundManager.sndSystem, newThread); // set the new thread
            newThread.start(); // and start the sound output again
        }
        catch (IllegalAccessException e)
        {
            Clef.LOGGER.error("Could not restart sound system command thread: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void startPlayingNote()
    {
        noteStartTime = System.currentTimeMillis();
    }

    public static void stopPlayingNote()
    {
        noteStartTime = -1;
    }

    public static void startTick()
    {
        tickStartTime = System.currentTimeMillis();
    }

    public static void stopTick()
    {
        tickStartTime = -1;
    }
}
