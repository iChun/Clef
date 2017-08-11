package me.ichun.mods.clef.common.util.instrument;

import java.io.File;

public class InstrumentPack
{
    public final File file;
    public final String md5;

    public InstrumentPack(File file, String md5)
    {
        this.file = file;
        this.md5 = md5;
    }
}
