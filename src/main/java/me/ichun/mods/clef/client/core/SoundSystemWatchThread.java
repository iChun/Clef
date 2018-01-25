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
 * We can't fix it, but we can shutdown the command queue if it is detected and reload it.
 * Some notes won't be played because we void the existing command queue, but at least the game does continue.
 */
@SideOnly(Side.CLIENT)
public class SoundSystemWatchThread extends Thread
{
    private static final int timeout = 3000;
    private static long startTime = -1;

    public SoundSystemWatchThread()
    {
        setName("Clef SoundSystem Watcher Thread");
        setDaemon(true);
        setUncaughtExceptionHandler((t, e) ->
        {
            Clef.LOGGER.error("Exception in SoundSystem Watcher Thread: " + e.getMessage());
            e.printStackTrace();
        });
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

            if (startTime == -1)
            {
                continue;
            }

            if ((startTime + timeout) < System.currentTimeMillis())
            {
                Clef.LOGGER.warn("Hanging note detected. Restarting sound system command thread");
                Minecraft mc = Minecraft.getMinecraft();
                SoundManager soundManager = mc.getSoundHandler().sndManager;
                try
                {
                    Field f = SoundSystem.class.getDeclaredField("commandThread"); // needs reflection, no at because no obfuscated mc code
                    f.setAccessible(true);
                    Thread thread = (Thread) f.get(soundManager.sndSystem); // grab the existing command thread
                    thread.stop(); // void in completely
                    CommandThread newThread = new CommandThread(soundManager.sndSystem); //rebuild a new one
                    f.set(soundManager.sndSystem, newThread); // set the new thread
                    newThread.start(); // and start the sound output again
                }
                catch (ReflectiveOperationException e)
                {
                    Clef.LOGGER.error("Could not restart sound system command thread: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public static void startPlayingNote()
    {
        startTime = System.currentTimeMillis();
    }

    public static void stopPlayingNote()
    {
        startTime = -1;
    }
}
