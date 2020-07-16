package me.ichun.mods.clef.common.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.clef.client.gui.GuiPlayTrack;
import me.ichun.mods.clef.client.render.BakedModelInstrument;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.item.DualHandedItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemInstrument extends Item
        implements DualHandedItem
{
    public ItemInstrument(Properties properties)
    {
        super(DistExecutor.runForDist(() -> () -> attachISTER(properties), () -> () -> properties));
        //        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    public static Properties attachISTER(Properties properties)
    {
        return properties.setISTER(() -> () -> new ItemStackTileEntityRenderer() {

            @Override
            public void /*render*/func_239207_a_(ItemStack itemStackIn, ItemCameraTransforms.TransformType p_239207_2_, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
            {
                if(BakedModelInstrument.currentModel != null)
                {
                    IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntityTranslucent(BakedModelInstrument.currentModel.instTx != null ? BakedModelInstrument.currentModel.instTx : AtlasTexture.LOCATION_BLOCKS_TEXTURE));
                    RenderHelper.renderModel(BakedModelInstrument.currentModel, itemStackIn, combinedLightIn, combinedOverlayIn, matrixStackIn, ivertexbuilder);
                }
            }
        });
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player)
    {
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ItemStack is = player.getHeldItem(hand);
        if(is.getTag() == null && !world.isRemote)
        {
            InstrumentLibrary.assignRandomInstrument(is);
        }
        if(getUsableInstrument(player) == is)
        {
            if(player.world.isRemote)
            {
                Track track = Clef.eventHandlerClient.getTrackPlayedByPlayer(player);
                if(track == null)
                {
                    //Open the GUI
                    openGui();
                }
                else
                {
                    Clef.eventHandlerClient.stopPlayingTrack(player);
                }
            }
            return new ActionResult<>(ActionResultType.SUCCESS, is);
        }
        return new ActionResult<>(ActionResultType.FAIL, is);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity)
    {
        if (player.world.isRemote) //is fired on the server as well
        {
            Clef.eventHandlerClient.stopPlayingTrack(player);
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void openGui()
    {
        Minecraft.getInstance().displayGuiScreen(new GuiPlayTrack());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillItemGroup(ItemGroup tab, NonNullList<ItemStack> items)
    {
        if (this.isInGroup(tab))
        {
            for(Instrument intrument : InstrumentLibrary.instruments)
            {
                ItemStack stack = new ItemStack(this);
                CompoundNBT stackTag = new CompoundNBT();
                stackTag.putString("itemName", intrument.info.itemName);
                stack.setTag(stackTag);
                items.add(stack);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack is, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flag)
    {
        CompoundNBT tag = is.getTag();
        if(tag != null)
        {
            Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
            if(instrument != null)
            {
                list.add(new StringTextComponent(instrument.info.description));
                if(!Clef.configServer.allowOneHandedTwoHandedInstrumentUse)
                {
                    list.add(new TranslationTextComponent(instrument.info.twoHanded ? "clef.item.twoHanded" : "clef.item.oneHanded"));
                }
                if(Screen.hasShiftDown() || flag.isAdvanced())
                {
                    list.add(new StringTextComponent(""));
                    list.add(new TranslationTextComponent("clef.item.packName", instrument.packInfo.packName));
                    list.add(new TranslationTextComponent("clef.item.itemName", instrument.info.itemName));
                }
            }
        }
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack)
    {

        CompoundNBT tag = stack.getTag();
        if(tag != null)
        {
            Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
            if(instrument != null)
            {
                return new StringTextComponent(instrument.info.shortdescription);
            }
        }
        return super.getDisplayName(stack);
    }

    @Override
    public String getTranslationKey(ItemStack is)
    {
        CompoundNBT tag = is.getTag();
        if(tag != null)
        {
            Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
            if(instrument != null)
            {
                //                return "item.clef.instrument." + instrument.info.itemName;
                return instrument.info.shortdescription;//we don't inject the localisation in anymore
            }
        }
        return super.getTranslationKey(is);
    }

    @Override
    public UseAction getUseAction(ItemStack stack)
    {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack is)
    {
        CompoundNBT tag = is.getTag();
        if(tag != null)
        {
            Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
            if(instrument != null)
            {
                return Math.round(instrument.tuning.fadeout * 20);
            }
        }
        return 0;
    }

    @Override
    public boolean isHeldLikeBow(@Nonnull ItemStack is, @Nonnull LivingEntity living)
    {
        ItemStack is1 = getUsableInstrument(living);
        if(is1.isEmpty())
        {
            return false;
        }
        if(living instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity)living;
            Track track = Clef.eventHandlerClient.getTrackPlayedByPlayer(player);
            if(track != null)
            {
                return track.timeToSilence > 0;
            }
        }
        return false;
    }

    public static @Nonnull ItemStack getUsableInstrument(LivingEntity entity)
    {
        ItemStack is = entity.getHeldItemMainhand();
        if(is.getItem() == Clef.Items.INSTRUMENT.get())
        {
            CompoundNBT tag = is.getTag();
            if(tag != null)
            {
                Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
                if(instrument != null && (!instrument.info.twoHanded || Clef.configServer.allowOneHandedTwoHandedInstrumentUse || entity.getHeldItemOffhand().isEmpty()))
                {
                    return is;
                }
            }
        }
        is = entity.getHeldItemOffhand();
        if(is.getItem() == Clef.Items.INSTRUMENT.get())
        {
            CompoundNBT tag = is.getTag();
            if(tag != null)
            {
                Instrument instrument = InstrumentLibrary.getInstrumentByName(tag.getString("itemName"));
                if(instrument != null && (!instrument.info.twoHanded || Clef.configServer.allowOneHandedTwoHandedInstrumentUse || entity.getHeldItemMainhand().isEmpty()))
                {
                    return is;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
