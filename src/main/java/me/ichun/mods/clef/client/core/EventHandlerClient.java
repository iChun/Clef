package me.ichun.mods.clef.client.core;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.ichun.mods.clef.client.render.BakedModelInstrument;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;

public class EventHandlerClient
{
    public ArrayList<Track> tracksPlaying = new ArrayList<>();

    public TextureAtlasSprite txInstrument;

    public boolean keyDown;

    @SubscribeEvent
    public void onTextureStitchedPre(TextureStitchEvent.Pre event)
    {
        txInstrument = Minecraft.getMinecraft().getTextureMapBlocks().registerSprite(new ResourceLocation("clef", "items/instrument"));
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event)
    {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
        builder.addAll(ItemLayerModel.getQuadsForSprite(0, txInstrument, DefaultVertexFormats.ITEM, Optional.absent()));
        event.getModelRegistry().putObject(new ModelResourceLocation("clef:instrument", "inventory"), new BakedModelInstrument(builder.build(), txInstrument, ImmutableMap.copyOf(new HashMap<>()), null, null));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            for(int i = tracksPlaying.size() - 1; i >= 0; i--)
            {
                Track track = tracksPlaying.get(i);
                if(!track.update())
                {
                    tracksPlaying.remove(i);
                }
            }

            if(keyDown && !Keyboard.isKeyDown(Keyboard.KEY_TAB))
            {
                if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
                {
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                    {
                        if(!tracksPlaying.isEmpty())
                        {
                                                        Instrument instrument = InstrumentLibrary.instruments.get((int)Math.floor(Math.random() * InstrumentLibrary.instruments.size()));
//                            Instrument instrument = InstrumentLibrary.getInstrumentByName("koto");

                            System.out.println("ADDING RANDOM INSTRUMENT: " + instrument.info.itemName);
                            tracksPlaying.get(0).addInstrument(instrument);
                        }
                    }
                    else
                    {
                        for(int i = tracksPlaying.size() - 1; i >= 0; i--)
                        {
                            Track track = tracksPlaying.get(i);
                            track.stop();
                        }
                        tracksPlaying.clear();

                        Instrument instrument = InstrumentLibrary.getInstrumentByName("microphone");
                        if(instrument != null)
                        {
                            TrackInfo trackInfo = AbcLibrary.tracks.get((int)(Math.floor(Math.random() * AbcLibrary.tracks.size()))).track;
                            tracksPlaying.add(new Track(trackInfo, instrument));
                            System.out.println("PLAYING: " + trackInfo.title);
                        }
                        else
                        {
                            System.out.println("NO SUCH INST");
                        }
                    }
                }
                else if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                {
                    for(int i = tracksPlaying.size() - 1; i >= 0; i--)
                    {
                        Track track = tracksPlaying.get(i);
                        track.stop();
                    }
                    tracksPlaying.clear();
                    System.out.println("CLEARING");
                }
                else
                {
                    AbcLibrary.init();
                    InstrumentLibrary.instruments.clear();
                    InstrumentLibrary.init();
                }
            }
            keyDown = Keyboard.isKeyDown(Keyboard.KEY_TAB);
        }
    }
}
