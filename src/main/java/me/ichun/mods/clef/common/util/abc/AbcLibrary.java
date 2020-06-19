package me.ichun.mods.clef.common.util.abc;

import me.ichun.mods.clef.client.gui.GuiPlayTrack;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.packet.PacketFileFragment;
import me.ichun.mods.clef.common.packet.PacketPlayingTracks;
import me.ichun.mods.clef.common.packet.PacketRequestFile;
import me.ichun.mods.clef.common.util.ResourceHelper;
import me.ichun.mods.clef.common.util.abc.play.NotePlayThread;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AbcLibrary
{
    public static ArrayList<TrackFile> tracks = new ArrayList<>();

    public static void init()
    {
        tracks.clear();
        File defaultAbc = new File(ResourceHelper.getAbcDir().toFile(), "files.extracted");
        if(!defaultAbc.exists())
        {
            try(InputStream in = Clef.class.getResourceAsStream("/abc.zip"); ZipInputStream zipStream = new ZipInputStream(in))
            {
                ZipEntry entry;
                int extractCount = 0;

                while((entry = zipStream.getNextEntry()) != null)
                {
                    File file = new File(ResourceHelper.getAbcDir().toFile(), entry.getName());
                    if(file.exists() && file.length() > 3L)
                    {
                        continue;
                    }
                    FileOutputStream out = new FileOutputStream(file);

                    byte[] buffer = new byte[8192];
                    int len;
                    while((len = zipStream.read(buffer)) != -1)
                    {
                        out.write(buffer, 0, len);
                    }
                    out.close();

                    extractCount++;
                }

                if(extractCount > 0)
                {
                    Clef.LOGGER.info("Extracted " + Integer.toString(extractCount) + (extractCount == 1 ? " file" : " files" + " from mod zip."));
                }
            }
            catch(IOException ignored){}
        }
        Clef.LOGGER.info("Loading abc files");
        Clef.LOGGER.info("Loaded " + readAbcs(ResourceHelper.getAbcDir().toFile(), tracks) + " abc files");
        if(tracks.isEmpty())
        {
            TrackInfo track = new TrackInfo();
            track.setTitle("You have no tracks");
            track.setFileTitle("You have no tracks");
            track.trackLength = 10;
            tracks.add(new TrackFile(track, new File(ResourceHelper.getAbcDir().toFile(), "You have no tracks.abc"), ""));
        }
    }

    private static int readAbcs(File dir, ArrayList<TrackFile> tracks)
    {
        int trackCount = 0;
        for(File file : dir.listFiles())
        {
            if(file.isDirectory())
            {
                trackCount += readAbcs(file, tracks);
            }
            else if(readAbc(file, tracks))
            {
                trackCount++;
            }
        }
        return trackCount;
    }

    public static boolean readAbc(File file, ArrayList<TrackFile> tracks)
    {
        if(file.exists() && file.getName().endsWith(".abc"))
        {
            String md5 = IOUtil.getMD5Checksum(file);
            if(!(tracks == AbcLibrary.tracks && hasTrack(md5)))
            {
                TrackInfo track = AbcParser.parse(file);
                if(track != null)
                {
                    tracks.add(new TrackFile(track, file, md5));
                    Collections.sort(tracks);

                    if(EffectiveSide.get().isServer() && tracksWaitingForTrackInfo.containsKey(md5))
                    {
                        tracksWaitingForTrackInfo.get(md5).setTrack(md5, track);
                        tracksWaitingForTrackInfo.remove(md5);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static void reloadTracks(GuiPlayTrack gui)
    {
        boolean result = NotePlayThread.INSTANCE.acquireLock();
        try
        {
            ArrayList<TrackFile> tracks = new ArrayList<>();
            Clef.LOGGER.info("Reloading abc files");
            Clef.LOGGER.info("Reloaded " + readAbcs(ResourceHelper.getAbcDir().toFile(), tracks) + " abc files");
            if (tracks.isEmpty())
            {
                TrackInfo track = new TrackInfo();
                track.setTitle("You have no tracks");
                track.setFileTitle("You have no tracks");
                track.trackLength = 10;
                tracks.add(new TrackFile(track, new File(ResourceHelper.getAbcDir().toFile(), "You have no tracks.abc"), ""));
            }
            AbcLibrary.tracks = tracks;

            gui.tracks = tracks;
            gui.index = -1;
            gui.doneTimeout = 20;
            gui.init(Minecraft.getInstance(), Minecraft.getInstance().getMainWindow().getScaledWidth(), Minecraft.getInstance().getMainWindow().getScaledHeight());
        }
        finally
        {
            NotePlayThread.INSTANCE.releaseLock(result);
        }
    }

    public static boolean hasTrack(String md5)
    {
        return getTrack(md5) != null;
    }

    public static TrackFile getTrack(String md5)
    {
        for(TrackFile track : tracks)
        {
            if(track.md5.equals(md5))
            {
                return track;
            }
        }
        return null;
    }

    public static void playAbc(String md5, String bandName, boolean syncPlay, boolean syncTrack, ServerPlayerEntity player) //CLIENT NEVER CALLS THIS.
    {
        TrackFile file = getTrack(md5);
        Track track;
        if(!bandName.isEmpty())
        {
            //Find the band
            track = Clef.eventHandlerServer.findTrackByBand(bandName);
            if(track == null && md5.isEmpty())
            {
                return;
            }
            Track track1 = track;
            if(track == null || !syncTrack) //No band
            {
                track = new Track(RandomStringUtils.randomAscii(IOUtil.IDENTIFIER_LENGTH), bandName, md5, (file != null ? file.track : null), false);
            }
            if(syncPlay && track1 != null)
            {
                track.playAtProgress(track1.playProg);
            }
        }
        else if(md5.isEmpty())
        {
            return;
        }
        else
        {
            track = new Track(RandomStringUtils.randomAscii(IOUtil.IDENTIFIER_LENGTH), bandName, md5, (file != null ? file.track : null), false);
        }

        if(file == null) //We don't have the ABC. Ask from the client.
        {
            if(requestedABCFromPlayers.add(md5))
            {
                if(track.getTrack() == null)
                {
                    tracksWaitingForTrackInfo.put(md5, track);
                }
                Clef.channel.sendTo(new PacketRequestFile(md5, false), player);
            }
        }
        Track playerTrack = Clef.eventHandlerServer.getTrackPlayedByPlayer(player);
        if(playerTrack != null)
        {
            playerTrack.players.remove(player);
            if(!playerTrack.hasObjectsPlaying())
            {
                playerTrack.stop();
            }
            Clef.channel.sendTo(new PacketPlayingTracks(playerTrack), PacketDistributor.ALL.noArg());
        }

        //queue the track. track plays when the trackfile is set.
        Clef.eventHandlerServer.tracksPlaying.add(track);
        track.players.put(player, 0);
        if(track.getTrack() != null)
        {
            Clef.channel.sendTo(new PacketPlayingTracks(track), PacketDistributor.ALL.noArg());
        }
    }

    public static void startPlayingTrack(ServerPlayerEntity player, Track...tracks)
    {
        Clef.channel.sendTo(new PacketPlayingTracks(tracks), player);
    }

    public static void sendAbc(String md5, ServerPlayerEntity player)
    {
        if(md5.isEmpty())
        {
            return;
        }
        TrackFile track = getTrack(md5);
        if(track != null)
        {
            if(!track.file.exists())
            {
                Clef.LOGGER.warn("Unable to send track " + track.file.getName() + ". File is no longer on disk.");
                return;
            }
            try(FileInputStream fis = new FileInputStream(track.file))
            {
                int fileSize = (int)track.file.length();

                if(fileSize > 10000000) // what tracks are >10 mb holy shaite
                {
                    Clef.LOGGER.warn("Unable to send track " + track.file.getName() + ". It is above the size limit!");
                    return;
                }
                else if(fileSize == 0)
                {
                    Clef.LOGGER.warn("Unable to send track " + track.file.getName() + ". The file is empty!");
                    return;
                }

                Clef.LOGGER.info("Sending track " + track.file.getName() + " to " + (player == null ? "the server" : player.getName().getUnformattedComponentText()));

                int packetsToSend = (int)Math.ceil((float)fileSize / 32000F);
                int packetCount = 0;

                while(fileSize > 0)
                {
                    byte[] fileBytes = new byte[Math.min(fileSize, 32000)];
                    fis.read(fileBytes);

                    if(player != null)
                    {
                        Clef.channel.sendTo(new PacketFileFragment(track.file.getName(), packetsToSend, packetCount, fileBytes), player);
                    }
                    else
                    {
                        Clef.channel.sendToServer(new PacketFileFragment(track.file.getName(), packetsToSend, packetCount, fileBytes));
                    }

                    packetCount++;
                    fileSize -= 32000;
                }
            }
            catch(IOException e){}
        }
        //Do nothing if we don't have it, unlike instruments.
    }

    public static HashSet<String> requestedABCFromServer = new HashSet<>(); // The server should ALWAYS have all the abc files, unlike the client.
    public static HashSet<String> requestedABCFromPlayers = new HashSet<>();
    public static HashMap<String, Track> tracksWaitingForTrackInfo = new HashMap<>();

    public static void handleReceivedFile(String fileName, byte[] fileData, LogicalSide side)
    {
        File dir = new File(ResourceHelper.getAbcDir().toFile(), "received");
        File file = new File(dir, fileName);
        try
        {
            dir.mkdirs();
            if(file.exists())
            {
                file.delete();
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileData);
            fos.close();

            Clef.LOGGER.info("Received " + fileName + ". Reading.");
            readAbc(file, tracks);

            String fileNameTruncated = fileName.substring(0, fileName.length() - 4);
            if(side.isServer())
            {
                requestedABCFromPlayers.remove(fileNameTruncated);
            }
            else
            {
                requestedABCFromServer.remove(fileNameTruncated);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
