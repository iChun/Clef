package me.ichun.mods.clef.common.util.abc.play.components;

import com.google.common.collect.Ordering;
import me.ichun.mods.clef.common.util.abc.AbcObject;
import me.ichun.mods.clef.common.util.abc.construct.Construct;
import me.ichun.mods.clef.common.util.abc.construct.Number;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class TrackInfo
{
    public String title = ""; //T
    public String composer = ""; //C
    public String transcriber = ""; //Z

    public TreeMap<Integer, ArrayList<Note>> notes = new TreeMap<>(Ordering.natural());
    public int trackLength = 0;

    private TrackInfo(String title, String composer, String transcriber)
    {
        this.title = title;
        this.composer = composer;
        this.transcriber = transcriber;
    }

    public static TrackInfo buildTrack(AbcObject abc)
    {
        TrackInfo trackInfo = new TrackInfo(abc.title, abc.composer, abc.transcriber);

        ArrayList<Note> notes = new ArrayList<>();
        boolean discard = false;
        Chord chord = null;
        Note currentNote = null;
        int lastId = 0;
        int numberNum = -1;
        int numberDen = -1;
        int chordNum = -1;
        int chordDen = -1;
        boolean hasNotes = false;
        for(int i = 0; i < abc.constructs.size(); i++)
        {
            Construct construct = abc.constructs.get(i);

            if(discard)
            {
                if(construct.getType() == Construct.EnumConstructType.GRACE)
                {
                    discard = false;
                }
                continue;
            }

            if(construct.getType() == Construct.EnumConstructType.SPECIAL)
            {
                notes.add(new Special(construct));
                lastId = 0;
                numberNum = numberDen = chordNum = chordDen = -1;
                continue;
            }

            int tries = 0;
            for(; lastId <= Construct.EnumConstructOrder.MAX_ID; lastId++)
            {
                //process
                if(Construct.EnumConstructOrder.ORDER[lastId].getType() == construct.getType().getId())
                {
                    //Found the type. Process and move to next construct;
                    switch(Construct.EnumConstructOrder.ORDER[lastId])
                    {
                        case GRACE_OPEN:
                        {
                            discard = true;
                            break;
                        }
                        case CHORD_START:
                        {
                            chord = new Chord();
                            break;
                        }
                        case OCTAVE:
                        {
                            lastId--;
                            //DO NOT BREAK;
                        }
                        case ACCIDENTAL:
                        case NOTE:
                        {
                            if(currentNote == null)
                            {
                                currentNote = new SingleNote();
                            }
                            currentNote.constructs.add(construct);
                            break;
                        }
                        case NUMBER_NUMERATOR:
                        {
                            if(numberNum != -1)
                            {
                                numberNum *= 10;
                                numberNum += ((Number)construct).number;
                            }
                            else
                            {
                                numberNum = ((Number)construct).number;
                            }
                            lastId--;
                            break;
                        }
                        case OPERATOR:
                        {
                            if(numberNum == -1)
                            {
                                numberNum = 1;
                            }
                            numberDen = 2;
                            break;
                        }
                        case NUMBER_DENOMINATOR:
                        {
                            if(abc.constructs.size() >= 2 && abc.constructs.get(i - 1).getType() == Construct.EnumConstructType.OPERATOR)
                            {
                                numberDen = ((Number)construct).number;
                            }
                            else
                            {
                                numberDen *= 10;
                                numberDen += ((Number)construct).number;
                            }
                            lastId--;
                            break;
                        }
                        case SPACE:
                        {
                            lastId = 0;

                            if(currentNote != null)
                            {
                                if(numberNum > 0)
                                {
                                    if(numberDen > 0)
                                    {
                                        currentNote.duration = numberNum / (double)numberDen;
                                    }
                                    else
                                    {
                                        currentNote.duration = numberNum;
                                    }
                                }
                                if(chord != null)
                                {
                                    chord.notes.add(currentNote);
                                    currentNote = null;
                                }
                                else
                                {
                                    notes.add(currentNote);
                                    currentNote = null;
                                }
                                numberNum = numberDen = -1;
                            }
                            break;
                        }
                        case CHORD_END:
                        {
                            if(chord == null)
                            {
                                //This is a chord start. RESET the lastId;
                                lastId = 0;

                                if(currentNote != null)
                                {
                                    if(numberNum > 0)
                                    {
                                        if(numberDen > 0)
                                        {
                                            currentNote.duration = numberNum / (double)numberDen;
                                        }
                                        else
                                        {
                                            currentNote.duration = numberNum;
                                        }
                                    }
                                    if(chord != null)
                                    {
                                        chord.notes.add(currentNote);
                                        currentNote = null;
                                    }
                                    else
                                    {
                                        notes.add(currentNote);
                                        currentNote = null;
                                    }
                                    numberNum = numberDen = -1;
                                }
                                break;
                            }
                            if(currentNote != null)
                            {
                                chord.notes.add(currentNote);
                                if(numberNum > 0)
                                {
                                    if(numberDen > 0)
                                    {
                                        currentNote.duration = numberNum / (double)numberDen;
                                    }
                                    else
                                    {
                                        currentNote.duration = numberNum;
                                    }
                                }
                                currentNote = null;
                                numberNum = numberDen = -1;
                            }
                            notes.add(chord);
                            chord = null;
                            break;
                        }
                        case CHORD_NUMBER_NUMERATOR:
                        {
                            if(chordNum != -1)
                            {
                                chordNum *= 10;
                                chordNum += ((Number)construct).number;
                            }
                            else
                            {
                                chordNum = ((Number)construct).number;
                            }
                            lastId--;
                            break;
                        }
                        case CHORD_OPERATOR:
                        {
                            if(chordNum == -1)
                            {
                                chordNum = 1;
                            }
                            chordDen = 2;
                            break;
                        }
                        case CHORD_NUMBER_DENOMINATOR:
                        {
                            if(abc.constructs.size() >= 2 && abc.constructs.get(i - 1).getType() == Construct.EnumConstructType.OPERATOR)
                            {
                                chordDen = ((Number)construct).number;
                            }
                            else
                            {
                                chordDen *= 10;
                                chordDen += ((Number)construct).number;
                            }
                            lastId--;
                            break;
                        }
                        case GRACE_CLOSE:
                        {
                            break;
                        } //nothing is done cause things hsould be discarded at this point.
                        case BAR_LINE:
                        {
                            notes.add(new BarLine());
                            break;
                        }
                    }


                    break;
                }

                if(lastId >= Construct.EnumConstructOrder.MAX_ID)
                {
                    tries++;
                    if(tries >= 2)
                    {
                        break;
                    }
                    lastId = -1;

                    if(currentNote != null)
                    {
                        if(numberNum > 0)
                        {
                            if(numberDen > 0)
                            {
                                currentNote.duration = numberNum / (double)numberDen;
                            }
                            else
                            {
                                currentNote.duration = numberNum;
                            }
                        }
                        if(chord != null)
                        {
                            chord.notes.add(currentNote);
                            currentNote = null;
                        }
                        else
                        {
                            notes.add(currentNote);
                            currentNote = null;
                        }
                    }
                    else if(chord == null) // we ended a note and a chord at the same time.
                    {
                        if(chordNum > 0 && notes.get(notes.size() - 1) instanceof Chord)
                        {
                            if(chordDen > 0)
                            {
                                ((Chord)notes.get(notes.size() - 1)).duration = chordNum / (double)chordDen;
                            }
                            else
                            {
                                ((Chord)notes.get(notes.size() - 1)).duration = chordNum;
                            }
                        }
                    }
                    numberNum = numberDen = chordNum = chordDen = -1;
                }
            }
            hasNotes = true;
            lastId++;
        }

        if(!hasNotes)
        {
            return null; //If the track has no notes, don't bother creating an "empty track".
        }

        double[] info = new double[] {
                20D, //ticks per beat
                0.125D, //unit note length
                1D, //meter
                0D, //key
                0.125D //tempo split
        };
        HashMap<Integer, Integer> keyAccidentals = new HashMap<>();
        int currentTick = 0;
        for(Note note : notes)
        {
            ArrayList<Note> noteAtTime = trackInfo.notes.computeIfAbsent(currentTick, v -> new ArrayList<>());
            if(note.setup(info, keyAccidentals))//if true, not a special note, move to next spot.
            {
                noteAtTime.add(note); //only add the actual notes. No specials.
                trackInfo.trackLength = currentTick + note.durationInTicks; // adds to the length of the note.
                currentTick += note.durationInTicks;
            }
        }

        return trackInfo;
    }
}
