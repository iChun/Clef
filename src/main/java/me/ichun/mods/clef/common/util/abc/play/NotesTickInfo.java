package me.ichun.mods.clef.common.util.abc.play;

import me.ichun.mods.clef.common.util.abc.play.components.Note;
import me.ichun.mods.clef.common.util.instrument.Instrument;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public class NotesTickInfo
{
    public final Object notePos;
    public final Instrument instrument;
    public final Set<Note>[] notes;
    public final boolean checkRest;

    public NotesTickInfo(Entity entity, Instrument instrument, Set<Note>[] notes, boolean checkRest)
    {
        this.notePos = entity;
        this.instrument = instrument;
        this.notes = notes;
        this.checkRest = checkRest;
    }

    public NotesTickInfo(BlockPos pos, Instrument instrument, Set<Note>[] notes)
    {
        this.notePos = pos;
        this.instrument = instrument;
        this.notes = notes;
        this.checkRest = false;
    }
}
