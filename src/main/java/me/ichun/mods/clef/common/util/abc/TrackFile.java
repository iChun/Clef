package me.ichun.mods.clef.common.util.abc;

import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;

import java.io.File;

public class TrackFile extends BaseTrackFile
    implements Comparable<TrackFile>
{
    public final TrackInfo track;
    public final File file;

    public TrackFile(TrackInfo track, File file, String md5)
    {
        super(md5);
        this.track = track;
        this.file = file;
    }

    @Override
    public boolean isSynced() {
        return true;
    }

    @Override
    public String getTitle() {
        return this.track.getTitle();
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
