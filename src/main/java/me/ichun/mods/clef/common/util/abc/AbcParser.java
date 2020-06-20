package me.ichun.mods.clef.common.util.abc;

import com.google.common.base.Splitter;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.util.abc.construct.Accidental;
import me.ichun.mods.clef.common.util.abc.construct.Octave;
import me.ichun.mods.clef.common.util.abc.construct.special.Key;
import me.ichun.mods.clef.common.util.abc.construct.special.Meter;
import me.ichun.mods.clef.common.util.abc.construct.special.Tempo;
import me.ichun.mods.clef.common.util.abc.construct.special.UnitNoteLength;
import me.ichun.mods.clef.common.util.abc.play.components.*;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AbcParser
{
    //This specifies how many "sub ticks" during one tick should be considered.
    public static final int SUB_TICKS = 5;

    //The order of abc constructs for a note is: <grace notes>, <chord symbols>, <annotations>/<decorations> (e.g. Irish roll, staccato marker or up/downbow), <accidentals>, <note>, <octave>, <note length>, i.e. ~^c'3 or even "Gm7"v.=G,2.
    public static final char[] accidentals = new char[] { '^', '=', '_' };
    public static final char[] notes = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'z', 'x', 'Z', 'X' };
    public static final char[] octaves = new char[] { ',', '\'' };
    public static final char[] numbers = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    public static final char[] ignored = new char[] { '-', '(', ')', '~', 'H', 'L', 'M', 'O', 'P', 'S', 'T', 'u', 'v' }; //ties = 3, decorations = 10
    public static final char[] brokenRhythm = new char[] { '<', '>' };

    public static final char[] endOfNoteChars = new char[] { ',', '\'', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '/', ']' };

    public static final String[] barLinePattern = new String[] { "::", ":\\|", "\\|:", "[\\[]\\|", "\\|\\|", "\\|[\\]]", "\\|", "\\|[\\[]", "\\| [\\[]" }; //last three are repeats
    public static final String[] ignoredInfoPattern = new String[] { "[{].*[}]", "[\"].*[\"]", "[!].*[!]", "[+].*[+]" };

    public static String[] ignoredStarts = new String[] { "%", "r:", "A:", "O:", "N:", "G:", "H:", "+:", "I:" };

    public static String[] rejectFiles = new String[] { "P:", "V:" };

    //TODO parse V: and multi-tune ABCs for another time.
    public static TrackInfo parse(File file)
    {
        try(FileInputStream stream = new FileInputStream(file))
        {
            TrackInfo abc = new TrackInfo();
            abc.setFileTitle(file.getName().substring(0, file.getName().length() - 4));
            ArrayList<Note> trackNotes = new ArrayList<>();

            boolean readKeys = false;
            List<String> lines = IOUtils.readLines(stream);
            for(int l = 0; l < lines.size(); l++)
            {
                String lineA = lines.get(l);

                //Discard comments first
                if(lineA.contains("%"))
                {
                    lineA = lineA.substring(0, lineA.indexOf("%"));
                }

                String line = lineA.trim();

                if(line.isEmpty())
                {
                    if(abc.referenceNumber != -1)
                    {
                        readKeys = true;
                    }
                    continue; //ignore empty lines
                }

                String lineLower = line.toLowerCase();
                boolean handledLine = readCommand(trackNotes, line);
                if(lineLower.startsWith("k:"))
                {
                    readKeys = true;
                }
                if(!handledLine)
                {
                    if(!readKeys)
                    {
                        if(lineLower.startsWith("x:"))
                        {
                            if(abc.referenceNumber == -1)
                            {
                                abc.referenceNumber = Integer.parseInt(line.substring(2).trim());
                            }
                            else
                            {
                                Clef.LOGGER.info("More than one reference number? - " + file.getName());
                            }
                        }
                        else if(lineLower.startsWith("t:"))
                        {
                            abc.setTitle(line.substring(2).trim());
                        }
                        else if(lineLower.startsWith("c:"))
                        {
                            abc.composer = line.substring(2).trim();
                        }
                        else if(lineLower.startsWith("z:"))
                        {
                            abc.transcriber = line.substring(2).trim();
                        }
                        else
                        {
                            Clef.LOGGER.info("Unknown abc line in " + file.getName() + " - " + lineA);
                            continue;
                        }
                    }
                    else
                    {
                        if(lineLower.startsWith("x:"))
                        {
                            Clef.LOGGER.warn("Clef doesn't support abc files with more than one tune yet, only reading the first tune: " + file.getName());
                            break;
                        }
                        //The order of abc constructs for a note is: <grace notes>, <chord symbols>, <annotations>/<decorations> (e.g. Irish roll, staccato marker or up/downbow), <accidentals>, <note>, <octave>, <note length>, i.e. ~^c'3 or even "Gm7"v.=G,2.
                        //WE should be reading the tune now.

                        if(line.endsWith("\\")) //ends with a "continues on next line"
                        {
                            if(l < lines.size() - 1) //add this line to the next line
                            {
                                String newLine = line.substring(0, line.length() - 1) + lines.get(l + 1);
                                lines.remove(l + 1);
                                lines.add(l + 1, newLine);
                                continue;
                            }
                            else
                            {
                                line = line.substring(0, line.length() - 1);
                            }
                        }

                        //Split the line by measures
                        ArrayList<String> partsBarLines = new ArrayList<>();
                        partsBarLines.add(line);
                        for(int j = 0; j < barLinePattern.length; j++)
                        {
                            for(int i = partsBarLines.size() - 1; i >= 0; i--)
                            {
                                String s = partsBarLines.get(i);
                                partsBarLines.remove(i);
                                partsBarLines.addAll(Splitter.onPattern(barLinePattern[j]).trimResults().splitToList(s));
                            }
                        }

                        //Wrap the parts with a bar line each.
                        for(int v = 0; v < partsBarLines.size(); v++)
                        {
                            String partPerBar = partsBarLines.get(v);
                            if(partPerBar.isEmpty())
                            {
                                trackNotes.add(new BarLine());
                                continue;
                            }
                            while(true) //remove repeated segments
                            {
                                try
                                {
                                    Integer.parseInt(partPerBar.substring(0, 1)); //Try to parse the first char as a number and trim if it is.
                                    partPerBar = partPerBar.substring(1);
                                }
                                catch(NumberFormatException e)
                                {
                                    break;
                                }
                            }

                            //split by graces, then chords, then notes.
                            ArrayList<String> partsGraces = new ArrayList<>();
                            partsGraces.add(partPerBar);
                            for(int j = 0; j < ignoredInfoPattern.length; j++)
                            {
                                for(int i = partsGraces.size() - 1; i >= 0; i--)
                                {
                                    String s = partsGraces.get(i);
                                    partsGraces.remove(i);
                                    partsGraces.addAll(Splitter.onPattern(ignoredInfoPattern[j]).omitEmptyStrings().trimResults().splitToList(s));
                                }
                            }

                            for(int k = 0; k < partsGraces.size(); k++) // Note has to be processed in this loop due to internal commands.
                            {
                                String partPerGrace = partsGraces.get(k);

                                if(partPerGrace.contains("[") && partPerGrace.indexOf("[") < partPerGrace.indexOf(":") && partPerGrace.indexOf("]") > partPerGrace.indexOf(":")) //Looking for commands
                                {
                                    partsGraces.remove(k);
                                    partsGraces.add(k, partPerGrace.substring(0, partPerGrace.indexOf("[")));
                                    partsGraces.add(k + 1, partPerGrace.substring(partPerGrace.indexOf("["), partPerGrace.indexOf("]") + 1));
                                    partsGraces.add(k + 2, partPerGrace.substring(partPerGrace.indexOf("]") + 1, partPerGrace.length()));
                                    k--;

                                    continue;
                                }

                                if(partPerGrace.contains(":")) //this is a command. It should be on it's own line.
                                {
                                    if(partPerGrace.startsWith("[") && partPerGrace.endsWith("]"))
                                    {
                                        readCommand(trackNotes, partPerGrace.substring(1, partPerGrace.length() - 1));
                                    }
                                    else
                                    {
                                        readCommand(trackNotes, partPerGrace);
                                    }
                                    continue;
                                }

                                ArrayList<String> partsNotes = new ArrayList<>();
                                partsNotes.addAll(Splitter.onPattern(" ").omitEmptyStrings().trimResults().splitToList(partPerGrace));
                                //From here on, all we have are chords and notes
                                Chord chord = null;
                                for(int o = 0; o < partsNotes.size(); o++) //end of notes only have octaves, note lengths and chord symbols.
                                {
                                    ArrayList<String> note = new ArrayList<>(); //this keeps each individual note.

                                    String partNote = partsNotes.get(o);
                                    int noteIndex = partNote.length();
                                    boolean foundStartOfNote = false;
                                    for(int x = partNote.length() - 1; x >= 0; x--)
                                    {
                                        char key = partNote.charAt(x);
                                        boolean startNote = true;
                                        for(char c : notes)
                                        {
                                            if(key == c) // WE FOUND THE NOTE.
                                            {
                                                if(foundStartOfNote) //THIS IS A NEW NOTE.
                                                {
                                                    note.add(0, partNote.substring(x + 1, noteIndex));
                                                    noteIndex = x + 1;
                                                    foundStartOfNote = false;
                                                }
                                            }
                                        }
                                        for(char c : endOfNoteChars)
                                        {
                                            if(key == c) // we're still in the tail of the note.
                                            {
                                                if(foundStartOfNote) //THIS IS A NEW NOTE.
                                                {
                                                    note.add(0, partNote.substring(x + 1, noteIndex));
                                                    noteIndex = x + 1;
                                                    foundStartOfNote = false;
                                                }
                                                startNote = false;
                                            }
                                        }
                                        if(startNote)
                                        {
                                            foundStartOfNote = true;
                                        }
                                        if(x == 0) //start of string
                                        {
                                            note.add(0, partNote.substring(x, noteIndex));
                                            foundStartOfNote = false;
                                        }
                                    }

                                    for(String singleNoteString : note)
                                    {
                                        //Only Note splices including chord starts and ends. No spaces.
                                        if(singleNoteString.startsWith("[") && singleNoteString.length() > 1 && singleNoteString.substring(1, 2).matches("\\d"))
                                        {
                                            //this is a repeat section.
                                            singleNoteString = singleNoteString.substring(2);
                                        }
                                        SingleNote singleNote = new SingleNote();
                                        boolean added = false;
                                        int brokenRhythmValue = 0;
                                        for(int r = 0; r < singleNoteString.length(); r++)
                                        {
                                            char key = singleNoteString.charAt(r);
                                            if(key == '[')//chord start
                                            {
                                                if(chord != null)
                                                {
                                                    Clef.LOGGER.warn("Uh oh, we found a malformed chord start in: " + file.getName());
                                                    Clef.LOGGER.warn("Line: " + line);
                                                }
                                                else
                                                {
                                                    chord = new Chord();
                                                }
                                            }
                                            else if(key == ']') //chord end
                                            {
                                                if(chord == null)
                                                {
                                                    Clef.LOGGER.warn("Uh oh, we found a malformed chord end in: " + file.getName());
                                                    Clef.LOGGER.warn("Line: " + line);
                                                }
                                                else
                                                {
                                                    //Find the duration of the chord;
                                                    r++;
                                                    int chordNum = -1;
                                                    int chordDom = -1;
                                                    boolean foundOperator = false;
                                                    while(r < singleNoteString.length())
                                                    {
                                                        char key1 = singleNoteString.charAt(r);
                                                        if(key1 == '/') //we found the operator.
                                                        {
                                                            foundOperator = true;
                                                            if(chordNum == -1) //we found the operator but the numerator wasn't set. Default it.
                                                            {
                                                                chordNum = 1;
                                                            }
                                                        }
                                                        else
                                                        {
                                                            boolean notANumber = true;
                                                            for(char c : numbers)
                                                            {
                                                                if(c == key) // we found a number.
                                                                {
                                                                    notANumber = false;
                                                                    if(foundOperator) //we're working on the denominator now
                                                                    {
                                                                        if(chordDom == -1)
                                                                        {
                                                                            chordDom = Integer.parseInt(Character.toString(c));
                                                                        }
                                                                        else
                                                                        {
                                                                            chordDom *= 10;
                                                                            chordDom += Integer.parseInt(Character.toString(c));
                                                                        }
                                                                    }
                                                                    else //we're working on the numerator now
                                                                    {
                                                                        if(chordNum == -1)
                                                                        {
                                                                            chordNum = Integer.parseInt(Character.toString(c));
                                                                        }
                                                                        else
                                                                        {
                                                                            chordNum *= 10;
                                                                            chordNum += Integer.parseInt(Character.toString(c));
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            if(notANumber)
                                                            {
                                                                Clef.LOGGER.warn("Uh oh, we found a problem looking for the chord duration in " + file.getName() + ". Found key: " + Character.toString(key));
                                                                Clef.LOGGER.warn("Line: " + line);
                                                            }
                                                        }
                                                        r++;
                                                    }
                                                    if(chordDom == -1)
                                                    {
                                                        if(foundOperator) // we found the operator but not the denominator. Default it.
                                                        {
                                                            chordDom = 2;
                                                        }
                                                        else //we didn't find an operator. number is a whole. Not a fraction.
                                                        {
                                                            chordDom = 1;
                                                        }
                                                    }
                                                    if(chordNum == -1) //we didn't find any numbers at all. set the numerator to 1
                                                    {
                                                        chordNum = 1;
                                                    }
                                                    chord.duration = chordNum / (float)chordDom;

                                                    //add the chord and reset
                                                    if(brokenRhythmValue != 0)
                                                    {
                                                        ArrayList<Note> trackNotes1 = chord.notes;
                                                        if(!trackNotes1.isEmpty() && trackNotes1.get(trackNotes1.size() - 1) instanceof SingleNote)
                                                        {
                                                            SingleNote referenceNote = (SingleNote)trackNotes1.get(trackNotes1.size() - 1);
                                                            if(brokenRhythmValue == 1)//give more to this and take from previous
                                                            {
                                                                singleNote.duration += referenceNote.duration / 2D;
                                                                referenceNote.duration /= 2D;
                                                            }
                                                            else
                                                            {
                                                                referenceNote.duration += singleNote.duration / 2D;
                                                                singleNote.duration /= 2D;
                                                            }
                                                        }
                                                    }
                                                    chord.notes.add(singleNote);
                                                    added = true;
                                                    trackNotes.add(chord);
                                                    chord = null;
                                                }
                                            }
                                            else //we are reading the actual note.
                                            {
                                                boolean handled = false;
                                                for(char c : ignored)
                                                {
                                                    if(key == c)
                                                    {
                                                        handled = true;
                                                        break;
                                                    }
                                                }
                                                for(char c : accidentals)
                                                {
                                                    if(key == c)
                                                    {
                                                        handled = true;
                                                        singleNote.constructs.add(new Accidental(key));
                                                        break;
                                                    }
                                                }
                                                for(char c : notes)
                                                {
                                                    if(key == c)
                                                    {
                                                        handled = true;
                                                        singleNote.constructs.add(new me.ichun.mods.clef.common.util.abc.construct.Note(key));
                                                        break;
                                                    }
                                                }
                                                for(char c : octaves)
                                                {
                                                    if(key == c)
                                                    {
                                                        handled = true;
                                                        singleNote.constructs.add(new Octave(key));
                                                        break;
                                                    }
                                                }
                                                for(char c : brokenRhythm)
                                                {
                                                    if(key == c)
                                                    {
                                                        handled = true;
                                                        brokenRhythmValue = key == '<' ? 1 : -1;
                                                        break;
                                                    }
                                                }
                                                if(!handled) //we've already found everything else. Presume this is a number or operator.
                                                {
                                                    //Find the duration of the note;
                                                    int noteNum = -1;
                                                    int noteDom = -1;
                                                    boolean foundOperator = false;
                                                    while(r < singleNoteString.length())
                                                    {
                                                        char key1 = singleNoteString.charAt(r);
                                                        if(key1 == '/') //we found the operator.
                                                        {
                                                            foundOperator = true;
                                                            if(noteNum == -1) //we found the operator but the numerator wasn't set. Default it.
                                                            {
                                                                noteNum = 1;
                                                            }
                                                        }
                                                        else if(key1 == ']')//we found the chord end.
                                                        {
                                                            if(chord == null)
                                                            {
                                                                Clef.LOGGER.warn("Uh oh, we found a malformed chord end in: " + file.getName());
                                                                Clef.LOGGER.warn("Line: " + line);
                                                            }
                                                            else
                                                            {
                                                                //Find the duration of the chord;
                                                                r++;
                                                                int chordNum = -1;
                                                                int chordDom = -1;
                                                                boolean foundOperator2 = false;
                                                                while(r < singleNoteString.length())
                                                                {
                                                                    char key2 = singleNoteString.charAt(r);
                                                                    if(key2 == '/') //we found the operator.
                                                                    {
                                                                        foundOperator2 = true;
                                                                        if(chordNum == -1) //we found the operator but the numerator wasn't set. Default it.
                                                                        {
                                                                            chordNum = 1;
                                                                        }
                                                                    }
                                                                    else
                                                                    {
                                                                        boolean notANumber = true;
                                                                        for(char c : numbers)
                                                                        {
                                                                            if(c == key2) // we found a number.
                                                                            {
                                                                                notANumber = false;
                                                                                if(foundOperator2) //we're working on the denominator now
                                                                                {
                                                                                    if(chordDom == -1)
                                                                                    {
                                                                                        chordDom = Integer.parseInt(Character.toString(c));
                                                                                    }
                                                                                    else
                                                                                    {
                                                                                        chordDom *= 10;
                                                                                        chordDom += Integer.parseInt(Character.toString(c));
                                                                                    }
                                                                                }
                                                                                else //we're working on the numerator now
                                                                                {
                                                                                    if(chordNum == -1)
                                                                                    {
                                                                                        chordNum = Integer.parseInt(Character.toString(c));
                                                                                    }
                                                                                    else
                                                                                    {
                                                                                        chordNum *= 10;
                                                                                        chordNum += Integer.parseInt(Character.toString(c));
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                        if(notANumber)
                                                                        {
                                                                            Clef.LOGGER.warn("Uh oh, we found a problem looking for the chord duration in " + file.getName() + ". Found key: " + Character.toString(key));
                                                                            Clef.LOGGER.warn("Line: " + line);
                                                                        }
                                                                    }
                                                                    r++;
                                                                }
                                                                if(chordDom == -1)
                                                                {
                                                                    if(foundOperator2) // we found the operator but not the denominator. Default it.
                                                                    {
                                                                        chordDom = 2;
                                                                    }
                                                                    else //we didn't find an operator. number is a whole. Not a fraction.
                                                                    {
                                                                        chordDom = 1;
                                                                    }
                                                                }
                                                                if(chordNum == -1) //we didn't find any numbers at all. set the numerator to 1
                                                                {
                                                                    chordNum = 1;
                                                                }
                                                                chord.duration = chordNum / (float)chordDom;

                                                                //add the chord and reset
                                                                if(brokenRhythmValue != 0)
                                                                {
                                                                    ArrayList<Note> trackNotes1 = chord.notes;
                                                                    if(!trackNotes1.isEmpty() && trackNotes1.get(trackNotes1.size() - 1) instanceof SingleNote)
                                                                    {
                                                                        SingleNote referenceNote = (SingleNote)trackNotes1.get(trackNotes1.size() - 1);
                                                                        if(brokenRhythmValue == 1)//give more to this and take from previous
                                                                        {
                                                                            singleNote.duration += referenceNote.duration / 2D;
                                                                            referenceNote.duration /= 2D;
                                                                        }
                                                                        else
                                                                        {
                                                                            referenceNote.duration += singleNote.duration / 2D;
                                                                            singleNote.duration /= 2D;
                                                                        }
                                                                    }
                                                                }
                                                                chord.notes.add(singleNote);
                                                                added = true;
                                                                trackNotes.add(chord);
                                                                chord = null;
                                                            }
                                                        }
                                                        else
                                                        {
                                                            boolean notANumber = true;
                                                            for(char c : numbers)
                                                            {
                                                                if(c == key1) // we found a number.
                                                                {
                                                                    notANumber = false;
                                                                    if(foundOperator) //we're working on the denominator now
                                                                    {
                                                                        if(noteDom == -1)
                                                                        {
                                                                            noteDom = Integer.parseInt(Character.toString(c));
                                                                        }
                                                                        else
                                                                        {
                                                                            noteDom *= 10;
                                                                            noteDom += Integer.parseInt(Character.toString(c));
                                                                        }
                                                                    }
                                                                    else //we're working on the numerator now
                                                                    {
                                                                        if(noteNum == -1)
                                                                        {
                                                                            noteNum = Integer.parseInt(Character.toString(c));
                                                                        }
                                                                        else
                                                                        {
                                                                            noteNum *= 10;
                                                                            noteNum += Integer.parseInt(Character.toString(c));
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            if(notANumber)
                                                            {
                                                                Clef.LOGGER.warn("Uh oh, we found a problem looking for the note duration in " + file.getName() + ". Found key: " + Character.toString(key));
                                                                Clef.LOGGER.warn("Line: " + line);
                                                            }
                                                        }
                                                        r++;
                                                    }
                                                    if(noteDom == -1)
                                                    {
                                                        if(foundOperator) // we found the operator but not the denominator. Default it.
                                                        {
                                                            noteDom = 2;
                                                        }
                                                        else //we didn't find an operator. number is a whole. Not a fraction.
                                                        {
                                                            noteDom = 1;
                                                        }
                                                    }
                                                    if(noteNum == -1) //we didn't find any numbers at all. set the numerator to 1
                                                    {
                                                        noteNum = 1;
                                                    }
                                                    singleNote.duration = noteNum / (float)noteDom;
                                                }
                                            }
                                        }
                                        if(brokenRhythmValue != 0)
                                        {
                                            ArrayList<Note> trackNotes1 = chord != null ? chord.notes : trackNotes;
                                            if(!trackNotes1.isEmpty() && trackNotes1.get(trackNotes1.size() - 1) instanceof SingleNote)
                                            {
                                                SingleNote referenceNote =  (SingleNote)trackNotes1.get(trackNotes1.size() - 1);
                                                if(brokenRhythmValue == 1)//give more to this and take from previous
                                                {
                                                    singleNote.duration += referenceNote.duration / 2D;
                                                    referenceNote.duration /= 2D;
                                                }
                                                else
                                                {
                                                    referenceNote.duration += singleNote.duration / 2D;
                                                    singleNote.duration /= 2D;
                                                }
                                            }
                                        }
                                        if(!added && !singleNote.constructs.isEmpty())
                                        {
                                            if(chord != null)
                                            {
                                                chord.notes.add(singleNote);
                                            }
                                            else
                                            {
                                                trackNotes.add(singleNote);
                                            }
                                        }
                                    }
                                }
                            }
                            if(v + 1 < partsBarLines.size())
                            {
                                trackNotes.add(new BarLine()); //last line.
                            }
                        }
                    }
                }
            }
            //OVER HERE

            if(trackNotes.isEmpty())
            {
                return null; //If the track has no notes, don't bother creating an "empty track".
            }

            double[] info = new double[] {
                    20D, //ticks per beat
                    0.125D, //unit note length
                    1D, //meter
                    0D, //key.. unused.
                    0.25D //tempo split
            };
            HashMap<Integer, Integer> keySignatures = new HashMap<>();
            HashMap<Integer, Integer> keyAccidentals = new HashMap<>();
            float currentParticialTick = 0F; //track particial ticks as well, to make timing more accurate
            int currentTick = 0;
            for(Note note : trackNotes)
            {
                //noinspection unchecked
                HashSet<Note>[] noteAtTime = abc.notes.computeIfAbsent(currentTick, v -> new HashSet[SUB_TICKS]);
                if(note.setup(info, keyAccidentals, keySignatures))//if true, not a special note, move to next spot.
                {
                    int subIndex = Math.min(SUB_TICKS, (int) (currentParticialTick * SUB_TICKS));
                    if (noteAtTime[subIndex] == null)
                        noteAtTime[subIndex] = new HashSet<>();
                    noteAtTime[subIndex].add(note); //only add the actual notes. No specials.
                    abc.trackLength = currentTick + note.durationInTicks + (int) (note.durationInPartialTicks + 0.99F); // adds to the length of the note.
                    currentTick += note.durationInTicks;
                    currentParticialTick += note.durationInPartialTicks;
                    //Make sure particialTick stays between 0..1
                    int currentSubTicksAsInt = (int) currentParticialTick;
                    currentParticialTick = currentParticialTick - currentSubTicksAsInt;
                    currentTick += currentSubTicksAsInt;
                }
                note.constructs.trimToSize();
            }
            return abc;
        }
        catch(IOException | NumberFormatException e)
        {
        }
        catch(Exception e)
        {
            Clef.LOGGER.warn("Error reading ABC file: " + file.getName());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean readCommand(ArrayList<Note> notes, String line)
    {
        String lineLower = line.toLowerCase();
        boolean handledLine = false;
        if(lineLower.startsWith("l:"))
        {
            //note length. Can be anywhere before key.
            String[] s = line.substring(2).split("/");
            if(s.length == 1)
            {
                notes.add(new Special(new UnitNoteLength(Integer.parseInt(s[0].trim()))));
            }
            else if(s.length == 2)
            {
                notes.add(new Special(new UnitNoteLength(Integer.parseInt(s[0].trim()) / (double)Integer.parseInt(s[1].trim()))));
            }
            handledLine = true;
        }
        else if(lineLower.startsWith("q:"))
        {
            notes.add(new Special(new Tempo(line.substring(2).trim())));
            handledLine = true;
        }
        else if(lineLower.startsWith("m:"))
        {
            String[] s = line.substring(2).split("/");
            if(s.length == 1)
            {
                notes.add(new Special(new Meter(Integer.parseInt(s[0].trim()))));
            }
            else if(s.length == 2)
            {
                notes.add(new Special(new Meter(Integer.parseInt(s[0].trim()) / (double)Integer.parseInt(s[1].trim()))));
            }
            handledLine = true;
        }
        else if(lineLower.startsWith("k:"))
        {
            handledLine = true;
            notes.add(new Special(new Key(line.substring(2).trim())));
        }
        else
        {
            for(String ig : ignoredStarts)
            {
                if(lineLower.startsWith(ig.toLowerCase()))
                {
                    handledLine = true;
                    break;
                }
            }
        }
        return handledLine;
    }
}
