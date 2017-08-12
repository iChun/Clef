package me.ichun.mods.clef.common.util.abc.play;

import io.netty.util.internal.ThreadLocalRandom;
import me.ichun.mods.clef.client.core.SoundSystemReflect;
import me.ichun.mods.clef.client.sound.InstrumentSound;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentTuning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class PlayedNote
{
    public final Instrument instrument;
    public final int key;
    public final InstrumentSound instrumentSound;
    public final int startTick;
    public final int duration;

    public String uniqueId;
    public boolean played;

    public PlayedNote(Instrument instrument, int startTick, int duration, int key)
    {
        this.instrument = instrument;
        this.key = key;
        this.instrumentSound = new InstrumentSound(SoundEvents.BLOCK_NOTE_HARP, SoundCategory.AMBIENT, 0.7F);
        this.startTick = startTick;
        this.duration = duration;
    }

    public PlayedNote start()
    {
//        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_PIG_AMBIENT, (float)Math.pow(2.0D, (double)((key) - 12 - 48) / 12.0D)));
//        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
        Minecraft mc = Minecraft.getMinecraft();
        SoundManager soundManager = mc.getSoundHandler().sndManager;
        if (mc.gameSettings.getSoundLevel(SoundCategory.MASTER) > 0.0F && instrument.hasAvailableKey(key))
        {
            instrumentSound.createAccessor(mc.getSoundHandler());

            float f3 = instrumentSound.getVolume();
            float f = 16.0F;

            if (f3 > 1.0F)
            {
                f *= f3;
            }

            SoundCategory soundcategory = instrumentSound.getCategory();
            float f1 = soundManager.getClampedVolume(instrumentSound);

            InstrumentTuning.TuningInfo tuning = instrument.tuning.keyToTuningMap.get(key);
            float f2 = (float)Math.pow(2.0D, (double)tuning.keyOffset / 12.0D);

            uniqueId = MathHelper.getRandomUuid(ThreadLocalRandom.current()).toString();

            try
            {
                SoundSystemReflect.ssNewSource.invoke(soundManager.sndSystem, false, uniqueId, getURLForSoundResource(instrument, key - tuning.keyOffset), "clef:" + instrument.info.itemName + ":" + (key - tuning.keyOffset) + ".ogg", false, instrumentSound.getXPosF(), instrumentSound.getYPosF(), instrumentSound.getZPosF(), instrumentSound.getAttenuationType().getTypeInt(), f);
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.sound.PlaySoundSourceEvent(soundManager, instrumentSound, uniqueId));

                SoundSystemReflect.ssSetPitch.invoke(soundManager.sndSystem, uniqueId, f2);
                SoundSystemReflect.ssSetVolume.invoke(soundManager.sndSystem, uniqueId, f1);
                SoundSystemReflect.ssPlay.invoke(soundManager.sndSystem, uniqueId);
            }
            catch(InvocationTargetException | IllegalAccessException e)
            {
                Clef.LOGGER.warn("Error playing instrument sound.");
                e.printStackTrace();
            }
            soundManager.playingSoundsStopTime.put(uniqueId, soundManager.playTime + 20);
            soundManager.playingSounds.put(uniqueId, instrumentSound);

            if (soundcategory != SoundCategory.MASTER)
            {
                soundManager.categorySounds.put(soundcategory, uniqueId);
            }
//            soundManager.tickableSounds.add(instrumentSound);

            played = true;
        }

        return this;
    }

    public void tick(int currentTick)
    {
        if(played && currentTick > startTick + duration)
        {
            int falloff = (int)Math.ceil(instrument.tuning.fadeout * 20D);
            float f1 = Minecraft.getMinecraft().getSoundHandler().sndManager.getClampedVolume(instrumentSound);
            try
            {
                float volume = f1 * (float)((falloff - (currentTick - (startTick + duration))) / (double)falloff);
                SoundSystemReflect.ssSetVolume.invoke(Minecraft.getMinecraft().getSoundHandler().sndManager.sndSystem, uniqueId, volume);
            }
            catch(InvocationTargetException | IllegalAccessException e)
            {
                Clef.LOGGER.warn("Error playing instrument sound.");
                e.printStackTrace();
            }
        }
    }

    public void stop()
    {
        //TODO pass this volume tapering over to the InstrumentSound later.
        if(played)
        {
            try
            {
                SoundSystemReflect.ssStop.invoke(Minecraft.getMinecraft().getSoundHandler().sndManager.sndSystem, uniqueId);
            }
            catch(InvocationTargetException | IllegalAccessException e)
            {
                Clef.LOGGER.warn("Error stopping instrument sound.");
                e.printStackTrace();
            }
        }
        instrumentSound.donePlaying = true;
    }

    private static URL getURLForSoundResource(final Instrument instrument, final int key)
    {
        int randKey = rand.nextInt(instrument.tuning.keyToTuningMap.get(key).stream.length);
        String s = String.format("%s:%s:%s", "clef", instrument.info.itemName, key + ":" + randKey + ".ogg");
        URLStreamHandler urlstreamhandler = new URLStreamHandler()
        {
            protected URLConnection openConnection(final URL p_openConnection_1_)
            {
                return new URLConnection(p_openConnection_1_)
                {
                    public void connect() throws IOException
                    {
                    }
                    public InputStream getInputStream() throws IOException
                    {
                        return instrument.tuning.keyToTuningMap.get(key).stream[randKey];
                    }
                };
            }
        };

        try
        {
            return new URL(null, s, urlstreamhandler);
        }
        catch (MalformedURLException var4)
        {
            throw new Error("Minecraft no has proper error throwing and handling.");
        }
    }

    private static Random rand = new Random();
}
