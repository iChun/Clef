package me.ichun.mods.clef.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.clef.common.item.ItemLayerModel;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.client.model.item.ItemModelRenderer;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.PerspectiveMapWrapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BakedModelInstrument //mostly taken from forge's BakedItemModel
        implements IBakedModel
{
    public static BakedModelInstrument currentModel = null;

    private final ImmutableList<BakedQuad> quads;
    private final TextureAtlasSprite particle;
    private final ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transforms;
    private final Instrument instrument;
    public final ResourceLocation instTx;

    public BakedModelInstrument(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transforms, Instrument instrument, ResourceLocation instTx)
    {
        this.quads = quads;
        this.particle = particle;
        this.transforms = transforms;
        this.instrument = instrument;
        this.instTx = instTx;
    }

    @Override
    public boolean isAmbientOcclusion() { return true; }
    @Override
    public boolean isGui3d() { return false; }
    @Override //??
    public boolean func_230044_c_()
    {
        return false;
    }
    @Override
    public boolean isBuiltInRenderer() { return true; }
    @Override
    public TextureAtlasSprite getParticleTexture() { return particle; }
    @Override
    public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
    @Override
    public ItemOverrideList getOverrides() { return BakedModelInstrument.ItemOverrideListHandler.INSTANCE; }
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand)
    {
//        if(instrument != null)
//        {
//            ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
//            builder.addAll(ItemLayerModel.getQuadsForSprite(0, particle, TransformationMatrix.identity()));
//            return builder.build();
//        }
        if(side == null)
        {
            return quads;
        }
        return ImmutableList.of();
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType type, MatrixStack mat)
    {
        if(instrument != null)
        {
            currentModel = ItemModelRenderer.isEntityRender(type) ? instrument.handModel : instrument.iconModel;
            PerspectiveMapWrapper.handlePerspective(currentModel, instrument.transformationMap, type, mat);
            return currentModel;
        }
        currentModel = this;
        return PerspectiveMapWrapper.handlePerspective(this, transforms, type, mat);
    }

    private static final class ItemOverrideListHandler extends ItemOverrideList
    {
        private static final BakedModelInstrument.ItemOverrideListHandler INSTANCE = new BakedModelInstrument.ItemOverrideListHandler();

        private ItemOverrideListHandler()
        {
            super();
        }

        @Override
        public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable LivingEntity entity)
        {
            CompoundNBT tag = stack.getTag();
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
