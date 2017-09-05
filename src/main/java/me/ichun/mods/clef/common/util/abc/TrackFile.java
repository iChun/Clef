package me.ichun.mods.clef.common.util.abc;

import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;

import java.io.File;

public class TrackFile
    implements Comparable<TrackFile>
{
    public final TrackInfo track;
    public final File file;
    public final String md5;

    public TrackFile(TrackInfo track, File file, String md5)
    {
        this.track = track;
        this.file = file;
        this.md5 = md5;
    }

    @Override
    public int compareTo(TrackFile o)
    {
        if(file.getParentFile().getAbsolutePath().equals(o.file.getParentFile().getAbsolutePath()))
        {
            return track.getTitle().toLowerCase().compareTo(o.track.getTitle().toLowerCase());
        }
        return file.getParentFile().getAbsolutePath().toLowerCase().compareTo(o.file.getParentFile().getAbsolutePath().toLowerCase());
    }
}
