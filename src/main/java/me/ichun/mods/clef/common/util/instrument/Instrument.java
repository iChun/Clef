package me.ichun.mods.clef.common.util.instrument;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class Instrument
{
    public final InstrumentInfo info;
    public final BufferedImage iconImg;
    public final BufferedImage handImg;
    public InstrumentTuning tuning;

    @SideOnly(Side.CLIENT)
    public InstrumentTexture iconTx;
    @SideOnly(Side.CLIENT)
    public InstrumentTexture handTx;

    public Instrument(InstrumentInfo info, BufferedImage iconImg, BufferedImage handImg)
    {
        this.info = info;
        this.iconImg = iconImg;
        this.handImg = handImg;
    }

    public boolean hasAvailableKey(int key)
    {
        return tuning.keyToTuningMap.containsKey(key);
    }

    @SideOnly(Side.CLIENT)
    public void setupTextures()
    {
        //TODO test reloading the resources and see what happens?
        if(iconTx == null && handTx == null)
        {
            Minecraft mc = Minecraft.getMinecraft();
            iconTx = new InstrumentTexture(new ResourceLocation("clef", "instrument/" + info.itemName + "/icon.png"), iconImg);
            handTx = new InstrumentTexture(new ResourceLocation("clef", "instrument/" + info.itemName + "/hand.png"), handImg);

            mc.getTextureManager().loadTexture(iconTx.rl, iconTx);
            mc.getTextureManager().loadTexture(handTx.rl, handTx);
        }
    }

    @SideOnly(Side.CLIENT)
    public class InstrumentTexture extends AbstractTexture
    {
        public final ResourceLocation rl;
        public final BufferedImage image;
        public ImmutableList<BakedQuad> quads;

        public InstrumentTexture(ResourceLocation rl, BufferedImage image)
        {
            this.rl = rl;
            this.image = image;
        }

        @Override
        public void loadTexture(IResourceManager resourceManager) throws IOException
        {
            TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), image, false, false);

            ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
            TextureAtlasSpriteInstrument tasi = new TextureAtlasSpriteInstrument(this.rl, this.image);
            tasi.load(Minecraft.getMinecraft().getResourceManager(), rl);
            builder.addAll(ItemLayerModel.getQuadsForSprite(0, tasi, DefaultVertexFormats.ITEM, Optional.absent()));
            quads = builder.build();
        }
    }

    @SideOnly(Side.CLIENT)
    public class TextureAtlasSpriteInstrument extends TextureAtlasSprite
    {
        public final BufferedImage image;

        public TextureAtlasSpriteInstrument(ResourceLocation rl, BufferedImage image)
        {
            super(rl.toString());
            this.image = image;
        }

        public boolean hasCustomLoader(net.minecraft.client.resources.IResourceManager manager, net.minecraft.util.ResourceLocation location)
        {
            return true;
        }

        public boolean load(net.minecraft.client.resources.IResourceManager manager, net.minecraft.util.ResourceLocation location)
        {
            this.width = image.getWidth();
            this.height = image.getHeight();

            int[][] aint = new int[Minecraft.getMinecraft().getTextureMapBlocks().getMipmapLevels() + 1][];
            aint[0] = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), aint[0], 0, image.getWidth());

            this.framesTextureData.add(aint);

            this.initSprite(width, height, 0, 0, false);

            return false;
        }
    }

}
