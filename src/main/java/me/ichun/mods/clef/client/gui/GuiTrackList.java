package me.ichun.mods.clef.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GuiTrackList extends ExtendedList<GuiTrackList.TrackEntry>
{
    private GuiPlayTrack parent;
    private List<TrackFile> tracks;

    public GuiTrackList(GuiPlayTrack parent, int width, int height, int top, int bottom, int left, int entryHeight, ArrayList<TrackFile> track)
    {
        super(Minecraft.getInstance(), width, height, top, bottom, entryHeight);
        setLeftPos(left);
        this.parent = parent;
        setTracks(track);
    }

    public void setTracks(List<TrackFile> tracks)
    {
        this.tracks = tracks;
        this.children().clear();
        for(int i = 0; i < tracks.size(); i++)
        {
            TrackFile trackFile = tracks.get(i);
            addEntry(new TrackEntry(trackFile, i));
        }
    }

    public void elementClicked(int index, boolean doubleClick)
    {
        parent.setIndex(index);
        if(doubleClick)
        {
            parent.confirmSelection(true);
        }
    }

    @Override
    public boolean isSelectedItem(int index)
    {
        return parent.isSelectedIndex(index);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick)
    {
        RenderHelper.startGlScissor(getLeft(), getTop(), width, height - 1);

        int i = this.getScrollbarPosition();
        int j = i + 6;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = this.getRowLeft();
        int l = this.y0 + 4 - (int)this.getScrollAmount();
        if (this.renderHeader) {
            this.renderHeader(k, l, tessellator);
        }

        int oriWidth = width;
        width = width - 6;
        this.renderList(k, l, mouseX, mouseY, partialTick);
        width = oriWidth;

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();
        int j1 = Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4)); //this.getMaxScroll();
        if (j1 > 0) {
            int k1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            k1 = MathHelper.clamp(k1, 32, this.y1 - this.y0 - 8);
            int l1 = (int)this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
            if (l1 < this.y0) {
                l1 = this.y0;
            }

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double)i, (double)this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos((double)j, (double)this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos((double)j, (double)this.y0, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos((double)i, (double)this.y0, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double)i, (double)(l1 + k1), 0.0D).tex(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos((double)j, (double)(l1 + k1), 0.0D).tex(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos((double)j, (double)l1, 0.0D).tex(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos((double)i, (double)l1, 0.0D).tex(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double)i, (double)(l1 + k1 - 1), 0.0D).tex(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos((double)(j - 1), (double)l1, 0.0D).tex(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos((double)i, (double)l1, 0.0D).tex(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }

        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();

        RenderHelper.endGlScissor();
    }

    @Override
    protected void renderHoleBackground(int p_renderHoleBackground_1_, int p_renderHoleBackground_2_, int p_renderHoleBackground_3_, int p_renderHoleBackground_4_)
    {
    }

    @Override
    public int getRowWidth() {
        return this.width; //minus scrollbar width;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getLeft() + this.width - 6;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int btn)
    {
        if(parent.bandName.getText().isEmpty() || parent.syncTrack == 0 || !parent.disableListWhenSyncTrack)
        {
            return super.mouseClicked(mouseX, mouseY, btn);
        }
        return false;
    }

    public class TrackEntry extends AbstractList.AbstractListEntry<TrackEntry>
    {
        public final TrackFile track;
        public final int index;

        public long lastClickTime = -1;

        public TrackEntry(TrackFile track, int index)
        {
            this.track = track;
            this.index = index;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int btn)
        {
            if(isMouseOver(mouseX, mouseY) && btn == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                elementClicked(index, System.currentTimeMillis() - this.lastClickTime < 250L);
                lastClickTime = System.currentTimeMillis();
            }
            return false;
        }

        @Override
        public void render(int idx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks)
        {
            if(idx >= 0 && idx < tracks.size())
            {
                FontRenderer font = parent.getFontRenderer();
                RenderSystem.pushMatrix();
                RenderSystem.scalef(0.5F, 0.5F, 1F);
                String trim = font.trimStringToWidth(track.track.getTitle(), (width - 10) * 2);
                if(isSelectedItem(idx) && !track.track.getTitle().endsWith(trim))
                {
                    int lengthDiff = (int)Math.ceil((track.track.getTitle().length() - trim.length()) * 1.4D);
                    String newString = track.track.getTitle().substring(lengthDiff);
                    int val = ((parent.scrollTicker % (lengthDiff * 2 + 40)) / 2) - 10;
                    if(val < 0)
                    {
                        val = 0;
                    }
                    String newTrim = font.trimStringToWidth(track.track.getTitle().substring(val), (width - 10) * 2);
                    if(newString.length() > newTrim.length())
                    {
                        trim = newString;
                    }
                    else
                    {
                        trim = newTrim;
                    }
                }
                font.drawString(trim, (left + 2) * 2, top * 2, idx % 2 == 0 ? 0xFFFFFF : 0xAAAAAA);
                RenderSystem.popMatrix();
            }
        }
    }
}
