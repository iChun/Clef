package me.ichun.mods.clef.common.util.abc.play;

import io.netty.util.internal.ThreadLocalRandom;
import me.ichun.mods.clef.client.sound.InstrumentSound;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentTuning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.io.InputStream;
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
    public final int startTick;
    public final int duration;
    public final InstrumentSound instrumentSound;
    public Object noteLocation;

    public String uniqueId;
    public boolean played;

    //TODO handle corrupt sound files somehow.

    public PlayedNote(Instrument instrument, int startTick, int duration, int key, SoundCategory category, Object noteLocation)
    {
        this.instrument = instrument;
        this.key = key;
        this.startTick = startTick;
        this.duration = duration;
        this.noteLocation = noteLocation;

        uniqueId = MathHelper.getRandomUUID(ThreadLocalRandom.current()).toString();

        InstrumentTuning.TuningInfo tuning = instrument.tuning.keyToTuningMap.get(key);
        float pitch = (float)Math.pow(2.0D, (double)tuning.keyOffset / 12.0D);
        this.instrumentSound = new InstrumentSound(uniqueId, SoundEvents.BLOCK_NOTE_HARP, category, duration, (int)Math.ceil(instrument.tuning.fadeout * 20F), 0.7F * (Clef.config.instrumentVolume / 100F), pitch, noteLocation);
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

            soundManager.sndSystem.newSource(false, uniqueId, getURLForSoundResource(instrument, key - tuning.keyOffset), "clef:" + instrument.info.itemName + ":" + (key - tuning.keyOffset) + ".ogg", false, instrumentSound.getXPosF(), instrumentSound.getYPosF(), instrumentSound.getZPosF(), instrumentSound.getAttenuationType().getTypeInt(), f);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.sound.PlaySoundSourceEvent(soundManager, instrumentSound, uniqueId));

            soundManager.sndSystem.setPitch(uniqueId, f2);
            soundManager.sndSystem.setVolume(uniqueId, f1);
            soundManager.sndSystem.play(uniqueId);
            soundManager.playingSoundsStopTime.put(uniqueId, soundManager.playTime + duration + (int)(instrument.tuning.fadeout * 20F) + 20);
            soundManager.playingSounds.put(uniqueId, instrumentSound);

            if (soundcategory != SoundCategory.MASTER)
            {
                soundManager.categorySounds.put(soundcategory, uniqueId);
            }
            soundManager.tickableSounds.add(instrumentSound);

            played = true;
        }

        return this;
    }

    private static URL getURLForSoundResource(final Instrument instrument, final int key)
    {
        int randKey = rand.nextInt(instrument.tuning.keyToTuningMap.get(key).streamsLength());
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
                        return instrument.tuning.keyToTuningMap.get(key).get(randKey);
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
