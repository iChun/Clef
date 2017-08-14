package me.ichun.mods.clef.client.gui;

import me.ichun.mods.clef.common.util.abc.TrackFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.ArrayList;

public class GuiTrackList extends GuiScrollingList
{
    private GuiPlayTrack parent;
    public ArrayList<TrackFile> tracks;

    public GuiTrackList(GuiPlayTrack parent, int width, int height, int top, int bottom, int left, int entryHeight, ArrayList<TrackFile> track)
    {
        super(Minecraft.getMinecraft(), width, height, top, bottom, left, entryHeight, parent.width, parent.height);
        this.parent = parent;
        this.tracks = track;
    }

    @Override
    protected int getSize()
    {
        return tracks.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick)
    {
        parent.setIndex(index);
        if(doubleClick)
        {
            parent.confirmSelection(true);
        }
    }

    @Override
    protected boolean isSelected(int index)
    {
        return parent.isSelectedIndex(index);
    }

    @Override
    protected void drawBackground()
    {
    }

    @Override
    protected void drawSlot(int idx, int right, int top, int height, Tessellator tess)
    {
        FontRenderer font     = this.parent.getFontRenderer();
        TrackFile track = tracks.get(idx);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5F, 0.5F, 1F);
        font.drawString(font.trimStringToWidth(track.track.title, (listWidth - 10) * 2), (this.left + 2) * 2 , top * 2, idx % 2 == 0 ? 0xFFFFFF : 0xAAAAAA);
        GlStateManager.popMatrix();
    }
}
