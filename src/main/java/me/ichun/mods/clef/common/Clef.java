package me.ichun.mods.clef.common;

import me.ichun.mods.clef.client.core.EventHandlerClient;
import me.ichun.mods.clef.common.core.Config;
import me.ichun.mods.clef.common.core.EventHandlerServer;
import me.ichun.mods.clef.common.core.ProxyCommon;
import me.ichun.mods.clef.common.util.ResourceHelper;
import me.ichun.mods.ichunutil.common.core.Logger;
import me.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.update.UpdateChecker;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import java.io.File;

@Mod(modid = Clef.MOD_ID, name = Clef.MOD_NAME,
        version = Clef.VERSION,
        guiFactory = "me.ichun.mods.ichunutil.common.core.config.GenericModGuiFactory",
        dependencies = "required-after:ichunutil@[" + iChunUtil.VERSION_MAJOR +".4.0," + (iChunUtil.VERSION_MAJOR + 1) + ".0.0)",
        acceptableRemoteVersions = "[" + iChunUtil.VERSION_MAJOR +".0.0," + iChunUtil.VERSION_MAJOR + ".1.0)"
)
public class Clef
{
    public static final String MOD_NAME = "Clef";
    public static final String MOD_ID = "clef";
    public static final String VERSION = iChunUtil.VERSION_MAJOR + ".0.0";

    public static final Logger LOGGER = Logger.createLogger(MOD_NAME);

    public static PacketChannel channel;

    public static Config config;

    @Mod.Instance(MOD_ID)
    public static Clef instance;

    @SidedProxy(clientSide = "me.ichun.mods.clef.client.core.ProxyClient", serverSide = "me.ichun.mods.clef.common.core.ProxyCommon")
    public static ProxyCommon proxy;

    public static EventHandlerServer eventHandlerServer;
    public static EventHandlerClient eventHandlerClient;

    private static ResourceHelper resourceHelper;

    public static CreativeTabs creativeTabInstruments;
    public static Item itemInstrument;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        resourceHelper = new ResourceHelper(new File(event.getModConfigurationDirectory().getParent(), "/mods/clef"));

        config = ConfigHandler.registerConfig(new Config(event.getSuggestedConfigurationFile()));

        proxy.preInitMod();

        UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo(MOD_NAME, iChunUtil.VERSION_OF_MC, VERSION, false));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.initMod();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInitMod();
    }

    @Mod.EventHandler
    public void onServerStoppingDown(FMLServerStoppingEvent event)
    {
        eventHandlerServer.shutdownServer();
    }

    public static ResourceHelper getResourceHelper()
    {
        return resourceHelper;
    }
}
