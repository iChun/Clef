package me.ichun.mods.clef.common.util.abc;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;
import me.ichun.mods.ichunutil.common.core.util.IOUtil;

import java.io.File;
import java.util.HashMap;

public class AbcLibrary
{
    public static HashMap<String, TrackInfo> tracks = new HashMap<>();

    public static void init()
    {
        readAbcs(Clef.getResourceHelper().abcDir);
    }

    private static void readAbcs(File dir)
    {
        Clef.LOGGER.info("Loading abc files");
        int trackCount = 0;
        for(File file : dir.listFiles())
        {
            if(file.isDirectory())
            {
                readAbcs(file);
            }
            else if(file.getName().endsWith(".abc"))
            {
                String md5 = IOUtil.getMD5Checksum(file);
                if(!tracks.containsKey(md5))
                {
                    TrackInfo track = AbcParser.parse(file);
                    if(track != null)
                    {
                        tracks.put(md5, track);
                        trackCount++;
                    }
                }
            }
        }
        Clef.LOGGER.info("Loaded " + trackCount + "abc files");
    }
}
