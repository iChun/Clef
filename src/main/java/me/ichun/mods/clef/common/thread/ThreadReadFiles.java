package me.ichun.mods.clef.common.thread;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.concurrent.CountDownLatch;

public class ThreadReadFiles extends Thread implements Thread.UncaughtExceptionHandler
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

    @Override
    public UncaughtExceptionHandler getUncaughtExceptionHandler()
    {
        return this;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
        Clef.LOGGER.fatal("Clef File Reader Thread crashed!", e);
        latch.countDown();
        CrashReport report = new CrashReport("Clef File Reader Thread crashed!", e);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().crashed(report));
    }
}
