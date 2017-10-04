package me.ichun.mods.clef.common.core;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.block.BlockInstrumentPlayer;
import me.ichun.mods.clef.common.item.ItemInstrument;
import me.ichun.mods.clef.common.packet.PacketPlayingTracks;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import me.ichun.mods.ichunutil.common.core.util.IOUtil;
import me.ichun.mods.ichunutil.common.iChunUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class EventHandlerServer
{
    public HashSet<Track> tracksPlaying = new HashSet<>();

    @SubscribeEvent
    public void onRegisterBlock(RegistryEvent.Register<Block> event)
    {
        Clef.blockInstrumentPlayer = new BlockInstrumentPlayer().setRegistryName("clef", "block_instrument_player").setUnlocalizedName("clef.item.instrumentPlayer");

        event.getRegistry().register(Clef.blockInstrumentPlayer);
    }

    @SubscribeEvent
    public void onRegisterItem(RegistryEvent.Register<Item> event)
    {
        Clef.itemInstrument = (new ItemInstrument()).setFull3D().setRegistryName("clef", "instrument").setUnlocalizedName("clef.item.instrument");
        event.getRegistry().register(Clef.itemInstrument);

        Clef.creativeTabInstruments = new CreativeTabs("clef") {
            public final ItemStack iconItem = new ItemStack(Clef.itemInstrument);

            @Override
            public ItemStack getTabIconItem()
            {
                return iconItem;
            }
        };
        Clef.itemInstrument.setCreativeTab(Clef.creativeTabInstruments);

        event.getRegistry().register(new ItemBlock(Clef.blockInstrumentPlayer).setRegistryName(Clef.blockInstrumentPlayer.getRegistryName()));
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.side == Side.SERVER && event.phase == TickEvent.Phase.END)
        {
            if(iChunUtil.eventHandlerServer.ticks + 5 % 10 == 2)
            {
                ItemStack isMain = event.player.getHeldItemMainhand();
                ItemStack isOff = event.player.getHeldItemOffhand();
                if(isMain.getItem() == Clef.itemInstrument)
                {
                    InstrumentLibrary.checkForInstrument(isMain, event.player);
                }
                if(isOff.getItem() == Clef.itemInstrument)
                {
                    InstrumentLibrary.checkForInstrument(isOff, event.player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemDrop(PlayerDropsEvent event)
    {
        if(!event.getEntityPlayer().getEntityWorld().isRemote)
        {
            for(EntityItem item : event.getDrops())
            {
                if(item.getItem().getItem() == Clef.itemInstrument)
                {
                    NBTTagCompound tag = item.getItem().getTagCompound();
                    if(tag != null)
                    {
                        String instName = tag.getString("itemName");
                        Instrument is = InstrumentLibrary.getInstrumentByName(instName);
                        if(is == null) //request the item then?
                        {
                            InstrumentLibrary.requestInstrument(instName, event.getEntityPlayer());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDropsEvent event)
    {
        if(!event.getEntityLiving().getEntityWorld().isRemote && (Clef.config.onlyHostileMobSpawn == 0 || event.getEntityLiving() instanceof IMob) && event.getEntityLiving().getRNG().nextFloat() < (Clef.config.mobDropRate / 10000F) * (event.getLootingLevel() + 1))
        {
            ItemStack stack = new ItemStack(Clef.itemInstrument, 1, 0);
            InstrumentLibrary.assignRandomInstrument(stack);
            event.getDrops().add(event.getEntityLiving().entityDropItem(stack, 0F));
        }
    }

    @SubscribeEvent
    public void onLivingSpawn(LivingSpawnEvent event)
    {
        if(!event.getEntityLiving().getEntityWorld().isRemote && event.getEntityLiving() instanceof EntityZombie && event.getEntityLiving().getRNG().nextFloat() < (Clef.config.zombieSpawnRate / 10000F))
        {
            EntityZombie zombie = (EntityZombie)event.getEntityLiving();
            if(zombie.getHeldItemMainhand().isEmpty())
            {
                ItemStack stack = new ItemStack(Clef.itemInstrument, 1, 0);
                InstrumentLibrary.assignRandomInstrument(stack);
                zombie.setHeldItem(EnumHand.MAIN_HAND, stack);
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event)
    {
        if(Clef.config.zombiesCanUseInstruments == 1 && !event.getEntityLiving().world.isRemote && event.getEntityLiving() instanceof EntityZombie)
        {
            EntityZombie zombie = (EntityZombie)event.getEntityLiving();
            if(zombie.getRNG().nextFloat() < 0.004F &&ItemInstrument.getUsableInstrument(zombie) != null && getTrackPlayedByPlayer(zombie) == null)
            {
                Track track = Clef.eventHandlerServer.findTrackByBand("zombies");
                if(track != null)
                {
                    if(track.zombies.add(zombie.getEntityId()))
                    {
                        Clef.channel.sendToAll(new PacketPlayingTracks(track));
                    }
                }
                else
                {
                    TrackFile randTrack = AbcLibrary.tracks.get(zombie.getRNG().nextInt(AbcLibrary.tracks.size()));
                    track = new Track(RandomStringUtils.randomAscii(IOUtil.IDENTIFIER_LENGTH), "zombies", randTrack.md5, randTrack.track, false);
                    if(track.getTrack().trackLength > 0)
                    {
                        track.playAtProgress(zombie.getRNG().nextInt(track.getTrack().trackLength));
                    }
                    Clef.eventHandlerServer.tracksPlaying.add(track);
                    track.zombies.add(zombie.getEntityId());
                    Clef.channel.sendToAll(new PacketPlayingTracks(track));
                }
            }
        }
    }

    @SubscribeEvent
    public void onLootTableEvent(LootTableLoadEvent event)
    {
        if(Clef.config.lootSpawnRate > 0)
        {
            for(String s : Clef.config.disabledLootChests)
            {
                if(event.getName().toString().equals(s))
                {
                    return;
                }
            }
            if(event.getName().getResourcePath().contains("chest"))
            {
                event.getTable().addPool(new LootPool(new LootEntry[] { new LootEntryItem(Clef.itemInstrument, Clef.config.lootSpawnRate, 0, new LootFunction[] { new LootFunction(new LootCondition[0])
                {
                    @Override
                    public ItemStack apply(ItemStack stack, Random rand, LootContext context)
                    {
                        InstrumentLibrary.assignRandomInstrument(stack);
                        return stack;
                    }
                } }, new LootCondition[0], "clef_instrument_pool") }, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0), "clef_instrument"));
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Iterator<Track> ite = tracksPlaying.iterator();
            while(ite.hasNext())
            {
                Track track = ite.next();
                if(!track.update())
                {
                    ite.remove();
                    continue;
                }
            }
        }
    }

    public void stopPlayingTrack(EntityPlayer player, String trackId)
    {
        for(Track track : tracksPlaying)
        {
            if(track.getId().equals(trackId))
            {
                track.players.remove(player);
                if(!track.hasObjectsPlaying())
                {
                    track.stop();
                }
                Clef.channel.sendToAll(new PacketPlayingTracks(track));
                break;
            }
        }
    }

    public Track getTrackPlayedByPlayer(EntityZombie zombie)
    {
        for(Track track : tracksPlaying)
        {
            if(track.zombies.contains(zombie.getEntityId()))
            {
                return track;
            }
        }
        return null;
    }

    public Track getTrackPlayedByPlayer(TileEntityInstrumentPlayer player)
    {
        for(Track track : tracksPlaying)
        {
            if(track.instrumentPlayers.containsKey(player.getWorld().provider.getDimension()) && track.instrumentPlayers.get(player.getWorld().provider.getDimension()).contains(player.getPos()))
            {
                return track;
            }
        }
        return null;
    }

    public Track getTrackPlayedByPlayer(EntityPlayer player)
    {
        for(Track track : tracksPlaying)
        {
            if(track.players.containsKey(player))
            {
                return track;
            }
        }
        return null;
    }

    public Track findTrackByBand(String bandName)
    {
        for(Track track : tracksPlaying)
        {
            if(track.getBandName().equalsIgnoreCase(bandName))
            {
                return track;
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event)
    {
        HashSet<Track> tracks = new HashSet<>();
        for(Track track : tracksPlaying)
        {
            if(track.getTrack() != null) //this means the track is actively played
            {
                tracks.add(track);
            }
        }
        AbcLibrary.startPlayingTrack(event.player, tracks.toArray(new Track[tracks.size()]));
    }

    public void shutdownServer()
    {
        tracksPlaying.clear();

        AbcLibrary.tracksWaitingForTrackInfo.clear();
        AbcLibrary.requestedABCFromPlayers.clear();
        InstrumentLibrary.requestsFromPlayers.clear();
        InstrumentLibrary.requestedInstrumentsFromPlayers.clear();
    }
}
