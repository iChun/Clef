package me.ichun.mods.clef.common.util.abc.play;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.AbcParser;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages all notes that need to be played at a certain tick
 */
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
        INSTANCE.start();
    }

    public void ensurePresent(TrackTracker trackTracker)
    {
        trackerSet.add(trackTracker);
    }

    public boolean startNewTick()
    {
        boolean result = acquireLock();
        for (int i = runTick.get(); i <= AbcParser.SUB_TICKS; i++)
        {
            runSubTicks();
        }
        trackerSet.forEach(TrackTracker::reset);
        runTick.set(0);
        return result;
    }

    public void endTick(boolean wasLocked)
    {
        done = false;
        releaseLock(wasLocked);
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
            while (done && Minecraft.getInstance().isRunning())
            {
                LockSupport.parkNanos(10000);
            }
            if (!Minecraft.getInstance().isRunning()) break;
            long startTime = Util.nanoTime();
            for (int i = runTick.get(); i <= AbcParser.SUB_TICKS; i++)
            {
                lock.lock();
                try
                {
                    if (runSubTicks()) break;
                } finally
                {
                    lock.unlock();
                }

                long now = Util.nanoTime();
                long targetTime = startTime + (INTERVAL_NANOS * (i + 1));
                long sleepTime = targetTime - now; //sleep shorter, so we are as acurate as possible (if we wake up early, we just spin)
                int nanos = (int) (sleepTime % 1000000);
                long millis = (sleepTime - nanos) / 1000000;
                if (runTick.get() >= AbcParser.SUB_TICKS)
                {
                    done = true;
                    break;
                }
                if (millis > 2)
                {
                    try
                    {
                        Thread.sleep(millis, nanos);
                    } catch (InterruptedException e)
                    {
                        //ignore
                    }
                }

                if (targetTime < Util.nanoTime())
                    System.out.println("No spin :/");
                else
                    System.out.println("Spin!");

                while (targetTime > Util.nanoTime())
                {
                    //spin!
                }
                if (done) break;
            }
        }

    }
}
