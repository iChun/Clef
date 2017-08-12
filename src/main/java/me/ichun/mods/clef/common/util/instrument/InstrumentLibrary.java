package me.ichun.mods.clef.common.util.instrument;

import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import me.ichun.mods.clef.common.Clef;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InstrumentLibrary
{
    public static ArrayList<Instrument> instruments = new ArrayList<>();

    public static void init()
    {
        Clef.LOGGER.info("Loading instruments");
        Clef.LOGGER.info("Loaded " + readInstruments(Clef.getResourceHelper().instrumentDir) + " instruments"); //TODO loaded instrument count?
    }

    private static int readInstruments(File dir)
    {
        int instrumentsCount = 0;
        for(File file : dir.listFiles())
        {
            if(file.isDirectory())
            {
                instrumentsCount += readInstruments(file);
            }
            else
            {
                instrumentsCount += readInstrumentPack(file);
            }
        }
        return instrumentsCount;
    }

    public static int readInstrumentPack(File file)
    {
        int instrumentCount = 0;
        if(file.exists() && (file.getName().endsWith(".cia") || file.getName().endsWith(".zip"))) //clef instrument archive, or SB mod in zip, but not in *.pak or *.modpak
        {
            //            String md5 = IOUtil.getMD5Checksum(file);
            //            //            if(!hasInstrument(md5))
            //            {
            //            }
            Clef.LOGGER.info("Reading file: " + file.getName());
            try
            {
                ZipFile zipFile = new ZipFile(file);
                Enumeration entriesIte = zipFile.entries();

                ArrayList<ZipEntry> entries = new ArrayList<>();
                while(entriesIte.hasMoreElements())
                {
                    ZipEntry entry = (ZipEntry)entriesIte.nextElement();
                    if(!entry.isDirectory() && !entry.getName().endsWith(".png"))
                    {
                        entries.add(entry);
                    }
                }
                ArrayList<InstrumentInfo> instrumentInfos = new ArrayList<>();
                for(int i = entries.size() - 1; i >= 0; i--)
                {
                    ZipEntry entry = entries.get(i);
                    if(entry.getName().endsWith(".instrument"))
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
                    ZipEntry icon = zipFile.getEntry("items/instruments/" + info.inventoryIcon);
                    ZipEntry hand = zipFile.getEntry("items/instruments/" + info.activeImage);
                    ZipEntry tuning = zipFile.getEntry("sfx/instruments/" + info.kind + "/tuning.config");
                    if(icon == null || hand == null || tuning == null)
                    {
                        Clef.LOGGER.warn("Error loading instrument " + info.itemName + " from pack " + file.getName());
                        continue;
                    }
                    try(InputStream iconStm = zipFile.getInputStream(icon); InputStream handStm = zipFile.getInputStream(hand); InputStream tuningStm = zipFile.getInputStream(tuning);)
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
                            if(e.getValue() != null)
                            {
                                String[] files = e.getValue();
                                streams = new InputStream[files.length];
                                for(int i = 0; i < files.length; i++)
                                {
                                    String s = files[i];
                                    String[] fileNameSplit = s.split("/");
                                    String fileName = fileNameSplit[fileNameSplit.length - 1]; //blah.ogg
                                    ZipEntry sound = zipFile.getEntry("sfx/instruments/" + info.kind + "/" + fileName); //TODO check if the files are OGG, reject if not.

                                    if(!fileName.endsWith(".ogg"))
                                    {
                                        Clef.LOGGER.warn("Error loading instrument " + info.itemName + " from pack " + file.getName() + ". Audio files are not .ogg");
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

                                    streams[i] = new ByteArrayInputStream(baos.toByteArray());
                                    tuning1.audioToOutputStream.put(fileName, baos);
                                }
                            }
                            if(streams != null)
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

                        instrument.tuning = tuning1;

                        instruments.add(instrument);
                    }
                    catch(Exception e)
                    {
                        Clef.LOGGER.warn("Error loading instrument " + info.itemName + " from pack " + file.getName());
                        e.printStackTrace();
                        continue;
                    }
                }
                instrumentCount += instruments.size();
            }
            catch(Exception e)
            {
                Clef.LOGGER.warn("Error loading instrument pack: " + file.getName());
                e.printStackTrace();
            }
        }
        return instrumentCount;
    }
}
