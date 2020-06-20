package me.ichun.mods.clef.common.util.instrument.component;

import com.google.gson.annotations.SerializedName;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstrumentTuning
{
    public float fadeout;
    public Map<String, TuningInt> mapping;

    public transient ConcurrentHashMap<Integer, TuningInfo> keyToTuningMap = new ConcurrentHashMap<>();
    public transient HashMap<String, byte[]> audioToOutputStream = new HashMap<>(); //this is so we can re-archive the sound and send the files

    public InstrumentTuning(){}

    public static class TuningInfo
    {
        private final List<byte[]> streams;
        public final int keyOffset;

        public TuningInfo(int keyOffset, List<byte[]> streams)
        {
            this.streams = streams;
            this.keyOffset = keyOffset;
        }

        public InputStream get(int i) {
            return new ByteArrayInputStream(streams.get(i));
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
