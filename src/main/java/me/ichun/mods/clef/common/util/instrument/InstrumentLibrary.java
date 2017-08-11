package me.ichun.mods.clef.common.util.instrument;

import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import me.ichun.mods.clef.common.Clef;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
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
        if(file.exists() && file.getName().endsWith(".cia")) //clef instrument archive
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

                        InstrumentTuningSB tuning1 = (new Gson()).fromJson(jsonString, InstrumentTuningSB.class);

                        TreeMap<Integer, String[]> tuningInfo = new TreeMap<>(Ordering.natural());
                        for(Map.Entry<String, InstrumentTuningSB.TuningInt> e : tuning1.mapping.entrySet())
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

                        InstrumentTuning tuning2 = new InstrumentTuning(tuning1.fadeout);

                        int offset = 0;
                        InputStream[] streams = null;
                        for(Map.Entry<Integer, String[]> e : tuningInfo.entrySet())
                        {
                            if(e.getValue() != null)
                            {
                                offset = e.getKey();
                                String[] files = e.getValue();
                                streams = new InputStream[files.length];
                                for(int i = 0; i < files.length; i++)
                                {
                                    String s = files[i];
                                    String[] fileNameSplit = s.split("/");
                                    String fileName = fileNameSplit[fileNameSplit.length - 1]; //blah.ogg
                                    ZipEntry sound = zipFile.getEntry("sfx/instruments/" + info.kind + "/" + fileName);
                                    streams[i] = zipFile.getInputStream(sound);
                                }
                            }
                            if(streams != null)
                            {
                                tuning2.keyToTuningMap.put(e.getKey(), new InstrumentTuning.TuningInfo(streams, e.getKey() - offset));
                            }
                        }

                        instrument.tuning = tuning2;

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
