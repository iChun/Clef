package me.ichun.mods.clef.common.util.abc;

import me.ichun.mods.clef.common.util.abc.construct.Octave;
import me.ichun.mods.clef.common.util.abc.construct.special.Meter;
import me.ichun.mods.clef.common.util.abc.construct.special.Tempo;
import me.ichun.mods.clef.common.util.abc.play.components.Note;
import me.ichun.mods.clef.common.util.abc.play.components.SingleNote;
import me.ichun.mods.clef.common.util.abc.play.components.Special;
import me.ichun.mods.clef.common.util.abc.play.components.TrackInfo;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class MidiParser
{
    private static final Logger LOGGER = LogManager.getLogger(MidiParser.class);
    private static final boolean DEBUG = false;

    public static TrackInfo parse(File file)
    {
        debug("------------------------------------------------");
        debug("--------------------NEW FILE--------------------");
        debug("------------------------------------------------");
        debug("Parsing " + file);
        TreeMap<Long, Set<Note>> trackNotes = new TreeMap<>(Long::compareTo);
        TrackInfo trackInfo = new TrackInfo();
        trackInfo.setFileTitle(file.getName().substring(0, file.getName().length() - 4));
        try
        {
            Sequence sequence = MidiSystem.getSequence(file);
            debug("Resolution: " +  sequence.getResolution() + ", type: " + sequence.getDivisionType());
            if (sequence.getDivisionType() != Sequence.PPQ) {
                LOGGER.warn("Clef can't handle non-PPQ midi files right now!");
                return null;
            }
            int ticksPerBeat = sequence.getResolution();
            trackNotes.computeIfAbsent(0L, unused -> new HashSet<>()).add(new Special(new Tempo(-1, ticksPerBeat)));
            for (Track track : sequence.getTracks())
            {
                debug("-------------------------------------------------");
                debug("--------------------NEW TRACK--------------------");
                debug("-------------------------------------------------");
                for (int i = 0; i < track.size(); i++)
                {
                    MidiEvent midiEvent = track.get(i);
                    if (midiEvent.getMessage() instanceof MetaMessage)
                    {
                        parseMetaMessage(midiEvent, trackNotes, trackInfo, ticksPerBeat);
                    }
                    else if (midiEvent.getMessage() instanceof ShortMessage)
                    {
                        ShortMessage message = (ShortMessage) midiEvent.getMessage();
                        switch (message.getCommand())
                        {
                            case ShortMessage.NOTE_ON:
                            {
                                // start of a new note
                                SingleNote note = new SingleNote();


                                int key = message.getData1();
                                int velocity = message.getData2();
                                note.volume = MathHelper.clamp(velocity / 128F, 0.2F, 0.8F);
                                int keyToPlay = key % 12;
                                if (key >= (6 * 12)) {
                                    note.constructs.add(new Octave('.'));
                                } else if (key < (4 * 12)) {
                                    note.constructs.add(new Octave(','));
                                }
                                note.constructs.add(me.ichun.mods.clef.common.util.abc.construct.Note.createFromRawKey(keyToPlay));
                                // try to find the end
                                boolean found = false;
                                for (int j = i + 1; j < track.size(); j++)
                                {
                                    MidiEvent possibleEnd = track.get(j);
                                    if (possibleEnd.getMessage() instanceof ShortMessage && ((ShortMessage) possibleEnd.getMessage()).getCommand() == ShortMessage.NOTE_OFF) {
                                        ShortMessage endMessage = (ShortMessage) possibleEnd.getMessage();
                                        if (endMessage.getData1() == key)
                                        {
                                            // this is the end. Calculate the duration based of that
                                            note.duration = (possibleEnd.getTick() - midiEvent.getTick());
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                                if (found) {
                                    Set<Note> notes = trackNotes.computeIfAbsent(midiEvent.getTick(), unused -> new HashSet<>());
                                    notes.add(note);
                                } else {
                                    LOGGER.warn("Uh oh, we could not find the end for a key!");
                                }
                            }
                                break;
                            case ShortMessage.NOTE_OFF:
                            {
//                                int key = message.getData1();
//                                int velocity = message.getData2();
                            }
                                break;
                            default:
                                debug(midiEvent.getTick() + ":" + message.getCommand() + ":" + midiEvent.getMessage());

                        }
                    }
                    else
                    {
                        debug(midiEvent.getTick() + ":" + midiEvent.getMessage());
                    }
                }
            }
            return TrackBuilder.buildMidiTrack(trackNotes, trackInfo);
        } catch (IOException | InvalidMidiDataException e)
        {
            throw new RuntimeException(e); //TODO
        }
    }

    private static void parseMetaMessage(MidiEvent midiEvent, TreeMap<Long, Set<Note>> track, TrackInfo trackInfo, int ticksPerBeat) {
        if (midiEvent.getMessage() instanceof MetaMessage)
        {
            MetaMessage message = (MetaMessage) midiEvent.getMessage();
            byte[] data = message.getData();
            switch (message.getType())
            {
                case 0x00:
                    debug(midiEvent.getTick() + ":Sequence number");
                    break;
                case 0x01:
                    debug(midiEvent.getTick() + ":Text: " + new String(data));
                    break;
                case 0x02:
                    debug(midiEvent.getTick() + ":Copyright: " + new String(data));
                    trackInfo.composer = new String(data);
                    break;
                case 0x03:
                    debug(midiEvent.getTick() + ":Track name: " + new String(data));
                    break;
                case 0x04:
                    debug(midiEvent.getTick() + ":Instrument name: " + new String(data));
                    break;
                case 0x05:
                    debug(midiEvent.getTick() + ":Lyric: " + new String(data));
                    break;
                case 0x06:
                    debug(midiEvent.getTick() + ":Marker: " + new String(data));
                    break;
                case 0x08:
                    debug(midiEvent.getTick() + ":Program Name" + new String(data));
                    break;
                case 0x09:
                    debug(midiEvent.getTick() + ":Device port name : " + new String(data));
                    break;
                case 0x2F:
                    debug(midiEvent.getTick() + ":End of track");
                    break;
                case 0x51:
                    if (data.length == 3)
                    {
                        int usPerQuarterNote = (Byte.toUnsignedInt(data[0]) << 16) + (Byte.toUnsignedInt(data[1]) << 8) + (Byte.toUnsignedInt(data[2]));
                        float sPerQuarterNote = ((usPerQuarterNote / 1000F) / 1000F);
                        float bpm = 60F / sPerQuarterNote;
                        Set<Note> notes = track.computeIfAbsent(midiEvent.getTick(), unused -> new HashSet<>());
                        notes.add(new Special(new Tempo(Math.round(bpm), ticksPerBeat)));
                        debug(midiEvent.getTick() + ":Tempo: " + bpm);
                        break;
                    }
                case 0x54:
                    debug("SMPTE offset");
                    break;
                case 0x58:
                    if (data.length == 4)
                    {
                        int nn = Byte.toUnsignedInt(data[0]); //numerator
                        int dd = Byte.toUnsignedInt(data[1]); //denominator
                        int cc = Byte.toUnsignedInt(data[2]); //number of midi clocks in a metronome click
                        int bb = Byte.toUnsignedInt(data[3]); //expresses the number of notated 32nd notes in a MIDI quarter note (24 MIDI clocks).
                        Set<Note> notes = track.computeIfAbsent(midiEvent.getTick(), unused -> new HashSet<>());
                        notes.add(new Special(new Meter(nn / Math.pow(2, dd))));
                        debug(midiEvent.getTick() + ":Time signature: " + nn + "/" + Math.pow(2, dd));
                    }
                    break;
                case 0x59:
                    if (data.length == 2)
                    {
                        int sf = data[0]; // key signature, negative: number of flats, positive: number of sharps. Range -7 to 7
                        int mi = Byte.toUnsignedInt(data[1]); // major/minor key 0=major,1=minor
                        debug(midiEvent.getTick() + ":Key signature: " + data[0] + " " +(mi == 0 ? "major" : "minor"));
                    }
                    break;
                default:
//                    debug(midiEvent.getTick() + ":" + message.getType() + ":" + midiEvent.getMessage());
                    break;
            }
        }
    }
    
    private static void debug(String msg)
    {
        if (DEBUG)
        {
            LOGGER.info("[MidiParser] {}", msg);
        }
    }
}
