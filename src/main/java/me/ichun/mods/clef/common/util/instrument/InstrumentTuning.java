package me.ichun.mods.clef.common.util.instrument;

import java.io.InputStream;
import java.util.HashMap;

public class InstrumentTuning
{
    public final float fadeout;
    public final HashMap<Integer, TuningInfo> keyToTuningMap = new HashMap<>();

    public InstrumentTuning(float fadeout)
    {
        this.fadeout = fadeout;
    }

    public static class TuningInfo
    {
        public final InputStream[] stream;
        public final int keyOffset;

        public TuningInfo(InputStream[] stream, int keyOffset)
        {
            this.stream = stream;
            this.keyOffset = keyOffset;
        }
    }
}
