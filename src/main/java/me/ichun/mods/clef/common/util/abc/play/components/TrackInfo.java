package me.ichun.mods.clef.common.util.abc.play.components;

import com.google.common.collect.Ordering;
import me.ichun.mods.clef.common.Clef;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.HashSet;
import java.util.TreeMap;

public class TrackInfo
{
    public int referenceNumber = -1; //X
    private String title = ""; //T
    private String fileTitle = "";
    public String composer = ""; //C
    public String transcriber = ""; //Z

    public TreeMap<Integer, HashSet<Note>[]> notes = new TreeMap<>(Ordering.natural());
    public int trackLength = 0;

    public TrackInfo()
    {
    }

    public void setTitle(String s)
    {
        title = s;
    }

    public void setFileTitle(String s)
    {
        fileTitle = s;
    }

    public String getTitle()
    {
        return FMLEnvironment.dist.isDedicatedServer() ? fileTitle : Clef.configClient.showFileTitle ? fileTitle : title;
    }
}
