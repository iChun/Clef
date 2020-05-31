package me.ichun.mods.clef.common.util.instrument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import me.ichun.mods.clef.client.render.BakedModelInstrument;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentInfo;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentPackInfo;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentTuning;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.model.TransformationHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("deprecation")
public class Instrument
        implements Comparable<Instrument>
{
    public final InstrumentInfo info;
    public final byte[] iconBytes; //Raw input stream bytes
    public final byte[] handBytes; //Raw input stream bytes
    public InstrumentTuning tuning;
    public InstrumentPackInfo packInfo;

    @OnlyIn(Dist.CLIENT)
    public BakedModelInstrument iconModel;
    @OnlyIn(Dist.CLIENT)
    public BakedModelInstrument handModel;
    @OnlyIn(Dist.CLIENT)
    public ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transformationMap;


    public Instrument(InstrumentInfo info, byte[] iconBytes, byte[] handBytes)
    {
        this.info = info;
        this.iconBytes = iconBytes;
        this.handBytes = handBytes;
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
            out.write(iconBytes, 0, iconBytes.length);
            out.closeEntry();

            if(!info.inventoryIcon.equals(info.activeImage))
            {
                out.putNextEntry(new ZipEntry("items/instruments/" + info.activeImage));
                out.write(handBytes, 0, handBytes.length);
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

    @OnlyIn(Dist.CLIENT)
    public void setupModels()
    {
        if(iconModel == null && handModel == null)
        {
            Minecraft mc = Minecraft.getInstance();

            ResourceLocation iconRl = new ResourceLocation("clef", "instrument/" + info.itemName + "/icon.png");
            ResourceLocation handRl = new ResourceLocation("clef", "instrument/" + info.itemName + "/hand.png");

            InstrumentTexture iconTx = new InstrumentTexture(iconRl, iconBytes);
            InstrumentTexture handTx = new InstrumentTexture(handRl, handBytes);

            mc.getTextureManager().loadTexture(iconTx.rl, iconTx);
            mc.getTextureManager().loadTexture(handTx.rl, handTx);

            iconModel = new BakedModelInstrument(iconTx.quads, iconTx.tas, ImmutableMap.copyOf(new HashMap<>()), this, iconRl);
            handModel = new BakedModelInstrument(handTx.quads, handTx.tas, ImmutableMap.copyOf(new HashMap<>()), this, handRl);

            HashMap<ItemCameraTransforms.TransformType, TransformationMatrix> map = new HashMap<>();
            IBakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(new ItemStack(Items.BRICK), null, null);
            ItemCameraTransforms cameraTransforms = model.getItemCameraTransforms();
            //taken from item/generated.json
            map.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, new TransformationMatrix(new Vector3f( 0, 3F / 16F, 1F / 16F ), null, null, null));
            map.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, new TransformationMatrix(new Vector3f( 0, 3F / 16F, 1F / 16F ), null, null, null));
            map.put(ItemCameraTransforms.TransformType.GROUND, TransformationHelper.toTransformation(cameraTransforms.ground));
            map.put(ItemCameraTransforms.TransformType.HEAD, TransformationHelper.toTransformation(cameraTransforms.head));
            map.put(ItemCameraTransforms.TransformType.FIXED, TransformationHelper.toTransformation(cameraTransforms.fixed));
            transformationMap = ImmutableMap.copyOf(map);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class InstrumentTexture extends Texture
    {
        public final ResourceLocation rl;
        public NativeImage image;
        public ImmutableList<BakedQuad> quads;
        public TextureAtlasSprite tas;

        public InstrumentTexture(ResourceLocation rl, byte[] imageBytes)
        {
            this.rl = rl;
            try (NativeImage image = NativeImage.read(new ByteArrayInputStream(imageBytes)))
            {
                int size = Math.max(Math.max(image.getWidth(), image.getHeight()), 16);
                //            BufferedImage image1 = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                NativeImage image1 = new NativeImage(size, size, true);
                int halfX = (int)Math.floor((size - image.getWidth()) / 2D); //offsetX
                int halfY = (int)Math.floor((size - image.getHeight()) / 2D); //offsetY
                for(int x = 0; x < image.getWidth(); x++)
                {
                    for(int y = 0; y < image.getHeight(); y++)
                    {
                        int clr = image.getPixelRGBA(x, y);
                        //                        System.out.println(Integer.toHexString(clr));
                        //                        if(clr != 0xffffff)
                        {
                            image1.setPixelRGBA(halfX + x, halfY + y, clr);
                        }
                    }
                }
                this.image = image1;

                this.tas = RenderHelper.buildTASFromNativeImage(this.rl, this.image);
                ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
                builder.addAll(ItemLayerModel.getQuadsForSprite(0, tas, TransformationMatrix.identity()));
                this.quads = builder.build();
            }
            catch(IOException e)
            {
                this.image = null;
                Clef.LOGGER.error("Failed to read NativeImage for " + rl.toString());
                e.printStackTrace();
            }
        }

        @Override
        public void loadTexture(IResourceManager resourceManager) throws IOException
        {
            if(image != null)
            {
                TextureUtil.prepareImage(getGlTextureId(), image.getWidth(), image.getHeight());
                image.uploadTextureSub(0, 0, 0, false);
            }
        }
    }
}
