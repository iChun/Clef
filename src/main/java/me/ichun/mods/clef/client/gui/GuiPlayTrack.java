package me.ichun.mods.clef.client.gui;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.packet.PacketPlayABC;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class GuiPlayTrack extends GuiScreen
{
    public static final ResourceLocation texBackground = new ResourceLocation("clef", "textures/gui/track_select.png");
    public static final ResourceLocation texBackgroundBlock = new ResourceLocation("clef", "textures/gui/track_select_block.png");
    public static final ResourceLocation texInstrument = new ResourceLocation("clef", "textures/items/instrument.png");
    public static final ResourceLocation texNote = new ResourceLocation("minecraft", "textures/particle/particles.png");
    public static final ResourceLocation texIcons = new ResourceLocation("minecraft", "textures/gui/icons.png");

    public static final int ID_CONFIRM = 0;
    public static final int ID_SYNC_PLAY = 1;
    public static final int ID_SYNC_TRACK = 2;
    public static final int ID_RELOAD_INSTRUMENTS = 20;
    public static final int ID_RELOAD_TRACKS = 21;
    public static final int ID_TOGGLE_TITLE = 22;

    public ResourceLocation background = texBackground;

    protected int xSize = 256;
    protected int ySize = 230;

    protected int guiLeft;
    protected int guiTop;

    public GuiTextField bandName;

    public GuiTrackList trackList;
    public int trackListBottom;

    public int syncPlay = 1;
    public int syncTrack = 0;

    public int index = -1;
    public int doneTimeout = 0;

    public ArrayList<TrackFile> tracks;
    public String bandNameString = "";

    public int scrollTicker = 0;
    public int bandIndex = 0;

    public boolean disableListWhenSyncTrack = true;

    public GuiPlayTrack()
    {
        tracks = AbcLibrary.tracks;
        bandNameString = Clef.config.favoriteBand;
    }

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        trackListBottom = guiTop + ySize - 6;

        buttonList.clear();

        buttonList.add(new GuiButton(ID_CONFIRM, guiLeft + 174, guiTop + 210, 83, 20, I18n.translateToLocal("clef.gui.play")));
        addButtons();

        bandName = new GuiTextField(0, mc.fontRenderer, this.guiLeft + 181, this.guiTop + 18, 64, mc.fontRenderer.FONT_HEIGHT);
        bandName.setMaxStringLength(15);
        bandName.setEnableBackgroundDrawing(false);
        bandName.setTextColor(16777215);
        bandName.setText(bandNameString);

        for(GuiButton btn : buttonList)
        {
            if(btn.id == ID_SYNC_PLAY)
            {
                btn.enabled = !bandName.getText().isEmpty() && syncTrack == 0;
                btn.displayString = I18n.translateToLocal(syncPlay == 1 ? "gui.yes" : "gui.no");
            }
            else if(btn.id == ID_SYNC_TRACK)
            {
                btn.enabled = !bandName.getText().isEmpty();
                btn.displayString = I18n.translateToLocal(syncTrack == 1 ? "gui.yes" : "gui.no");
            }
        }

        trackList = new GuiTrackList(this, 158, ySize - 22, guiTop + 17, trackListBottom, guiLeft + 7, 8, tracks);
    }

    public void addButtons()
    {
        buttonList.add(new GuiButton(ID_SYNC_PLAY, guiLeft + 179, guiTop + 51, 72, 20, I18n.translateToLocal(syncPlay == 1 ? "gui.yes" : "gui.no")));
        buttonList.add(new GuiButton(ID_SYNC_TRACK, guiLeft + 179, guiTop + 94, 72, 20, I18n.translateToLocal(syncTrack == 1 ? "gui.yes" : "gui.no")));
        addReloadButtons();
    }

    public void addReloadButtons()
    {
        buttonList.add(new GuiButton(ID_RELOAD_INSTRUMENTS, guiLeft + 179, guiTop + 137, 20, 20, ""));
        buttonList.add(new GuiButton(ID_RELOAD_TRACKS, guiLeft + 205, guiTop + 137, 20, 20, ""));
        buttonList.add(new GuiButton(ID_TOGGLE_TITLE, guiLeft + 231, guiTop + 137, 20, 20, ""));
    }

    public FontRenderer getFontRenderer()
    {
        return fontRenderer;
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen()
    {
        scrollTicker++;
        doneTimeout--;
        bandName.updateCursorCounter();
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        super.handleMouseInput();
        if(syncTrack == 0 || !disableListWhenSyncTrack)
        {
            this.trackList.handleMouseInput(mouseX, mouseY);
        }
        bandNameString = bandName.getText();
    }

    @Override
    protected void keyTyped(char c, int i)
    {
        bandName.textboxKeyTyped(c, i);
        if(bandName.isFocused())
        {
            if (i != 1)
            {
                syncPlay = 1;
                syncTrack = 1;
                for(GuiButton btn : buttonList)
                {
                    if(btn.id == ID_SYNC_PLAY)
                    {
                        btn.enabled = false;
                        btn.displayString = I18n.translateToLocal(syncPlay == 1 ? "gui.yes" : "gui.no");
                    }
                    else if(btn.id == ID_SYNC_TRACK)
                    {
                        btn.displayString = I18n.translateToLocal(syncTrack == 1 ? "gui.yes" : "gui.no");
                    }
                }
            }
        }
        if (i == 1)
        {
            if(bandName.isFocused())
            {
                bandName.setText("");
                bandName.setFocused(false);
            }
            else
            {
                closeScreen();
                mc.setIngameFocus();
            }
        }
        for(GuiButton btn : buttonList)
        {
            if(btn.id == ID_SYNC_PLAY)
            {
                btn.enabled = !bandName.getText().isEmpty() && syncTrack == 0;
                btn.displayString = I18n.translateToLocal(syncPlay == 1 ? "gui.yes" : "gui.no");
            }
            else if(btn.id == ID_SYNC_TRACK)
            {
                btn.enabled = !bandName.getText().isEmpty();
                btn.displayString = I18n.translateToLocal(syncTrack == 1 ? "gui.yes" : "gui.no");
            }
        }
        bandNameString = bandName.getText();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        //        initGui();
        if(mc == null)
        {
            mc = Minecraft.getMinecraft();
            fontRenderer = mc.fontRenderer;
        }
        drawDefaultBackground();

        GlStateManager.color(1F, 1F, 1F, 1F);
        this.mc.getTextureManager().bindTexture(background);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        Gui.drawRect(guiLeft + 6, guiTop + 16, guiLeft + 166, trackListBottom + 1, -1072689136);

        if(bandName.getVisible())
        {
            bandName.drawTextBox();
        }

        fontRenderer.drawString(I18n.translateToLocal("clef.gui.band"), guiLeft + 179, guiTop + 5, 16777215, true);

        if(bandName.getText().isEmpty() && !bandName.isFocused())
        {
            fontRenderer.drawString(I18n.translateToLocal("clef.gui.bandSolo"), guiLeft + 182, guiTop + 18, 0xcccccc, false);
        }

        drawText();

        trackList.drawScreen(mouseX, mouseY, partialTicks);

        super.drawScreen(mouseX, mouseY, partialTicks);

        drawReloadButtons();

        if(syncTrack == 1 && disableListWhenSyncTrack && !bandName.getText().isEmpty())
        {
            Gui.drawRect(guiLeft + 6, guiTop + 16, guiLeft + 166, trackListBottom + 1, -1072689136);
        }
    }

    public void drawText()
    {
        fontRenderer.drawString(I18n.translateToLocal("clef.gui.chooseSong") + " (" + tracks.size() + ")", guiLeft + 6, guiTop + 5, 16777215, true);
        fontRenderer.drawString(I18n.translateToLocal("clef.gui.syncPlayTime"), guiLeft + 179, guiTop + 40, 16777215, true);
        fontRenderer.drawString(I18n.translateToLocal("clef.gui.syncTrack"), guiLeft + 179, guiTop + 83, 16777215, true);
        GlStateManager.pushMatrix();
        int length = fontRenderer.getStringWidth(I18n.translateToLocal("clef.gui.moreSongs"));
        GlStateManager.translate(guiLeft - 4, guiTop + length + 3, 0);
        GlStateManager.scale(0.5F, 0.5F, 1F);
        GlStateManager.rotate(-90F, 0F, 0F, 1F);
        fontRenderer.drawString(I18n.translateToLocal("clef.gui.moreSongs"), 0, 0, 16777215, true);
        GlStateManager.popMatrix();
    }

    public void closeScreen()
    {
        mc.displayGuiScreen(null);
    }

    public void drawReloadButtons()
    {
        disableListWhenSyncTrack = true;
        if(!bandName.getText().isEmpty())
        {
            disableListWhenSyncTrack = Clef.eventHandlerClient.findTrackByBand(bandName.getText()) != null;
        }

        fontRenderer.drawString(I18n.translateToLocal("clef.gui.reload"), guiLeft + 179, guiTop + 126, 16777215, true);
        if(doneTimeout > 0)
        {
            fontRenderer.drawString(I18n.translateToLocal("gui.done"), guiLeft + 179 + 2 + fontRenderer.getStringWidth(I18n.translateToLocal("clef.gui.reload")), guiTop + 126, 16777215, true);
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
        this.mc.getTextureManager().bindTexture(texInstrument);
        float x = guiLeft + 179 + 2;
        float y = guiTop + 137 + 2;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(7, DefaultVertexFormats.POSITION_TEX);
        builder.pos((double)(x + 0), (double)(y + 16), (double)this.zLevel).tex(0F, 1F).endVertex();
        builder.pos((double)(x + 16), (double)(y + 16), (double)this.zLevel).tex(1F, 1F).endVertex();
        builder.pos((double)(x + 16), (double)(y + 0), (double)this.zLevel).tex(1F, 0F).endVertex();
        builder.pos((double)(x + 0), (double)(y + 0), (double)this.zLevel).tex(0F, 0F).endVertex();
        tessellator.draw();

        this.mc.getTextureManager().bindTexture(texIcons);
        this.drawTexturedModalRect(guiLeft + 236, guiTop + 142, 0, 224, 10, 10);

        GlStateManager.color(0.5F, 1F, 1F, 1F);
        this.mc.getTextureManager().bindTexture(texNote);
        this.drawTexturedModalRect(guiLeft + 205 + 2, guiTop + 137 + 2, 0, 64, 16, 16);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int btn) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, btn);
        bandName.mouseClicked(mouseX, mouseY, btn);
        if(btn == 1) //RMB
        {
            boolean flag = mouseX >= bandName.x && mouseX < bandName.x + this.width && mouseY >= bandName.y && mouseY < bandName.y + this.height;
            if(flag)
            {
                if(bandName.getText().isEmpty())
                {
                    ArrayList<String> bands = new ArrayList<>();
                    for(Track track : Clef.eventHandlerClient.tracksPlaying)
                    {
                        if(!track.getBandName().isEmpty() && !bands.contains(track.getBandName()))
                        {
                            bands.add(track.getBandName());
                        }
                    }
                    if(!bands.isEmpty())
                    {
                        if(bandIndex >= bands.size())
                        {
                            bandIndex = 0;
                        }
                        bandName.setText(bands.get(bandIndex));
                        syncPlay = 1;
                        syncTrack = 1;
                        for(GuiButton btn1 : buttonList)
                        {
                            if(btn1.id == ID_SYNC_PLAY)
                            {
                                btn1.enabled = false;
                                btn1.displayString = I18n.translateToLocal(syncPlay == 1 ? "gui.yes" : "gui.no");
                            }
                            else if(btn1.id == ID_SYNC_TRACK)
                            {
                                btn1.enabled = true;
                                btn1.displayString = I18n.translateToLocal(syncTrack == 1 ? "gui.yes" : "gui.no");
                            }
                        }
                        bandIndex++;
                    }
                }
                else
                {
                    bandName.setText("");
                    for(GuiButton btn1 : buttonList)
                    {
                        if(btn1.id == ID_SYNC_PLAY)
                        {
                            btn1.enabled = false;
                            btn1.displayString = I18n.translateToLocal(syncPlay == 1 ? "gui.yes" : "gui.no");
                        }
                        else if(btn1.id == ID_SYNC_TRACK)
                        {
                            btn1.enabled = false;
                            btn1.displayString = I18n.translateToLocal(syncTrack == 1 ? "gui.yes" : "gui.no");
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton btn)
    {
        if(btn.id == ID_CONFIRM)
        {
            confirmSelection(false);
        }
        else if(btn.id == ID_SYNC_PLAY)
        {
            syncPlay = syncPlay == 1 ? 0 : 1;
            btn.displayString = I18n.translateToLocal(syncPlay == 1 ? "gui.yes" : "gui.no");
        }
        else if(btn.id == ID_SYNC_TRACK)
        {
            syncTrack = syncTrack == 1 ? 0 : 1;
            btn.displayString = I18n.translateToLocal(syncTrack == 1 ? "gui.yes" : "gui.no");

            for(GuiButton btn1 : buttonList)
            {
                if(btn1.id == ID_SYNC_PLAY)
                {
                    btn1.enabled = syncTrack == 0;
                    if(!btn1.enabled)
                    {
                        syncPlay = 1;
                        btn1.displayString = I18n.translateToLocal("gui.yes");
                    }
                    break;
                }
            }
        }
        else if(btn.id == ID_RELOAD_INSTRUMENTS)
        {
            if(doneTimeout <= 0)
            {
                InstrumentLibrary.reloadInstruments(this);
            }
        }
        else if(btn.id == ID_RELOAD_TRACKS)
        {
            if(doneTimeout <= 0)
            {
                AbcLibrary.reloadTracks(this);
            }
        }
        else if(btn.id == ID_TOGGLE_TITLE)
        {
            Clef.config.showFileTitle = Clef.config.showFileTitle == 1 ? 0 : 1;
            Clef.config.save();
            Collections.sort(tracks);
        }
    }

    public void setIndex(int i)
    {
        scrollTicker = 0;
        index = i;
    }

    public boolean isSelectedIndex(int i)
    {
        return (!disableListWhenSyncTrack  || syncTrack == 0) && index == i;
    }

    public void confirmSelection(boolean doubleClick)
    {
        if(syncTrack == 0 && !(index >= 0 && index < tracks.size()))
        {
            return;
        }
        Clef.channel.sendToServer(new PacketPlayABC(index >= 0 && index < tracks.size() ? tracks.get(index).md5 : "", bandName.getText(), syncPlay == 1, syncTrack == 1));
        closeScreen();
        mc.setIngameFocus();
    }

    protected void drawTooltip(List par1List, int par2, int par3)
    {
        if (!par1List.isEmpty())
        {
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableDepth();
            int k = 0;

            for(Object aPar1List : par1List)
            {
                String s = (String)aPar1List;
                int l = this.fontRenderer.getStringWidth(s);

                if(l > k)
                {
                    k = l;
                }
            }

            int i1 = par2 + 12;
            int j1 = par3 - 12;
            int k1 = 8;

            if (par1List.size() > 1)
            {
                k1 += 2 + (par1List.size() - 1) * 10;
            }

            if (i1 + k > this.width)
            {
                i1 -= 28 + k;
            }

            if (j1 + k1 + 6 > this.height)
            {
                j1 = this.height - k1 - 6;
            }

            this.zLevel = 300.0F;
            int l1 = -267386864;
            this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
            this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
            int i2 = 1347420415;
            int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
            this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
            this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

            for (int k2 = 0; k2 < par1List.size(); ++k2)
            {
                String s1 = (String)par1List.get(k2);
                this.fontRenderer.drawStringWithShadow(s1, i1, j1, -1);

                if (k2 == 0)
                {
                    j1 += 2;
                }

                j1 += 10;
            }

            this.zLevel = 0.0F;
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();
        }
    }
}
