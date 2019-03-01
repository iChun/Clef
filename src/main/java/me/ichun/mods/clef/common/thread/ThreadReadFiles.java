package me.ichun.mods.clef.common.thread;

import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;

import java.util.concurrent.CountDownLatch;

public class ThreadReadFiles extends Thread
{
    public final CountDownLatch latch = new CountDownLatch(1);

    public ThreadReadFiles()
    {
        this.setName("Clef File Reader Thread");
        this.setDaemon(true);
    }

    @Override
    public void run()
    {
        AbcLibrary.init();
        InstrumentLibrary.init();
        latch.countDown();
    }
}
