package me.ichun.mods.clef.common.util.instrument;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class InstrumentTuningSB
{
    public float fadeout;
    public Map<String, TuningInt> mapping;

    public class TuningInt
    {
        @SerializedName("f")
        public float frequency;
        public String file;
        public String files[];
    }
}
