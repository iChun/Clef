package me.ichun.mods.clef.common.util.instrument.component;

public class InstrumentPackInfo
{
    public String packName;
    public String description;
    public String creator;

    public InstrumentPackInfo()
    {
        packName = "Not Defined";
        description = "This pack does not have a clef instrument pack info.";
        creator = "Unknown";
    }

    public static InstrumentPackInfo fromModInfo(InstrumentModPackInfo infoMod)
    {
        InstrumentPackInfo info = new InstrumentPackInfo();
        info.packName = infoMod.name + " - " + infoMod.version;
        info.description = infoMod.metadata.description;
        info.creator = infoMod.metadata.author;
        return info;
    }
}
