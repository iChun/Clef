package me.ichun.mods.clef.common.util;

import me.ichun.mods.clef.common.Clef;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceHelper
{
    private static boolean init = false;
    private static boolean allGood = false;
    private static Path workingDir;
    private static Path abcDir;
    private static Path instrumentDir;

    private static void init()
    {
        if(init)
        {
            return;
        }

        workingDir = FMLPaths.MODSDIR.get().resolve(Clef.MOD_ID);
        if(!Files.exists(workingDir))
        {
            try
            {
                Files.createDirectory(workingDir);
            }
            catch(IOException e)
            {
                Clef.LOGGER.fatal("Error creating working directory!");
                e.printStackTrace();
                return;
            }
        }

        abcDir = workingDir.resolve("abc");
        if(!Files.exists(abcDir))
        {
            try
            {
                Files.createDirectory(abcDir);
            }
            catch(IOException e)
            {
                Clef.LOGGER.warn("Error creating abc directory!");
                e.printStackTrace();
                return;
            }
        }

        instrumentDir = workingDir.resolve("instruments");
        if(!Files.exists(instrumentDir))
        {
            try
            {
                Files.createDirectory(instrumentDir);
            }
            catch(IOException e)
            {
                Clef.LOGGER.warn("Error creating instrument directory!");
                e.printStackTrace();
                return;
            }
        }

        init = allGood = true;
    }

    public static boolean allGood()
    {
        if(!init)
        {
            init();
        }
        return allGood;
    }

    public static Path getWorkingDir()
    {
        return workingDir;
    }

    public static Path getAbcDir()
    {
        return abcDir;
    }

    public static Path getInstrumentDir()
    {
        return instrumentDir;
    }
}
