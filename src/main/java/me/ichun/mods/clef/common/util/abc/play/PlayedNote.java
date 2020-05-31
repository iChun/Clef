package me.ichun.mods.clef.common.util.abc.play;

import me.ichun.mods.clef.client.sound.InstrumentSound;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentTuning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class PlayedNote
{
    public final Instrument instrument;
    public final int key;
    public final int startTick;
    public final int duration;
    public final InstrumentSound instrumentSound;
    public Object noteLocation;

    public boolean played;

    public Random rand = new Random();

    public PlayedNote(Instrument instrument, int startTick, int duration, int key, SoundCategory category, Object noteLocation)
    {
        this.instrument = instrument;
        this.key = key;
        this.startTick = startTick;
        this.duration = duration;
        this.noteLocation = noteLocation;

        InstrumentTuning.TuningInfo tuning = instrument.tuning.keyToTuningMap.get(key);
        float pitch = (float)Math.pow(2.0D, (double)tuning.keyOffset / 12.0D);
        this.instrumentSound = new InstrumentSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP, category, duration, (int)Math.ceil(instrument.tuning.fadeout * 20F), 0.7F * (Clef.configClient.instrumentVolume / 100F), pitch, noteLocation);
    }

    public PlayedNote start()
    {
        //        if(true)
        //        {
        //            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.ENTITY_PIG_AMBIENT, (float)Math.pow(2.0D, (double)((key) - 12 - 48) / 12.0D)));
        //            played = true;
        //            return this;
        //        }
        //        Minecraft.getInstance().getSoundHandler().playSound(sound);
        Minecraft mc = Minecraft.getInstance();
        SoundEngine soundManager = mc.getSoundHandler().sndManager;
        if (mc.gameSettings.getSoundLevel(SoundCategory.MASTER) > 0.0F && instrument.hasAvailableKey(key))
        {
            instrumentSound.createAccessor(mc.getSoundHandler());

            Sound sound = instrumentSound.getSound();

            float f3 = instrumentSound.getVolume();
            float f = Math.max(f3, 1.0F) * (float)sound.getAttenuationDistance();

            SoundCategory soundcategory = instrumentSound.getCategory();
            float f1 = soundManager.getClampedVolume(instrumentSound);

            InstrumentTuning.TuningInfo tuning = instrument.tuning.keyToTuningMap.get(key);
            float f2 = (float)Math.pow(2.0D, (double)tuning.keyOffset / 12.0D);

            ISound.AttenuationType isound$attenuationtype = instrumentSound.getAttenuationType();
            boolean flag = false;

            //SoundEngine.play
            Vec3d vec3d = new Vec3d(instrumentSound.getX(), instrumentSound.getY(), instrumentSound.getZ());
            ChannelManager.Entry channelmanager$entry = soundManager.channelManager.createChannel(SoundSystem.Mode.STATIC);

            //            soundManager.sndSystem.newSource(false, uniqueId, getURLForSoundResource(instrument, key - tuning.keyOffset), "clef:" + instrument.info.itemName + ":" + (key - tuning.keyOffset) + ".ogg", false, instrumentSound.getXPosF(), instrumentSound.getYPosF(), instrumentSound.getZPosF(), instrumentSound.getAttenuationType().getTypeInt(), f);
            soundManager.playingSoundsStopTime.put(instrumentSound, soundManager.ticks + duration + (int)(instrument.tuning.fadeout * 20F) + 20);
            soundManager.playingSoundsChannel.put(instrumentSound, channelmanager$entry);
            soundManager.categorySounds.put(soundcategory, instrumentSound);
            channelmanager$entry.runOnSoundExecutor((source) -> {
                source.setPitch(f2);
                source.setGain(f1);
                if (isound$attenuationtype == ISound.AttenuationType.LINEAR) {
                    source.setLinearAttenuation(f);
                } else {
                    source.setNoAttenuation();
                }

                source.setLooping(false);
                source.updateSource(vec3d);
                source.setRelative(flag);
            });
            final ISound isound = instrumentSound;
            if (!sound.isStreaming()) {
                int randKey = rand.nextInt(tuning.streamsLength());
                createResource(soundManager.audioStreamManager, new ResourceLocation("clef", instrument.info.itemName.toLowerCase(Locale.ROOT) + "_" + (key - tuning.keyOffset) + randKey + ".ogg"), () -> tuning.get(randKey)).thenAccept((buffer) -> {
                    channelmanager$entry.runOnSoundExecutor((source) -> {
                        source.func_216429_a(buffer);
                        source.play();
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.sound.PlaySoundSourceEvent(soundManager, isound, source));
                    });
                });
            } else {
                soundManager.audioStreamManager.createStreamingResource(sound.getSoundAsOggLocation()).thenAccept((buffer) -> {
                    channelmanager$entry.runOnSoundExecutor((source) -> {
                        source.func_216433_a(buffer);
                        source.play();
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.sound.PlayStreamingSourceEvent(soundManager, isound, source));
                    });
                });
            }
            soundManager.tickableSounds.add(instrumentSound);
            //END SoundEngine.play

            played = true;
        }

        return this;
    }

    //Taken from AudioStreamManager.createResource
    public CompletableFuture<AudioStreamBuffer> createResource(AudioStreamManager audioStreamManager, ResourceLocation rl, Supplier<InputStream> inputStream) {
        return audioStreamManager.bufferCache.computeIfAbsent(rl, (newRL) -> {
            return CompletableFuture.supplyAsync(() -> {
                try (
                        IAudioStream iaudiostream = new OggAudioStream(inputStream.get());
                ) {
                    ByteBuffer bytebuffer = iaudiostream.func_216453_b();
                    AudioStreamBuffer audiostreambuffer = new AudioStreamBuffer(bytebuffer, iaudiostream.getAudioFormat());
                    return audiostreambuffer;
                } catch (IOException ioexception) {
                    throw new CompletionException(ioexception);
                }
            }, Util.getServerExecutor());
        });
    }
}
