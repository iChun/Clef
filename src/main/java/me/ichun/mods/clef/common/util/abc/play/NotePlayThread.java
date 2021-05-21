package me.ichun.mods.clef.common.util.abc.play;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.AbcParser;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages all notes that need to be played at a certain tick
 */
@OnlyIn(Dist.CLIENT)
public class NotePlayThread extends Thread
{
    public static final NotePlayThread INSTANCE = new NotePlayThread();
    private static final int INTERVAL_NANOS = (50 * 1000 * 1000) / AbcParser.SUB_TICKS;
    private final Set<TrackTracker> trackerSet = Collections.newSetFromMap(new WeakHashMap<>());
    private final AtomicInteger runTick = new AtomicInteger();
    private final Lock lock = new ReentrantLock();
    private volatile boolean done = true;

    static
    {
        INSTANCE.setName("Clef NotePlay Thread");
        INSTANCE.setDaemon(true);
        INSTANCE.setPriority(7); //slightly above normal to get better scheduler accuracy
        INSTANCE.start();
    }

    /**
     * Adds a tracker to a list, to make sure the scheduled sub ticks are played
     */
    public void ensurePresent(TrackTracker trackTracker)
    {
        trackerSet.add(trackTracker);
    }

    /**
     * Finishes all non-played sounds when the NotePlay Thread has been late, and prepares the thread to wait for new work
     * @return If the lock could be acquired
     */
    public boolean startNewTick()
    {
        boolean result = acquireLock();
        if (!done)
        {
            for (int i = runTick.get(); i < AbcParser.SUB_TICKS; i++)
            {
                runSubTicks();
            }
        }
        trackerSet.forEach(TrackTracker::reset);
        runTick.set(0);
        return result;
    }

    /**
     * Sends the thread off to run the work scheduled
     * @param wasLocked The result of {@link #startNewTick()}
     */
    public void endTick(boolean wasLocked)
    {
        done = false;
        releaseLock(wasLocked);
        synchronized (this)
        {
            notifyAll();
        }
    }

    public boolean acquireLock()
    {
        boolean result = true;
        try
        {
            if (!lock.tryLock(500L, TimeUnit.MILLISECONDS))
            {
                Clef.LOGGER.error("Failed to aquire lock for track at startNewTick in 500ms, continuing anyway!");
                result = false;
            }
        } catch (InterruptedException e)
        {
            Clef.LOGGER.warn("Unexpected main thread interrupt!", e);
            result = false;
        }
        return result;
    }

    public void releaseLock(boolean wasLocked)
    {
        if (wasLocked)
            lock.unlock();
    }

    /**
     * Runs the current subtick, playing all sounds from all trackers queued for this subtick.
     * Requires {@link #lock} to be locked by the calling thread
     * @return True if the tick limit has been reached, and no further data is available
     */
    private boolean runSubTicks()
    {
        Iterator<TrackTracker> iterator = trackerSet.iterator();
        int val = runTick.getAndIncrement();
        if (val >= AbcParser.SUB_TICKS)
        {
            return true;
        }
        while (iterator.hasNext())
        {
            TrackTracker tracker = iterator.next();
            if (tracker.didNotStart())
            {
                iterator.remove();
            }
            else
            {
                tracker.runSubTick(val);
            }
        }
        return false;
    }

    @Override
    public void run()
    {
        while (Minecraft.getInstance().isRunning())
        {
            synchronized (this)
            {
                while (done && Minecraft.getInstance().isRunning())
                {
                    try
                    {
                        wait();
                    } catch (InterruptedException e)
                    {
                        //doesn't really matter
                    }
                }
            }
            if (!Minecraft.getInstance().isRunning()) break;
            long startTime = Util.nanoTime();
            for (int i = runTick.get(); i < AbcParser.SUB_TICKS; i++)
            {
                lock.lock();
                try
                {
                    if (runSubTicks()) break;
                } finally
                {
                    lock.unlock();
                }

                if (runTick.get() >= AbcParser.SUB_TICKS)
                {
                    done = true;
                    break;
                }

                //Calculate how much time we spend on this play and how much time we still have left to sleep
                long targetTime = startTime + (INTERVAL_NANOS * (i + 1));
                long now = Util.nanoTime();
                long sleepTime = targetTime - now - 200; //sleep shorter, so we are as accurate as possible (if we wake up early, we just spin)
                if (sleepTime > 0)
                {
                    int nanos = (int) (sleepTime % 1000000);
                    long millis = (sleepTime - nanos) / 1000000;
                    if (millis > 1)
                    {
                        try
                        {
                            Thread.sleep(millis, nanos);
                        } catch (InterruptedException e)
                        {
                            //ignore
                        }
                    }
                }

                while (targetTime > Util.nanoTime())
                {
                    //spin for the rest of the time left!
                }
                if (done) break;
            }
        }

    }
}
