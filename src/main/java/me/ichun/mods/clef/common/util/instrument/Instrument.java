package me.ichun.mods.clef.common.util.instrument;

import java.awt.image.BufferedImage;
import java.io.InputStream;

public class Instrument
{
    public final InstrumentInfo info;
    public final BufferedImage iconImg;
    public int iconImgId = -1;
    public final BufferedImage handImg;
    public int handImgId = -1;
    public InstrumentTuning tuning;

    public Instrument(InstrumentInfo info, BufferedImage iconImg, BufferedImage handImg)
    {
        this.info = info;
        this.iconImg = iconImg;
        this.handImg = handImg;
    }

    public boolean hasAvailableKey(int key)
    {
        return tuning.keyToTuningMap.containsKey(key);
    }
}
