package me.ichun.mods.clef.client.render;

import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.client.model.item.IModelBase;
import me.ichun.mods.ichunutil.client.model.item.ModelBaseWrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

@SuppressWarnings("deprecation")
public class ItemRenderInstrument implements IModelBase
{
    public static final ResourceLocation TX_GUITAR = new ResourceLocation("clef", "textures/items/instrument.png");

    //Stuff to do in relation to getting the current stack and the current player holding it
    private ItemStack heldStack;
    private ItemCameraTransforms.TransformType currentPerspective;
    public EntityLivingBase lastEntity;

    @Nonnull
    @Override
    public ResourceLocation getTexture()
    {
        return TX_GUITAR;
    }

    @Override
    public void renderModel(float renderTick)
    {
        NBTTagCompound tag = heldStack.getTagCompound();
        if(tag != null)
        {
            Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
            if(instrument != null)
            {
                if(ModelBaseWrapper.isItemRender(currentPerspective))
                {
                    //item icon
                }
                else
                {
                    //larger icon
                }
            }
            else
            {
                //TODO request for the instrument from the server if we don't have it.
            }
        }
    }

    @Override
    public void postRender()
    {
        heldStack = null;
        lastEntity = null;
        currentPerspective = null;
    }

    @Nonnull
    @Override
    public ModelBase getModel()
    {
        return null; //YEAH UH NOTHING USES THIS SOOOO.... LEAVE IT NULL.
    }

    @Override
    public ItemCameraTransforms getCameraTransforms()
    {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public void handleBlockState(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {}

    @Override
    public void handleItemState(ItemStack stack, World world, EntityLivingBase entity)
    {
        lastEntity = entity;
        heldStack = stack;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, Pair<? extends IBakedModel, Matrix4f> pair)
    {
        currentPerspective = cameraTransformType;
        return pair;
    }

    @Override
    public boolean useVanillaCameraTransform()
    {
        return true;
    }
}
