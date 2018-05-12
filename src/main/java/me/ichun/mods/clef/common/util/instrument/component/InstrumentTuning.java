package me.ichun.mods.clef.common.util.instrument.component;

import com.google.gson.annotations.SerializedName;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class InstrumentTuning
{
    public float fadeout;
    public Map<String, TuningInt> mapping;

    public transient HashMap<Integer, TuningInfo> keyToTuningMap = new HashMap<>();
    public transient HashMap<String, byte[]> audioToOutputStream = new HashMap<>(); //this is so we can re-archive the sound and send the files

    public InstrumentTuning(){}

    public static class TuningInfo
    {
        private final List<Function<Void, InputStream>> streams;
        public final int keyOffset;

        public TuningInfo(int keyOffset, List<Function<Void, InputStream>> streams)
        {
            this.streams = streams;
            this.keyOffset = keyOffset;
        }

        public InputStream get(int i) {
            return streams.get(i).apply(null);
        }

        public int streamsLength() {
            return streams.size();
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
