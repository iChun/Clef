package me.ichun.mods.clef.common.util.abc.play;

import me.ichun.mods.clef.common.util.abc.AbcObject;
import me.ichun.mods.clef.common.util.abc.construct.Construct;
import me.ichun.mods.clef.common.util.abc.construct.Number;
import me.ichun.mods.clef.common.util.abc.play.components.Chord;
import me.ichun.mods.clef.common.util.abc.play.components.Note;
import me.ichun.mods.clef.common.util.abc.play.components.SingleNote;
import me.ichun.mods.clef.common.util.abc.play.components.Special;

import java.util.ArrayList;

public class Track
{
    public String title = ""; //T
    public String composer = ""; //C
    public String transcriber = ""; //Z

    public int beatOffset = 0;
    public int ticksPerBeat;
    public double currentMeter;
    public double unitNoteLength;

    public int playProg;
    public boolean playing = true;

    public ArrayList<Note> notes = new ArrayList<>();
    public int trackLength = 0;

    public ArrayList<PlayedNote> playingNotes = new ArrayList<>();

    public Track()
    {
    }

    public boolean update() //returns false if it's time to stop laying.
    {
        if(!playing || playProg > trackLength)
        {
            return false;
        }
        return true;
    }

    public void stop()
    {
        playing = false;
    }

    public void playAtProgress(int i)
    {
        playProg = i;
    }

    public static Track buildTrack(AbcObject abc)
    {
        Track track = new Track();

        boolean discard = false;
        Chord chord = null;
        Note currentNote = null;
        int lastId = 0;
        int numberNum = -1;
        int numberDen = -1;
        int chordNum = -1;
        int chordDen = -1;
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
                track.notes.add(new Special(construct));
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
                        case ACCIDENTAL:
                        case NOTE:
                        case OCTAVE:
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
                            numberNum = ((Number)construct).number;
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
                            numberDen = ((Number)construct).number;
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
                                    track.notes.add(currentNote);
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
                                        track.notes.add(currentNote);
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
                            track.notes.add(chord);
                            chord = null;
                            break;
                        }
                        case CHORD_NUMBER_NUMERATOR:
                        {
                            chordNum = ((Number)construct).number;
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
                            chordDen = ((Number)construct).number;
                            break;
                        }
                        case GRACE_CLOSE:{break;} //nothing is done cause things hsould be discarded at this point.
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
                            track.notes.add(currentNote);
                            currentNote = null;
                        }
                    }
                    else if(chord == null) // we ended a note and a chord at the same time.
                    {
                        if(chordNum > 0 && track.notes.get(track.notes.size() - 1) instanceof Chord)
                        {
                            if(chordDen > 0)
                            {
                                ((Chord)track.notes.get(track.notes.size() - 1)).duration = chordNum / (double)chordDen;
                            }
                            else
                            {
                                ((Chord)track.notes.get(track.notes.size() - 1)).duration = chordNum;
                            }
                        }
                    }
                    numberNum = numberDen = chordNum = chordDen = -1;
                }
            }
            lastId++;
        }

        //TODO calculate the length of the track.

        return track;
    }
}
