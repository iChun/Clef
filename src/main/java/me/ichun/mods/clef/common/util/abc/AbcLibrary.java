package me.ichun.mods.clef.common.util.abc;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;
import me.ichun.mods.ichunutil.common.core.util.IOUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class AbcLibrary
{
    public static ArrayList<TrackFile> tracks = new ArrayList<>();

    public static void init()
    {
        tracks.clear();
        Clef.LOGGER.info("Loading abc files");
        Clef.LOGGER.info("Loaded " + readAbcs(Clef.getResourceHelper().abcDir) + " abc files");
    }

    private static int readAbcs(File dir)
    {
        int trackCount = 0;
        for(File file : dir.listFiles())
        {
            if(file.isDirectory())
            {
                trackCount += readAbcs(file);
            }
            else if(readAbc(file))
            {
                trackCount++;
            }
        }
        return trackCount;
    }

    public static boolean readAbc(File file)
    {
        if(file.exists() && file.getName().endsWith(".abc"))
        {
            String md5 = IOUtil.getMD5Checksum(file);
//            if(!hasTrack(md5))
            {
                TrackInfo track = AbcParser.parse(file);
                if(track != null)
                {
                    tracks.add(new TrackFile(track, file, md5));
                    Collections.sort(tracks);
                    return true;
                }
            }
        }
        return false;
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
}
