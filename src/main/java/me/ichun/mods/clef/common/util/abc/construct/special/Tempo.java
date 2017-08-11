package me.ichun.mods.clef.common.util.abc.construct.special;

import me.ichun.mods.clef.common.util.abc.construct.Construct;

public class Tempo extends Construct
{
    public int bpm = 60;
    public double splits = 0.125D;

    public Tempo()
    {
    }

    public Tempo(String s)
    {
        splits = 0D;
        String[] split = s.split(" ");
        for(String s1 : split)
        {
            if(s1.contains("\"") || s1.isEmpty()) //ignore text strings and empty strings
            {
                continue;
            }
            try
            {
                String[] info = s1.trim().split("=");
                if(info.length == 2) //has the bpm
                {
                    bpm = Integer.parseInt(info[1].trim());
                }
                if(info.length == 1)
                {
                    String[] splitss = info[0].trim().split("/");
                    if(splitss.length == 2)
                    {
                        splits += Double.parseDouble(splitss[0].trim()) / Double.parseDouble(splitss[1].trim());
                    }
                    else if(splitss.length == 1)
                    {
                        if(split.length == 1)
                        {
                            bpm = Integer.parseInt(splitss[0].trim());
                        }
                        else //This should ideally never happen?
                        {
                            splits += Double.parseDouble(splitss[0].trim());
                        }
                    }
                }
            }
            catch(NumberFormatException e)
            {
                e.printStackTrace();
            }
        }
        if(splits == 0D)
        {
            splits = 0.125D;
        }
    }

    @Override
    public EnumConstructType getType()
    {
        return EnumConstructType.SPECIAL;
    }
}
