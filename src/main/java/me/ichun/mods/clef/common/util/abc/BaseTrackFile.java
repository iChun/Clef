package me.ichun.mods.clef.common.util.abc;

public abstract class BaseTrackFile {
    public final String md5;

    protected BaseTrackFile(String md5) {
        this.md5 = md5;
    }

    public abstract boolean isSynced();

    public abstract String getTitle();
}
