package me.ichun.mods.clef.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.ichun.mods.clef.client.core.EventHandlerClient;
import me.ichun.mods.clef.client.gui.GuiPlayTrackBlock;
import me.ichun.mods.clef.client.render.BakedModelInstrument;
import me.ichun.mods.clef.common.block.BlockInstrumentPlayer;
import me.ichun.mods.clef.client.config.ConfigClient;
import me.ichun.mods.clef.common.config.ConfigCommon;
import me.ichun.mods.clef.common.config.ConfigServer;
import me.ichun.mods.clef.common.core.EventHandlerServer;
import me.ichun.mods.clef.common.inventory.ContainerInstrumentPlayer;
import me.ichun.mods.clef.common.item.ItemInstrument;
import me.ichun.mods.clef.common.packet.*;
import me.ichun.mods.clef.common.thread.ThreadReadFiles;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.clef.common.util.ResourceHelper;
import me.ichun.mods.ichunutil.common.network.PacketChannel;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

@Mod(Clef.MOD_ID)
public class Clef
{
    public static final String MOD_NAME = "Clef";
    public static final String MOD_ID = "clef";
    public static final String PROTOCOL = "1";

    public static final Logger LOGGER = LogManager.getLogger();

    public static ConfigCommon configCommon;
    public static ConfigClient configClient;
    public static ConfigServer configServer;

    public static EventHandlerServer eventHandlerServer;
    public static EventHandlerClient eventHandlerClient;

    public static PacketChannel channel;

    private static ThreadReadFiles threadReadFiles;

    public Clef()
    {
        if(!ResourceHelper.allGood())
        {
            return;
        }
        configCommon = new ConfigCommon().init();
        configServer = new ConfigServer().init();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Blocks.REGISTRY.register(bus);
        Items.REGISTRY.register(bus);
        ContainerTypes.REGISTRY.register(bus);
        TileEntityTypes.REGISTRY.register(bus);

        bus.addListener(this::finishLoading);

        MinecraftForge.EVENT_BUS.register(eventHandlerServer = new EventHandlerServer());

        Clef.channel = new PacketChannel(new ResourceLocation(MOD_ID, "channel"), PROTOCOL, PacketRequestFile.class, PacketFileFragment.class, PacketPlayABC.class, PacketPlayingTracks.class, PacketStopPlayingTrack.class, PacketInstrumentPlayerInfo.class, PacketCreateInstrument.class);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            configClient = new ConfigClient().init();

            bus.addListener(this::onClientSetup);
            bus.addListener(this::onModelBake);

            MinecraftForge.EVENT_BUS.register(eventHandlerClient = new EventHandlerClient());
        });

        threadReadFiles  = new ThreadReadFiles();
        threadReadFiles.start();
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        ScreenManager.registerFactory(ContainerTypes.INSTRUMENT_PLAYER.get(), (ScreenManager.IScreenFactory<ContainerInstrumentPlayer, GuiPlayTrackBlock>)((container, playerInventory, name) -> new GuiPlayTrackBlock(container)));

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> me.ichun.mods.ichunutil.client.core.EventHandlerClient::getConfigGui);
    }

    @OnlyIn(Dist.CLIENT)
    private void onModelBake(ModelBakeEvent event)
    {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
        TextureAtlasSprite tas = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(new ResourceLocation("clef", "items/instrument"));
        builder.addAll(ItemLayerModel.getQuadsForSprite(0, tas, TransformationMatrix.identity()));
        event.getModelRegistry().put(new ModelResourceLocation("clef:instrument", "inventory"), new BakedModelInstrument(builder.build(), tas, ImmutableMap.copyOf(new HashMap<>()), null, null));
    }

    private void finishLoading(FMLLoadCompleteEvent event)
    {
        if(threadReadFiles.latch.getCount() > 0)
        {
            Clef.LOGGER.info("Waiting for file reader thread to finish");
            try
            {
                threadReadFiles.latch.await();
            }
            catch(InterruptedException e)
            {
                Clef.LOGGER.error("Got interrupted while waiting for FileReaderThread to finish");
                e.printStackTrace();
            }
        }
        threadReadFiles = null; //enjoy this thread, GC.
    }

    public static class Blocks
    {
        private static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

        public static final RegistryObject<BlockInstrumentPlayer> INSTRUMENT_PLAYER = REGISTRY.register("instrument_player", BlockInstrumentPlayer::new);
    }

    public static class Items
    {
        private static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

        public static final ItemGroup GROUP_INSTRUMENTS = new ItemGroup("clef") {
            @Override
            public ItemStack createIcon()
            {
                return new ItemStack(INSTRUMENT.get());
            }
        };

        public static final RegistryObject<ItemInstrument> INSTRUMENT = REGISTRY.register("instrument", () -> new ItemInstrument(new Item.Properties().maxDamage(0).group(GROUP_INSTRUMENTS)));

        public static final RegistryObject<BlockItem> INSTRUMENT_PLAYER = REGISTRY.register("instrument_player", () -> new BlockItem(Blocks.INSTRUMENT_PLAYER.get(), (new Item.Properties()).group(GROUP_INSTRUMENTS)));
    }

    public static class ContainerTypes
    {
        private static final DeferredRegister<ContainerType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);

        public static final RegistryObject<ContainerType<ContainerInstrumentPlayer>> INSTRUMENT_PLAYER = REGISTRY.register("instrument_player", () -> IForgeContainerType.create(ContainerInstrumentPlayer::new));
    }

    public static class TileEntityTypes
    {
        private static final DeferredRegister<TileEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);

        public static final RegistryObject<TileEntityType<TileEntityInstrumentPlayer>> INSTRUMENT_PLAYER = REGISTRY.register("instrument_player", () -> TileEntityType.Builder.create(TileEntityInstrumentPlayer::new, Blocks.INSTRUMENT_PLAYER.get()).build(null));
    }
}
