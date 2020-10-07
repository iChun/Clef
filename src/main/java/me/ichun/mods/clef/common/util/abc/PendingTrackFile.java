package me.ichun.mods.clef.common.util.abc;

public class PendingTrackFile extends BaseTrackFile
{
    private int tries = 0;
    private final String title;

    public PendingTrackFile(String md5)
    {
        this(md5, null);
    }

    public PendingTrackFile(String md5, String title)
    {
        super(md5);
        this.title = title;
    }

    @Override
    public boolean isSynced()
    {
        return false;
    }

    @Override
    public String getTitle()
    {
        if (title != null)
        {
            return title;
        }
        else
        {
            return "{" + md5 + "}";
        }
    }

    public TrackFile resolve()
    {
        TrackFile file = AbcLibrary.getTrack(md5);
        if (file != null)
            return file;
        tries++;
        return null;
    }

    public int getResolveTries()
    {
        return tries;
    }
}
