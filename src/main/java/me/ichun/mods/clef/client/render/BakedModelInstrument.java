package me.ichun.mods.clef.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.client.model.item.ModelBaseWrapper;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.List;

public class BakedModelInstrument
        implements IPerspectiveAwareModel
{
    private final ImmutableList<BakedQuad> quads;
    private final TextureAtlasSprite particle;
    private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;
    private final Instrument instrument;
    public final ResourceLocation instTx;

    public BakedModelInstrument(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms, Instrument instrument, ResourceLocation instTx)
    {
        this.quads = quads;
        this.particle = particle;
        this.transforms = transforms;
        this.instrument = instrument;
        this.instTx = instTx;
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
                Minecraft.getMinecraft().getTextureManager().bindTexture(instTx);
            }
            return quads;
        }
        return ImmutableList.of();
    }

    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type)
    {
        if(instrument != null)
        {
            HashMap<ItemCameraTransforms.TransformType, TRSRTransformation> map = new HashMap<>();
            map.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, new TRSRTransformation(new Vector3f(1F, 0F, 1F), TRSRTransformation.quatFromXYZDegrees(new Vector3f(0F, 180F, 0F)), new Vector3f(1F, 1F, 1F), new Quat4f()));
            map.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, new TRSRTransformation(new Vector3f(0.1F, 0F + (instrument.handImg.getHeight() <= 16F ? 0F : MathHelper.clamp_float((float)instrument.info.activeHandPosition[1], -0.3F, 0.3F)), 0.025F - (instrument.handImg.getWidth() <= 16F ? 0F : MathHelper.clamp_float((float)instrument.info.activeHandPosition[0], -0.5F, 0.5F))), TRSRTransformation.quatFromXYZDegrees(new Vector3f(0F, 80F, 0F)), new Vector3f(-1F, 1F, 1F),  TRSRTransformation.quatFromXYZDegrees(new Vector3f(0F, 0F, 0F))));
            map.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, new TRSRTransformation(new Vector3f(-0.1F, 0F + (instrument.handImg.getHeight() <= 16F ? 0F : MathHelper.clamp_float((float)instrument.info.activeHandPosition[1], -0.3F, 0.3F)), 1F - (instrument.handImg.getWidth() <= 16F ? 0F : MathHelper.clamp_float((float)instrument.info.activeHandPosition[0], -0.5F, 0.5F))), TRSRTransformation.quatFromXYZDegrees(new Vector3f(0F, 80F, 0F)), new Vector3f(1F, 1F, 1F), TRSRTransformation.quatFromXYZDegrees(new Vector3f(0F, 0F, 0F))));
            ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms = ImmutableMap.copyOf(map);
            return IPerspectiveAwareModel.MapWrapper.handlePerspective(ModelBaseWrapper.isEntityRender(type) ? instrument.handModel : instrument.iconModel, transforms, type);
        }
        return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, transforms, type);
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
            NBTTagCompound tag = stack.getTagCompound();
            if(tag != null)
            {
                Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
                if(instrument != null)
                {
                    instrument.setupModels();
                    return instrument.iconModel;
                }
                else
                {
                    InstrumentLibrary.requestInstrument(tag.getString("itemName"), null);
                }
            }
            return originalModel;
        }
    }
}
