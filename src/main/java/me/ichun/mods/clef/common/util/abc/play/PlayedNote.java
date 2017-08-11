package me.ichun.mods.clef.common.util.abc.play;

import me.ichun.mods.clef.client.sound.InstrumentSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlayedNote
{
    public final InstrumentSound sound;
    public final int startTick;
    public final int duration;

    public PlayedNote(float pitch, int startTick, int duration)
    {
        this.sound = new InstrumentSound(SoundEvents.BLOCK_NOTE_HARP, SoundCategory.AMBIENT, 0.2F, pitch);
        this.startTick = startTick;
        this.duration = duration;
    }

    public PlayedNote start()
    {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_NOTE_HARP, sound.pitch));
//        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
        return this;
    }

    public void stop()
    {
        sound.donePlaying = true;
    }
}
