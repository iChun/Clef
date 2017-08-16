package me.ichun.mods.clef.common.util.instrument;

import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import me.ichun.mods.clef.client.gui.GuiPlayTrack;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.packet.PacketFileFragment;
import me.ichun.mods.clef.common.packet.PacketRequestFile;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentInfo;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentModPackInfo;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentPackInfo;
import me.ichun.mods.clef.common.util.instrument.component.InstrumentTuning;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InstrumentLibrary
{
    public static ArrayList<Instrument> instruments = new ArrayList<>();

    public static void init()
    {
        instruments.clear();
        File defaultPack = new File(Clef.getResourceHelper().instrumentDir, "starbound.cia");
        if(!defaultPack.exists())
        {
            try(InputStream in = Clef.class.getResourceAsStream("/starbound.cia"))
            {
                FileOutputStream file = new FileOutputStream(defaultPack);
                IOUtils.copy(in, file);
                file.close();
            }
            catch(IOException ignored){}
        }
        Clef.LOGGER.info("Loading instruments");
        Clef.LOGGER.info("Loaded " + readInstruments(Clef.getResourceHelper().instrumentDir, instruments) + " instruments");
    }

    private static int readInstruments(File dir, ArrayList<Instrument> instruments)
    {
        int instrumentsCount = 0;
        for(File file : dir.listFiles())
        {
            if(file.isDirectory())
            {
                instrumentsCount += readInstruments(file, instruments);
            }
            else
            {
                instrumentsCount += readInstrumentPack(file, instruments);
            }
        }
        return instrumentsCount;
    }

    public static int readInstrumentPack(File file, ArrayList<Instrument> instruments)
    {
        int instrumentCount = 0;
        if(file.exists() && file.getName().endsWith(".pak"))
        {
            Clef.LOGGER.warn("We can't read Starbound Mod Packs in .pak format! File: " + file.getName());
            return 0;
        }
        if(file.exists() && (file.getName().endsWith(".cia") || file.getName().endsWith(".zip"))) //clef instrument archive, or SB mod in zip, but not in *.pak or *.modpak
        {
            Clef.LOGGER.info("Reading file: " + file.getName());
            try
            {
                ZipFile zipFile = new ZipFile(file);
                Enumeration entriesIte = zipFile.entries();

                InstrumentPackInfo packInfo = new InstrumentPackInfo();
                ZipEntry packInfoZip = zipFile.getEntry("info.cii");
                if(packInfoZip != null)
                {
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(zipFile.getInputStream(packInfoZip), writer);
                    String jsonString = writer.toString();
                    packInfo = (new Gson()).fromJson(jsonString, InstrumentPackInfo.class);
                }

                ArrayList<ZipEntry> entries = new ArrayList<>();
                while(entriesIte.hasMoreElements())
                {
                    ZipEntry entry = (ZipEntry)entriesIte.nextElement();
                    if(entry.getName().endsWith(".modinfo"))
                    {
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(zipFile.getInputStream(entry), writer);
                        String jsonString = writer.toString();
                        packInfo = InstrumentPackInfo.fromModInfo((new Gson()).fromJson(jsonString, InstrumentModPackInfo.class));
                        continue;
                    }
                    if(!entry.isDirectory() && !entry.getName().endsWith(".png"))
                    {
                        entries.add(entry);
                    }
                }
                ArrayList<InstrumentInfo> instrumentInfos = new ArrayList<>();
                for(int i = entries.size() - 1; i >= 0; i--)
                {
                    ZipEntry entry = entries.get(i);
                    if(entry.getName().startsWith("items/instruments/") && entry.getName().endsWith(".instrument"))
                    {
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(zipFile.getInputStream(entry), writer);
                        String jsonString = writer.toString();
                        instrumentInfos.add((new Gson()).fromJson(jsonString, InstrumentInfo.class));
                        entries.remove(i);
                    }
                }
                for(InstrumentInfo info : instrumentInfos)
                {
                    if(isInstrumentDisabled(info.itemName))
                    {
                        Clef.LOGGER.warn("Not loading instrument " + info.itemName + " from pack " + file.getName() + ". Instrument is disabled.");
                        continue;
                    }
                    ZipEntry icon = zipFile.getEntry("items/instruments/" + info.inventoryIcon);
                    ZipEntry hand = zipFile.getEntry("items/instruments/" + info.activeImage);
                    ZipEntry tuning = zipFile.getEntry("sfx/instruments/" + info.kind + "/tuning.config");
                    if(icon == null || hand == null || tuning == null)
                    {
                        Clef.LOGGER.warn("Error loading instrument " + info.itemName + " from pack " + file.getName());
                        continue;
                    }
                    try(InputStream iconStm = zipFile.getInputStream(icon); InputStream handStm = zipFile.getInputStream(hand); InputStream tuningStm = zipFile.getInputStream(tuning))
                    {
                        Instrument instrument = new Instrument(info, ImageIO.read(iconStm), ImageIO.read(handStm));

                        StringWriter writer = new StringWriter();
                        IOUtils.copy(tuningStm, writer);
                        String jsonString = writer.toString();

                        InstrumentTuning tuning1 = (new Gson()).fromJson(jsonString, InstrumentTuning.class);

                        TreeMap<Integer, String[]> tuningInfo = new TreeMap<>(Ordering.natural());
                        for(Map.Entry<String, InstrumentTuning.TuningInt> e : tuning1.mapping.entrySet())
                        {
                            String[] files = null;
                            if(e.getValue().files != null)
                            {
                                files = e.getValue().files;
                            }
                            else if(e.getValue().file != null)
                            {
                                files = new String[] { e.getValue().file };
                            }
                            tuningInfo.put(Integer.parseInt(e.getKey()), files);
                        }

                        for(Map.Entry<Integer, String[]> e : tuningInfo.entrySet())
                        {
                            InputStream[] streams = null;
                            boolean mute = false;
                            if(e.getValue() != null)
                            {
                                String[] files = e.getValue();
                                ArrayList<InputStream> streamList = new ArrayList<>();
                                for(int i = 0; i < files.length; i++)
                                {
                                    String s = files[i];
                                    String[] fileNameSplit = s.split("/");
                                    String fileName = fileNameSplit[fileNameSplit.length - 1]; //blah.ogg
                                    ZipEntry sound = zipFile.getEntry("sfx/instruments/" + info.kind + "/" + fileName);

                                    if(fileName.contains("mute"))
                                    {
                                        mute = true;
                                    }

                                    if(!fileName.endsWith(".ogg"))
                                    {
                                        Clef.LOGGER.warn("Error loading instrument " + info.itemName + " from pack " + file.getName() + ". Audio files are not .ogg");
                                        continue;
                                    }

                                    if(sound == null)
                                    {
                                        Clef.LOGGER.warn("Error loading instrument " + info.itemName + " from pack " + file.getName() + ". Audio file " + fileName + " does not exist. Skipping file.");
                                        continue;
                                    }

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();//copy to an output stream so we can copy it as an input stream.
                                    byte[] buffer = new byte[1024];
                                    int len;
                                    InputStream input = zipFile.getInputStream(sound);
                                    while ((len = input.read(buffer)) > -1 ) {
                                        baos.write(buffer, 0, len);
                                    }
                                    baos.flush();

                                    streamList.add(new ByteArrayInputStream(baos.toByteArray()));
                                    tuning1.audioToOutputStream.put(fileName, baos);
                                }
                                if(!streamList.isEmpty())
                                {
                                    streams = streamList.toArray(new InputStream[streamList.size()]);
                                }
                            }
                            if(streams != null)
                            {
                                if(mute)
                                {
                                    tuning1.keyToTuningMap.put(e.getKey(), new InstrumentTuning.TuningInfo(new InputStream[0], 0));
                                }
                                else
                                {
                                    for(int i = -6; i <= 6; i++)
                                    {
                                        if(!tuning1.keyToTuningMap.containsKey(e.getKey() + i))
                                        {
                                            tuning1.keyToTuningMap.put(e.getKey() + i, new InstrumentTuning.TuningInfo(streams, i));
                                        }
                                    }
                                }
                            }
                        }

                        instrument.tuning = tuning1;
                        instrument.packInfo = packInfo;

                        instruments.add(instrument);
                        instrumentCount++;
                    }
                    catch(Exception e)
                    {
                        Clef.LOGGER.warn("Error loading instrument " + info.itemName + " from pack " + file.getName());
                        e.printStackTrace();
                        continue;
                    }
                }
                Collections.sort(instruments);
            }
            catch(Exception e)
            {
                Clef.LOGGER.warn("Error loading instrument pack: " + file.getName());
                e.printStackTrace();
            }
        }
        return instrumentCount;
    }

    @SideOnly(Side.CLIENT)
    public static void reloadInstruments(GuiPlayTrack gui)
    {
        ArrayList<Instrument> instruments = new ArrayList<>();
        Clef.LOGGER.info("Reloading instruments");
        Clef.LOGGER.info("Reloaded " + readInstruments(Clef.getResourceHelper().instrumentDir, instruments) + " instruments");
        for(Instrument instrument : InstrumentLibrary.instruments) //delete the textures to free up memory.
        {
            if(instrument.iconModel != null)
            {
                Minecraft.getMinecraft().getTextureManager().deleteTexture(instrument.iconModel.instTx);
            }
            if(instrument.handModel != null)
            {
                Minecraft.getMinecraft().getTextureManager().deleteTexture(instrument.handModel.instTx);
            }
        }
        InstrumentLibrary.instruments = instruments;
        gui.doneTimeout = 20;
    }

    public static void injectLocalization(Instrument instrument)
    {
        String localName = "item.clef.instrument." + instrument.info.itemName + ".name=" + instrument.info.shortdescription;
        String localDesc = "item.clef.instrument." + instrument.info.itemName + ".desc=" + instrument.info.description;
        InputStream streamName = new ByteArrayInputStream(localName.getBytes(StandardCharsets.UTF_8));
        InputStream streamDesc = new ByteArrayInputStream(localDesc.getBytes(StandardCharsets.UTF_8));
        LanguageMap.inject(streamName);
        LanguageMap.inject(streamDesc);
    }

    public static Instrument getInstrumentByName(String s)
    {
        for(Instrument instrument : instruments)
        {
            if(instrument.info.itemName.equalsIgnoreCase(s))
            {
                return instrument;
            }
        }
        return null;
    }

    public static HashMap<String, HashSet<String>> requestsFromPlayers = new HashMap<>();
    public static HashSet<String> requestedInstrumentsFromServer = new HashSet<>();
    public static HashSet<String> requestedInstrumentsFromPlayers = new HashSet<>();

    public static void checkForInstrument(ItemStack is, EntityPlayer player) //Primarily called by server. Can be called by client though
    {
        NBTTagCompound tag = is.getTagCompound();
        if(tag != null)
        {
            String instName = tag.getString("itemName");
            Instrument inst = getInstrumentByName(instName);
            if(inst == null)
            {
                requestInstrument(instName, player);
            }
        }
        else
        {
            assignRandomInstrument(is);
        }
    }

    public static void assignRandomInstrument(ItemStack is)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("itemName", instruments.get((int)Math.floor(Math.random() * instruments.size())).info.itemName);
        is.setTagCompound(tag);
    }

    public static void requestInstrument(String name, EntityPlayer player)
    {
        if(player == null)
        {
            if(requestedInstrumentsFromServer.add(name))
            {
                Clef.channel.sendToServer(new PacketRequestFile(name, true));
            }
        }
        else
        {
            if(requestedInstrumentsFromPlayers.add(name))
            {
                Clef.channel.sendTo(new PacketRequestFile(name, true), player);
            }
        }
    }

    public static boolean isInstrumentDisabled(String name)
    {
        for(String s : Clef.config.disabledInstruments)
        {
            if(s.equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }

    public static void packageAndSendInstrument(String name, EntityPlayer player) //side is the side receiving this request
    {
        if(name.isEmpty())
        {
            return;
        }
        Instrument instrument = getInstrumentByName(name);
        if(instrument != null)
        {
            //Archive and send the instrument
            ByteArrayOutputStream baos = instrument.getAsBAOS();
            if(baos != null)
            {
                byte[] file = baos.toByteArray();

                if(file.length > 10000000) // what instruments are >10 mb holy shaite
                {
                    Clef.LOGGER.warn("Unable to send instrument " + instrument.info.itemName + ". It is above the size limit!");
                    return;
                }
                else if(file.length == 0)
                {
                    Clef.LOGGER.warn("Unable to send instrument " + instrument.info.itemName + ". The file is empty!");
                    return;
                }

                Clef.LOGGER.info("Sending instrument " + instrument.info.itemName + " to " + (player == null ? "the server" : player.getName()));

                int fileSize = file.length;
                int packetsToSend = (int)Math.ceil((float)fileSize / 32000F);
                int packetCount = 0;
                int index = 0;

                while(fileSize > 0)
                {
                    byte[] fileBytes = new byte[fileSize > 32000 ? 32000 : fileSize];
                    System.arraycopy(file, index, fileBytes, 0, fileBytes.length);
                    index += fileBytes.length;

                    if(player != null)
                    {
                        Clef.channel.sendTo(new PacketFileFragment(instrument.info.itemName + ".cia", packetsToSend, packetCount, fileSize > 32000 ? 32000 : fileSize, fileBytes), player);
                    }
                    else
                    {
                        Clef.channel.sendToServer(new PacketFileFragment(instrument.info.itemName + ".cia", packetsToSend, packetCount, fileSize > 32000 ? 32000 : fileSize, fileBytes));
                    }

                    packetCount++;
                    fileSize -= 32000;
                }
            }
        }
        else if(player != null)
        {
            //Ask players for it if server
            HashSet<String> players = requestsFromPlayers.computeIfAbsent(name, v -> new HashSet<>());
            players.add(player.getName());
            if(requestedInstrumentsFromPlayers.add(name))
            {
                Clef.channel.sendToAllExcept(new PacketRequestFile(name, true), player);
            }
        }
        //Do nothing if client
    }

    public static void handleReceivedFile(String fileName, byte[] fileData, Side side)
    {
        File dir = new File(Clef.getResourceHelper().instrumentDir, "received");
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
            readInstrumentPack(file, instruments);

            if(!isInstrumentDisabled(fileName.substring(0, fileName.length() - 4)))
            {
                if(side.isServer())
                {
                    String instName = fileName.substring(0, fileName.length() - 4);
                    requestedInstrumentsFromPlayers.remove(instName);
                    HashSet<String> playersRequesting = requestsFromPlayers.get(instName);
                    if(playersRequesting != null)
                    {
                        for(String s : playersRequesting)
                        {
                            EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(s);
                            if(player != null)
                            {
                                packageAndSendInstrument(instName, player);
                            }
                        }
                    }
                }
                else
                {
                    requestedInstrumentsFromServer.remove(fileName.substring(0, fileName.length() - 4));
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
