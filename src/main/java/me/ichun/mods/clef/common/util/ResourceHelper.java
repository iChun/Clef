package me.ichun.mods.clef.common.util;

import me.ichun.mods.clef.common.Clef;

import java.io.File;

public class ResourceHelper
{
    public final File workingDir;
    public final File abcDir;
    public final File instrumentDir;

    public ResourceHelper(File file)
    {
        workingDir = file;
        if(!workingDir.exists() && !workingDir.mkdirs())
        {
            Clef.LOGGER.warn("Error creating working directory!");
        }

        abcDir = new File(workingDir, "/abc");
        if(!abcDir.exists() && !abcDir.mkdirs())
        {
            Clef.LOGGER.warn("Error creating abc directory!");
        }

        instrumentDir = new File(workingDir, "/instruments");
        if(!instrumentDir.exists() && !instrumentDir.mkdirs())
        {
            Clef.LOGGER.warn("Error creating instrument directory!");
        }
    }
}
