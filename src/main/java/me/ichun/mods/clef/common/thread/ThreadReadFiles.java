package me.ichun.mods.clef.common.thread;

import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;

public class ThreadReadFiles extends Thread
{
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
    }
}
