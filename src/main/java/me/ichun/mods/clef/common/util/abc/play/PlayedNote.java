package me.ichun.mods.clef.common.util.abc.play;

import me.ichun.mods.clef.client.sound.InstrumentEntitySound;
import me.ichun.mods.clef.client.sound.InstrumentSound;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentTuning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class PlayedNote
{
    private static final ConcurrentHashMap<ResourceLocation, CompletableFuture<AudioStreamBuffer>> CLEF_CACHE = new ConcurrentHashMap<>();
    public static final Random rand = new Random();

    public static void clearCache()
    {
        CLEF_CACHE.clear();
    }

    public static void start(Instrument instrument, int startTick, int duration, int key, SoundCategory category, Object noteLocation)
    {
        InstrumentSound instrumentSound;
        InstrumentTuning.TuningInfo tuning = instrument.tuning.keyToTuningMap.get(key);
        float pitch = (float)Math.pow(2.0D, (double)tuning.keyOffset / 12.0D);
        int falloffTime = (int) Math.ceil(instrument.tuning.fadeout * 20F);
        float volume = 0.7F * (Clef.configClient.instrumentVolume / 100F);
        boolean relative;
        if (noteLocation == Minecraft.getInstance().player)
        {
            instrumentSound = new InstrumentSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP, category, duration, falloffTime, volume, pitch, 0, 0, 0);
            relative = true;
        } else if (noteLocation instanceof LivingEntity)
        {
            instrumentSound = new InstrumentEntitySound(SoundEvents.BLOCK_NOTE_BLOCK_HARP, category, duration, falloffTime, volume, pitch, (LivingEntity) noteLocation);
            relative = false;
        } else if (noteLocation instanceof BlockPos)
        {
            BlockPos pos = (BlockPos) noteLocation;
            instrumentSound = new InstrumentSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP, category, duration, falloffTime, volume, pitch, pos.getX() + 0.5F, pos.getY() + 0.5f, pos.getZ() + 0.5F);
            relative = false;
        } else
        {
            throw new IllegalArgumentException("Cannot handle noteLocation of type " + noteLocation.getClass());
        }
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

//            Sound sound = instrumentSound.getSound(); clef: sound is not needed

            float f3 = instrumentSound.getVolume();
            float f = Math.max(f3, 1.0F) * (float) Clef.configClient.instrumentHearableDistance/*sound.getAttenuationDistance()*/; //clef: make instruments hearable from further away

            SoundCategory soundcategory = instrumentSound.getCategory();
            float f1 = soundManager.getClampedVolume(instrumentSound);

            float f2 = (float)Math.pow(2.0D, (double)tuning.keyOffset / 12.0D);

            ISound.AttenuationType isound$attenuationtype = instrumentSound.getAttenuationType();

            //SoundEngine.play
            Vector3d vec3d = new Vector3d(instrumentSound.getX(), instrumentSound.getY(), instrumentSound.getZ());
            CompletableFuture<ChannelManager.Entry> completablefuture = soundManager.channelManager./*createChannel*/func_239534_a_(SoundSystem.Mode.STATIC);
            ChannelManager.Entry channelmanager$entry = completablefuture.join();

            //            soundManager.sndSystem.newSource(false, uniqueId, getURLForSoundResource(instrument, key - tuning.keyOffset), "clef:" + instrument.info.itemName + ":" + (key - tuning.keyOffset) + ".ogg", false, instrumentSound.getXPosF(), instrumentSound.getYPosF(), instrumentSound.getZPosF(), instrumentSound.getAttenuationType().getTypeInt(), f);
            mc.runAsync(() -> {
                soundManager.playingSoundsStopTime.put(instrumentSound, soundManager.ticks + duration + (int)(instrument.tuning.fadeout * 20F) + 20);
                soundManager.playingSoundsChannel.put(instrumentSound, channelmanager$entry);
                soundManager.categorySounds.put(soundcategory, instrumentSound);
                soundManager.tickableSounds.add(instrumentSound);
            }); //clef: this may run offthread

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
                source.setRelative(relative); //clef: relative sound for player
            });
            final ISound isound = instrumentSound;
            //clef:sound is always not streaming
            if (true/*!sound.isStreaming()*/) {
                int randKey = rand.nextInt(tuning.streamsLength());
                createResource(soundManager.audioStreamManager, new ResourceLocation("clef", instrument.info.itemName.toLowerCase(Locale.ROOT) + "_" + (key - tuning.keyOffset) + randKey + ".ogg"), () -> tuning.get(randKey)).thenAccept((buffer) -> {
                    channelmanager$entry.runOnSoundExecutor((source) -> {
                        source.func_216429_a(buffer);
                        source.play();
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.sound.PlaySoundSourceEvent(soundManager, isound, source));
                    });
                });
            } /*else {
                soundManager.audioStreamManager.createStreamingResource(sound.getSoundAsOggLocation()).thenAccept((buffer) -> {
                    channelmanager$entry.runOnSoundExecutor((source) -> {
                        source.func_216433_a(buffer);
                        source.play();
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.sound.PlayStreamingSourceEvent(soundManager, isound, source));
                    });
                });
            }*/
            //END SoundEngine.play
        }
    }

    //Taken from AudioStreamManager.createResource
    public static CompletableFuture<AudioStreamBuffer> createResource(AudioStreamManager audioStreamManager, ResourceLocation rl, Supplier<InputStream> inputStream) {
        //Use a custom cache instead of the AudioStreammanager's cache to be thread safe
        return CLEF_CACHE.computeIfAbsent(rl, (newRL) -> {
            return CompletableFuture.supplyAsync(() -> {
                try (
                        OggAudioStream iaudiostream = new OggAudioStream(inputStream.get());
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
