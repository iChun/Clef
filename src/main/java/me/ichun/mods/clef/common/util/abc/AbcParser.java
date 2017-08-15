package me.ichun.mods.clef.common.util.abc;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.construct.*;
import me.ichun.mods.clef.common.util.abc.construct.Number;
import me.ichun.mods.clef.common.util.abc.construct.special.Key;
import me.ichun.mods.clef.common.util.abc.construct.special.Meter;
import me.ichun.mods.clef.common.util.abc.construct.special.Tempo;
import me.ichun.mods.clef.common.util.abc.construct.special.UnitNoteLength;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class AbcParser
{
    //The order of abc constructs for a note is: <grace notes>, <chord symbols>, <annotations>/<decorations> (e.g. Irish roll, staccato marker or up/downbow), <accidentals>, <note>, <octave>, <note length>, i.e. ~^c'3 or even "Gm7"v.=G,2.
    public static final char[] graceNotes = new char[] { '{', '}' };
    public static final char[] chords = new char[] { '[', ']' };
    public static final char[] accidentals = new char[] { '^', '=', '_' };

    public static final char[] notes = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'z', 'x', 'Z', 'X' };
    public static final char[] octaves = new char[] { ',', '\'' };

    public static final char[] numbers = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    public static final char operator = '/';
    public static final char space = ' ';

    public static final char[] barLines = new char[] { '|', ':' };

    public static final char[] ignored = new char[] { '-', '(', ')', '~', 'H', 'L', 'M', 'O', 'P', 'S', 'T', 'u', 'v', '\\' }; //ties = 3, decorations = 10, continue on next line = 1

    //TODO there are some strings that we need to ignore. Some start with % and some start with others. Look them up.
    public static String[] ignoredStarts = new String[] { "%", "[r:", "O:", "V:" };//TODO ignore remarks in ABC where when looking for chords

    public static TrackInfo parse(File file)
    {
        //TODO log if we don't know what we're reading.
        Clef.LOGGER.info("Parsing - " + file.getName());
        try(FileInputStream stream = new FileInputStream(file))
        {
            AbcObject abc = new AbcObject();

            boolean readKeys = false;
            boolean unknownLine = false; //TODO use this.
            List<String> lines = IOUtils.readLines(stream);
            for(String lineA : lines)
            {
                boolean handledLine = false;
                String line = lineA.trim();
                if(line.startsWith("L:"))
                {
                    //note length. Can be anywhere before key.
                    String[] s = line.substring(2).split("/");
                    if(s.length == 1)
                    {
                        abc.constructs.add(new UnitNoteLength(Integer.parseInt(s[0].trim())));
                    }
                    else if(s.length == 2)
                    {
                        abc.constructs.add(new UnitNoteLength(Integer.parseInt(s[0].trim()) / (double)Integer.parseInt(s[1].trim())));
                    }
                    handledLine = true;
                }
                else if(line.startsWith("Q:"))
                {
                    abc.constructs.add(new Tempo(line.substring(2).trim()));
                    handledLine = true;
                }
                else if(line.startsWith("M:"))
                {
                    String[] s = line.substring(2).split("/");
                    if(s.length == 1)
                    {
                        abc.constructs.add(new Meter(Integer.parseInt(s[0].trim())));
                    }
                    else if(s.length == 2)
                    {
                        abc.constructs.add(new Meter(Integer.parseInt(s[0].trim()) / (double)Integer.parseInt(s[1].trim())));
                    }
                    handledLine = true;
                }
                else if(line.startsWith("K:"))
                {
                    handledLine = true;
                    abc.constructs.add(new Key(line.substring(2).trim()));
                    readKeys = true;
                }
                else
                {
                    for(String ig : ignoredStarts)
                    {
                        if(line.startsWith(ig))
                        {
                            handledLine = true;
                            break;
                        }
                    }
                }
                if(!handledLine)
                {
                    if(!readKeys)
                    {
                        if(line.startsWith("%abc"))
                        {
                            //version. Ignore for now
                            continue;
                        }
                        else if(line.startsWith("X:"))
                        {
                            if(abc.referenceNumber == -1)
                            {
                                abc.referenceNumber = Integer.parseInt(line.substring(2).trim());
                            }
                            else
                            {
                                Clef.LOGGER.info("More than one reference number? - " + file.getName()); //TODO possibly more than 1 tune in a single ABC?
                            }
                        }
                        else if(line.startsWith("T:"))
                        {
                            abc.title = line.substring(2).trim();
                        }
                        else if(line.startsWith("C:"))
                        {
                            abc.composer = line.substring(2).trim();
                        }
                        else if(line.startsWith("Z:"))
                        {
                            abc.transcriber = line.substring(2).trim();
                        }
                        else if(!line.isEmpty() && !line.startsWith("%") && !line.startsWith("[r:"))
                        {
                            unknownLine = true;
                            Clef.LOGGER.info("Unknown abc line - " + lineA);
                            break;
                        }
                    }
                    else
                    {
                        //The order of abc constructs for a note is: <grace notes>, <chord symbols>, <annotations>/<decorations> (e.g. Irish roll, staccato marker or up/downbow), <accidentals>, <note>, <octave>, <note length>, i.e. ~^c'3 or even "Gm7"v.=G,2.
                        for(int i = 0; i < line.length(); i++)
                        {
                            char key = line.charAt(i);
                            boolean handled = false;
                            for(char c : graceNotes)
                            {
                                if(c == key)
                                {
                                    abc.constructs.add(new Grace());
                                    handled = true;
                                    break;
                                }
                            }

                            if(!handled)
                            {
                                for(char c : chords)
                                {
                                    if(c == key)
                                    {
                                        handled = true;
                                        if(key == '[' && i < line.length() - 1 && line.charAt(i + 1) == '|' || key == ']' && i > 0 && line.charAt(i - 1) == '|')
                                        {
                                            abc.constructs.add(new BarLine());
                                            handled = true;
                                            break;
                                        }
                                        if(key == '[' && i < line.length() - 1 && line.charAt(i + 1) == 'r')//remark
                                        {
                                            while(i < line.length() && line.charAt(++i) != ']'){} //intended
                                        }
                                        //TODO fields in within [];
                                        abc.constructs.add(new Chord());
                                        break;
                                    }
                                }
                            }

                            if(!handled)
                            {
                                for(char c : barLines) //TODO repeat sections?
                                {
                                    if(c == key)
                                    {
                                        abc.constructs.add(new BarLine());
                                        handled = true;
                                        break;
                                    }
                                }
                            }

                            if(!handled)
                            {
                                for(char c : accidentals)
                                {
                                    if(c == key)
                                    {
                                        abc.constructs.add(new Accidental(c));
                                        handled = true;
                                        break;
                                    }
                                }
                            }

                            if(!handled)
                            {
                                for(char c : notes)
                                {
                                    if(c == key)
                                    {
                                        abc.constructs.add(new Note(c));
                                        handled = true;
                                        break;
                                    }
                                }
                            }

                            if(!handled)
                            {
                                for(char c : octaves)
                                {
                                    if(c == key)
                                    {
                                        abc.constructs.add(new Octave(c));
                                        handled = true;
                                        break;
                                    }
                                }
                            }

                            if(!handled)
                            {
                                for(char c : numbers)
                                {
                                    if(c == key)
                                    {
                                        abc.constructs.add(new Number(Integer.parseInt(Character.toString(c))));
                                        handled = true;
                                        break;
                                    }
                                }
                            }

                            if(!handled)
                            {
                                if(key == '!' && i < line.length() - 1)//named decoration
                                {
                                    while(i < line.length() && line.charAt(++i) != '!'){} //intended
                                }

                                if(operator == key)
                                {
                                    abc.constructs.add(new Operator());
                                    handled = true;
                                }
                                if(space == key)
                                {
                                    abc.constructs.add(new Space());
                                    handled = true;
                                }
                            }

                            if(!handled)
                            {
                                for(char c : ignored)
                                {
                                    if(c == key)
                                    {
                                        handled = true;
                                        break;
                                    }
                                }
                                if(!handled)
                                {
                                    Clef.LOGGER.info("Uh oh at line: " + lineA);
                                    Clef.LOGGER.info("We don't know how to handle this: " + Character.toString(key));
                                }
                            }
                        }
                        abc.constructs.add(new Space());
                    }
                }
            }
            //Finish reading the file
            return TrackInfo.buildTrack(abc, file.getName().substring(0, file.getName().length() - 4));
        }
        catch(IOException | NumberFormatException e)
        {

        }
        return null;
    }
}
