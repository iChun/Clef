package me.ichun.mods.clef.common.util.abc;

import me.ichun.mods.clef.common.Clef;

import java.io.File;

public class AbcLibrary
{
    public static void init()
    {
        readAbcs(Clef.getResourceHelper().abcDir);
    }

    private static void readAbcs(File dir)
    {
        Clef.LOGGER.info("BEGGININGNASDLKAJS LKJLAKJS");
        for(File file : dir.listFiles())
        {
            if(file.isDirectory())
            {
                readAbcs(file);
            }
            else if(file.getName().endsWith(".abc"))
            {
                AbcParser.parse(file);
            }
        }
    }
}
