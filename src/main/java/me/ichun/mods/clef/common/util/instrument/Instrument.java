package me.ichun.mods.clef.common.util.instrument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import me.ichun.mods.clef.client.render.BakedModelInstrument;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentInfo;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentPackInfo;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentTuning;
import me.ichun.mods.ichunutil.client.render.TextureAtlasSpriteBufferedImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Instrument
        implements Comparable<Instrument>
{
    public final InstrumentInfo info;
    public final BufferedImage iconImg;
    public final BufferedImage handImg;
    public InstrumentTuning tuning;
    public InstrumentPackInfo packInfo;

    @SideOnly(Side.CLIENT)
    public BakedModelInstrument iconModel;
    @SideOnly(Side.CLIENT)
    public BakedModelInstrument handModel;

    public Instrument(InstrumentInfo info, BufferedImage iconImg, BufferedImage handImg)
    {
        this.info = info;
        this.iconImg = iconImg;
        this.handImg = handImg;
    }

    public boolean hasAvailableKey(int key)
    {
        return tuning.keyToTuningMap.containsKey(key) && tuning.keyToTuningMap.get(key).streamsLength() > 0;
    }

    @Override
    public int compareTo(Instrument o)
    {
        if(packInfo.packName.toLowerCase().equals(o.packInfo.packName.toLowerCase()))
        {
            return info.shortdescription.toLowerCase().compareTo(o.info.shortdescription.toLowerCase());
        }
        return packInfo.packName.toLowerCase().compareTo(o.packInfo.packName.toLowerCase());
    }

    public ByteArrayOutputStream getAsBAOS()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            ZipOutputStream out = new ZipOutputStream(baos);
            out.setLevel(9);

            if(packInfo != null)
            {
                String desc = packInfo.description;
                packInfo.description = packInfo.description.concat(" - This pack is a single instrument from the main pack.");
                out.putNextEntry(new ZipEntry("info.cii"));
                byte[] data = (new Gson()).toJson(packInfo).getBytes();
                out.write(data, 0, data.length);
                out.closeEntry();
                packInfo.description = desc;
            }

            out.putNextEntry(new ZipEntry("items/"));
            out.putNextEntry(new ZipEntry("items/instruments/"));
            out.putNextEntry(new ZipEntry("items/instruments/" + info.itemName + ".instrument"));
            byte[] data = (new Gson()).toJson(info).getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

            out.putNextEntry(new ZipEntry("items/instruments/" + info.inventoryIcon));
            ImageIO.write(iconImg, "png", out);
            out.closeEntry();

            if(!info.inventoryIcon.equals(info.activeImage))
            {
                out.putNextEntry(new ZipEntry("items/instruments/" + info.activeImage));
                ImageIO.write(handImg, "png", out);
                out.closeEntry();
            }

            out.putNextEntry(new ZipEntry("sfx/"));
            out.putNextEntry(new ZipEntry("sfx/instruments/"));
            out.putNextEntry(new ZipEntry("sfx/instruments/" + info.kind + "/"));
            out.putNextEntry(new ZipEntry("sfx/instruments/" + info.kind + "/tuning.config"));
            data = (new Gson()).toJson(tuning).getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

            for(Map.Entry<String, byte[]> e : tuning.audioToOutputStream.entrySet())
            {
                out.putNextEntry(new ZipEntry("sfx/instruments/" + info.kind + "/" + e.getKey()));
                out.write(e.getValue());
                out.closeEntry();
            }

            out.close();

            return baos;
        }
        catch(Exception e)
        {
            Clef.LOGGER.warn("Error creating instrument package: " + info.itemName);
            e.printStackTrace();
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void setupModels()
    {
        if(iconModel == null && handModel == null)
        {
            Minecraft mc = Minecraft.getMinecraft();

            ResourceLocation iconRl = new ResourceLocation("clef", "instrument/" + info.itemName + "/icon.png");
            ResourceLocation handRl = new ResourceLocation("clef", "instrument/" + info.itemName + "/hand.png");

            InstrumentTexture iconTx = new InstrumentTexture(iconRl, iconImg);
            InstrumentTexture handTx = new InstrumentTexture(handRl, handImg);

            mc.getTextureManager().loadTexture(iconTx.rl, iconTx);
            mc.getTextureManager().loadTexture(handTx.rl, handTx);

            iconModel = new BakedModelInstrument(iconTx.quads, iconTx.tasi, ImmutableMap.copyOf(new HashMap<>()), this, iconRl);
            handModel = new BakedModelInstrument(handTx.quads, handTx.tasi, ImmutableMap.copyOf(new HashMap<>()), this, handRl);
        }
    }

    @SideOnly(Side.CLIENT)
    public class InstrumentTexture extends AbstractTexture
    {
        public final ResourceLocation rl;
        public final BufferedImage image;
        public ImmutableList<BakedQuad> quads;
        public TextureAtlasSpriteBufferedImage tasi;

        public InstrumentTexture(ResourceLocation rl, BufferedImage image)
        {
            this.rl = rl;
            int size = Math.max(Math.max(image.getWidth(), image.getHeight()), 16);
            BufferedImage image1 = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            int halfX = (int)Math.floor((size - image.getWidth()) / 2D); //offsetX
            int halfY = (int)Math.floor((size - image.getHeight()) / 2D); //offsetY
            for(int x = 0; x < image.getWidth(); x++)
            {
                for(int y = 0; y < image.getHeight(); y++)
                {
                    int clr = image.getRGB(x, y);
                    if(clr != 0xffffffff)
                    {
                        image1.setRGB(halfX + x, halfY + y, clr);
                    }
                }
            }
            this.image = image1;
        }

        @Override
        public void loadTexture(IResourceManager resourceManager) throws IOException
        {
            TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), image, false, false);

            ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
            tasi = new TextureAtlasSpriteBufferedImage(this.rl, this.image);
            tasi.load(Minecraft.getMinecraft().getResourceManager(), rl);
            builder.addAll(ItemLayerModel.getQuadsForSprite(0, tasi, DefaultVertexFormats.ITEM, Optional.empty()));
            quads = builder.build();
        }
    }
}
