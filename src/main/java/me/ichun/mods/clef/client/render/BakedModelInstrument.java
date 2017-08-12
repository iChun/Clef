package me.ichun.mods.clef.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.HashMap;
import java.util.List;

public class BakedModelInstrument
        implements IPerspectiveAwareModel
{
    private final ImmutableList<BakedQuad> quads;
    private final TextureAtlasSprite particle;
    private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;
    private final Instrument.InstrumentTexture instrument;

    public BakedModelInstrument(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms, Instrument.InstrumentTexture instrument)
    {
        this.quads = quads;
        this.particle = particle;
        this.transforms = transforms;
        this.instrument = instrument;
    }

    public boolean isAmbientOcclusion() { return true; }
    public boolean isGui3d() { return false; }
    public boolean isBuiltInRenderer() { return false; }
    public TextureAtlasSprite getParticleTexture() { return particle; }
    public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
    public ItemOverrideList getOverrides() { return BakedModelInstrument.ItemOverrideListHandler.INSTANCE; }
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
    {
        if(side == null)
        {
            if(instrument != null)
            {
                Minecraft.getMinecraft().getTextureManager().bindTexture(instrument.rl);
            }
            return quads;
        }
        return ImmutableList.of();
    }

    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type)
    {
        Pair<? extends IBakedModel, Matrix4f> pair = IPerspectiveAwareModel.MapWrapper.handlePerspective(this, transforms, type);
        return pair;
    }

    private static final class ItemOverrideListHandler extends ItemOverrideList
    {
        private static final BakedModelInstrument.ItemOverrideListHandler INSTANCE = new BakedModelInstrument.ItemOverrideListHandler();

        private ItemOverrideListHandler()
        {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
        {
            //TODO switch models according to this.
            NBTTagCompound tag = stack.getTagCompound();
            if(tag != null)
            {
                Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
                if(instrument != null)
                {
                    instrument.setupTextures();
                    return new BakedModelInstrument(instrument.iconTx.quads, Clef.eventHandlerClient.txInstrument, ImmutableMap.copyOf(new HashMap<>()), instrument.iconTx);
                }
                //TODO should we request for the instrument here?
            }
            return originalModel;
        }
    }
}
