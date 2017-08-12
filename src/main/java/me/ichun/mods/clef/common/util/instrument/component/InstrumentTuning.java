package me.ichun.mods.clef.common.util.instrument.component;

import com.google.gson.annotations.SerializedName;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class InstrumentTuning
{
    public float fadeout;
    public Map<String, TuningInt> mapping;

    public transient HashMap<Integer, TuningInfo> keyToTuningMap = new HashMap<>();
    public transient HashMap<String, ByteArrayOutputStream> audioToOutputStream = new HashMap<>(); //this is so we can re-archive the sound and send the files

    public InstrumentTuning(){}

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

    public class TuningInt
    {
        @SerializedName("f")
        public float frequency;
        public String file;
        public String files[];
    }
}
