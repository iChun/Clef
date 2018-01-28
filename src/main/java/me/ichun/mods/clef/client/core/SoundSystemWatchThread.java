package me.ichun.mods.clef.client.core;

import me.ichun.mods.clef.common.Clef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
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
    private static Field debugUpdateTimeField; //TODO move this to an AT
    private static Field hasCrashedField;
    private static boolean didRestartTickTimeout = false;

    private static long noteStartTime = -1;

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
            debugUpdateTimeField = ReflectionHelper.findField(Minecraft.class, "debugUpdateTime", "field_71419_L");
            debugUpdateTimeField.setAccessible(true);
            hasCrashedField = ReflectionHelper.findField(Minecraft.class, "hasCrashed", "field_71434_R");
            hasCrashedField.setAccessible(true);
        }
        catch (ReflectiveOperationException e)
        {
            throw new RuntimeException("Could not setup SoundSystem reflection", e);
        }
    }

    private static boolean hasCrashed()
    {
        try
        {
            return hasCrashedField.getBoolean(Minecraft.getMinecraft());
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static boolean isTickTimeout(int tickTimeout)
    {
        Minecraft minecraft = Minecraft.getMinecraft();
        //Check if we are in a world and fully connected
        if (minecraft.isGamePaused() || minecraft.world == null || minecraft.player == null || minecraft.player.connection == null)
        {
            didRestartTickTimeout = false;
            return false;
        }

        long debugUpdateTime;
        try
        {
            debugUpdateTime = debugUpdateTimeField.getLong(Minecraft.getMinecraft()); //Field updated in Minecraft#runGameLoop
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }

        boolean timeout = Minecraft.getSystemTime() > (debugUpdateTime + tickTimeout);
        if (!timeout)
        {
            didRestartTickTimeout = false;
        }
        return timeout;
    }

    @Override
    public void run()
    {
        Clef.LOGGER.info("Starting up sound system watch thread");

        while (!hasCrashed())
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
                stopPlayingNote();
            }
            else if (tickTimeout != 0 && isTickTimeout(tickTimeout)) //Sometimes vanilla deadlocks because it can't get the sound lock because the bugged JOrbis codec is blocking it
            {
                if (!didRestartTickTimeout) //only restart once per hang, if this doesn't fix it, it isn't our fault
                {
                    //There are still some edge cases where we restart where it isn't needed (resource reload on remote server), but it doesn't seem to cause any issues, as the client is not using the sound system either
                    Clef.LOGGER.info("Long client tick time: " + tickTimeout + "ms for one tick! Restarting sound system command thread as a precaution");
                    restartSoundManager();
                }
                didRestartTickTimeout = true;
            }
        }
    }

    private static void restartSoundManager()
    {
        Minecraft mc = Minecraft.getMinecraft();
        SoundManager soundManager = mc.getSoundHandler().sndManager;
        try
        {
            Thread thread = (Thread) commandThreadField.get(soundManager.sndSystem); // grab the existing command thread
            if (thread == null) //SoundSystem is currently being reloaded or is stopped, so it isn't the sound system that's freezing the client
            {
                Clef.LOGGER.info("Cannot restart sound thread, as it isn't running at the moment");
                return;
            }
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
}
