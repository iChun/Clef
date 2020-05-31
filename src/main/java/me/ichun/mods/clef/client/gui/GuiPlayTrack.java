package me.ichun.mods.clef.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.packet.PacketPlayABC;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import me.ichun.mods.clef.common.util.abc.play.Track;
import me.ichun.mods.clef.common.util.instrument.InstrumentLibrary;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings("deprecation")
public class GuiPlayTrack extends Screen
{
    public static final ResourceLocation texBackground = new ResourceLocation("clef", "textures/gui/track_select.png");
    public static final ResourceLocation texBackgroundBlock = new ResourceLocation("clef", "textures/gui/track_select_block.png");
    public static final ResourceLocation texInstrument = new ResourceLocation("clef", "textures/items/instrument.png");
    public static final ResourceLocation texNote = new ResourceLocation("minecraft", "textures/particle/note.png");
    public static final ResourceLocation texIcons = new ResourceLocation("minecraft", "textures/gui/icons.png");

    public ResourceLocation background = texBackground;

    protected int xSize = 256;
    protected int ySize = 230;

    protected int guiLeft;
    protected int guiTop;

    public TextFieldWidget bandName;

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

    public Button buttonConfirm = null;
    public Button buttonSyncPlay = null;
    public Button buttonSyncTrack = null;

    public GuiPlayTrack()
    {
        super(new TranslationTextComponent("clef.gui.chooser"));
        tracks = AbcLibrary.tracks;
        bandNameString = Clef.configClient.favoriteBand;
    }

    @Override
    public void init()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(true);

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        trackListBottom = guiTop + ySize - 6;

        bandName = new TextFieldWidget(font, this.guiLeft + 181, this.guiTop + 18, 64, font.FONT_HEIGHT, I18n.format("clef.gui.band"));
        bandName.setMaxStringLength(15);
        bandName.setEnableBackgroundDrawing(false);
        bandName.setTextColor(16777215);
        bandName.setText(bandNameString);
        this.children.add(bandName);

        this.addButton(buttonConfirm = new Button(guiLeft + 174, guiTop + 210, 83, 20, I18n.format("clef.gui.play"), btn -> confirmSelection(false)));
        addButtons();

        buttonSyncPlay.active = !bandName.getText().isEmpty() && syncTrack == 0;
        buttonSyncPlay.setMessage(I18n.format(syncPlay == 1 ? "gui.yes" : "gui.no"));
        buttonSyncTrack.active = !bandName.getText().isEmpty();
        buttonSyncTrack.setMessage(I18n.format(syncTrack == 1 ? "gui.yes" : "gui.no"));

        trackList = new GuiTrackList(this, 158, ySize - 22, guiTop + 17, trackListBottom, guiLeft + 7, 8, tracks);
        this.children.add(trackList);
    }

    public void addButtons()
    {
        //sync play button
        this.addButton(buttonSyncPlay = new Button(guiLeft + 179, guiTop + 51, 72, 20, I18n.format(syncPlay == 1 ? "gui.yes" : "gui.no"), btn -> {
            syncPlay = syncPlay == 1 ? 0 : 1;
            btn.setMessage(I18n.format(syncPlay == 1 ? "gui.yes" : "gui.no"));
        }));

        //sync track button
        this.addButton(buttonSyncTrack = new Button(guiLeft + 179, guiTop + 94, 72, 20, I18n.format(syncTrack == 1 ? "gui.yes" : "gui.no"), btn -> {
            syncTrack = syncTrack == 1 ? 0 : 1;
            btn.setMessage(I18n.format(syncTrack == 1 ? "gui.yes" : "gui.no"));

            buttonSyncPlay.active = syncTrack == 0;
            if(!buttonSyncPlay.active)
            {
                syncPlay = 1;
                buttonSyncPlay.setMessage(I18n.format("gui.yes"));
            }
        }));
        addReloadButtons();
    }

    public void addReloadButtons() //TODO switch these to imagebuttons
    {
        //reload instruments
        this.addButton(new Button(guiLeft + 179, guiTop + 137, 20, 20, "", btn -> {
            if(doneTimeout <= 0)
            {
                InstrumentLibrary.reloadInstruments(this);
            }
        }));
        //reload tracks
        this.addButton(new Button(guiLeft + 205, guiTop + 137, 20, 20, "", btn -> {
            if(doneTimeout <= 0)
            {
                AbcLibrary.reloadTracks(this);
            }
        }));
        //toggle title
        this.addButton(new Button(guiLeft + 231, guiTop + 137, 20, 20, "", btn -> {
            Clef.configClient.showFileTitle = !Clef.configClient.showFileTitle;
            Clef.configClient.save();
            Collections.sort(tracks);
        }));
    }

    @Override
    public void removed()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public void tick()
    {
        scrollTicker++;
        doneTimeout--;
        bandName.tick();
        bandNameString = bandName.getText();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        boolean flag = super.keyPressed(keyCode, scanCode, modifiers);
        if(bandName.isFocused())
        {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE)
            {
                syncPlay = 1;
                syncTrack = 1;
                buttonSyncPlay.active = false;
                buttonSyncPlay.setMessage(I18n.format(syncPlay == 1 ? "gui.yes" : "gui.no"));
                buttonSyncTrack.setMessage(I18n.format(syncTrack == 1 ? "gui.yes" : "gui.no"));
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE)
        {
            if(bandName.isFocused())
            {
                bandName.setText("");
                bandName.setFocused2(false);
            }
            else
            {
                closeScreen();
                this.minecraft.mouseHelper.grabMouse();
            }
        }
        buttonSyncPlay.active = !bandName.getText().isEmpty() && syncTrack == 0;
        buttonSyncPlay.setMessage(I18n.format(syncPlay == 1 ? "gui.yes" : "gui.no"));
        buttonSyncTrack.active = !bandName.getText().isEmpty();
        buttonSyncTrack.setMessage(I18n.format(syncTrack == 1 ? "gui.yes" : "gui.no"));
        bandNameString = bandName.getText();

        return flag;
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return !bandName.isFocused();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground();

        RenderSystem.color4f(1F, 1F, 1F, 1F);
        this.minecraft.getTextureManager().bindTexture(background);
        this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);

        AbstractGui.fill(guiLeft + 6, guiTop + 16, guiLeft + 166, trackListBottom + 1, -1072689136);
        AbstractGui.fill(guiLeft + 6 + 1, guiTop + 16 + 1, guiLeft + 166 - 1, trackListBottom, 0xff1a1a1a);

        if(bandName.getVisible())
        {
            bandName.render(mouseX, mouseY, partialTicks);
        }

        font.drawStringWithShadow(I18n.format("clef.gui.band"), guiLeft + 179, guiTop + 5, 16777215);

        if(bandName.getText().isEmpty() && !bandName.isFocused())
        {
            font.drawString(I18n.format("clef.gui.bandSolo"), guiLeft + 182, guiTop + 18, 0xcccccc);
        }

        drawText();

        trackList.render(mouseX, mouseY, partialTicks);

        super.render(mouseX, mouseY, partialTicks);

        drawReloadButtons();

        if(syncTrack == 1 && disableListWhenSyncTrack && !bandName.getText().isEmpty())
        {
            AbstractGui.fill(guiLeft + 6, guiTop + 16, guiLeft + 166, trackListBottom + 1, -1072689136);
        }
    }

    public void drawText()
    {
        font.drawStringWithShadow(I18n.format("clef.gui.chooseSong") + " (" + tracks.size() + ")", guiLeft + 6, guiTop + 5, 16777215);
        font.drawStringWithShadow(I18n.format("clef.gui.syncPlayTime"), guiLeft + 179, guiTop + 40, 16777215);
        font.drawStringWithShadow(I18n.format("clef.gui.syncTrack"), guiLeft + 179, guiTop + 83, 16777215);
        RenderSystem.pushMatrix();
        int length = font.getStringWidth(I18n.format("clef.gui.moreSongs"));
        RenderSystem.translatef(guiLeft - 4, guiTop + length + 3, 0);
        RenderSystem.scalef(0.5F, 0.5F, 1F);
        RenderSystem.rotatef(-90F, 0F, 0F, 1F);
        font.drawStringWithShadow(I18n.format("clef.gui.moreSongs"), 0, 0, 16777215);
        RenderSystem.popMatrix();
    }

    public void closeScreen()
    {
        minecraft.displayGuiScreen(null);
    }

    public void drawReloadButtons()
    {
        disableListWhenSyncTrack = true;
        if(!bandName.getText().isEmpty())
        {
            disableListWhenSyncTrack = Clef.eventHandlerClient.findTrackByBand(bandName.getText()) != null;
        }

        font.drawStringWithShadow(I18n.format("clef.gui.reload"), guiLeft + 179, guiTop + 126, 16777215);
        if(doneTimeout > 0)
        {
            font.drawStringWithShadow(I18n.format("gui.done"), guiLeft + 179 + 2 + font.getStringWidth(I18n.format("clef.gui.reload")), guiTop + 126, 16777215);
        }

        RenderSystem.color4f(1F, 1F, 1F, 1F); //reset the colors.

        RenderSystem.enableAlphaTest();
        this.minecraft.getTextureManager().bindTexture(texInstrument);
        float x = guiLeft + 179 + 2;
        float y = guiTop + 137 + 2;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)(x + 0), (double)(y + 16), (double)this.getBlitOffset()).tex(0F, 1F).endVertex();
        bufferbuilder.pos((double)(x + 16), (double)(y + 16), (double)this.getBlitOffset()).tex(1F, 1F).endVertex();
        bufferbuilder.pos((double)(x + 16), (double)(y + 0), (double)this.getBlitOffset()).tex(1F, 0F).endVertex();
        bufferbuilder.pos((double)(x + 0), (double)(y + 0), (double)this.getBlitOffset()).tex(0F, 0F).endVertex();
        tessellator.draw();

        this.minecraft.getTextureManager().bindTexture(texIcons);
        this.blit(guiLeft + 236, guiTop + 142, 0, 224, 10, 10);

        this.minecraft.getTextureManager().bindTexture(texNote);
        x = guiLeft + 205 + 2;
        y = guiTop + 137 + 2;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        bufferbuilder.pos((double)(x + 0), (double)(y + 16), (double)this.getBlitOffset()).color(0.5F, 1F, 1F, 1F).tex(0F, 1F).endVertex();
        bufferbuilder.pos((double)(x + 16), (double)(y + 16), (double)this.getBlitOffset()).color(0.5F, 1F, 1F, 1F).tex(1F, 1F).endVertex();
        bufferbuilder.pos((double)(x + 16), (double)(y + 0), (double)this.getBlitOffset()).color(0.5F, 1F, 1F, 1F).tex(1F, 0F).endVertex();
        bufferbuilder.pos((double)(x + 0), (double)(y + 0), (double)this.getBlitOffset()).color(0.5F, 1F, 1F, 1F).tex(0F, 0F).endVertex();
        tessellator.draw();

        RenderSystem.disableAlphaTest();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int btn)
    {
        boolean superResult = super.mouseClicked(mouseX, mouseY, btn);
        if(btn == GLFW.GLFW_MOUSE_BUTTON_RIGHT) //RMB
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
                        buttonSyncPlay.active = false;
                        buttonSyncPlay.setMessage(I18n.format(syncPlay == 1 ? "gui.yes" : "gui.no"));
                        buttonSyncTrack.active = true;
                        buttonSyncTrack.setMessage(I18n.format(syncTrack == 1 ? "gui.yes" : "gui.no"));
                        bandIndex++;
                    }
                }
                else
                {
                    bandName.setText("");
                    buttonSyncPlay.active = false;
                    buttonSyncPlay.setMessage(I18n.format(syncPlay == 1 ? "gui.yes" : "gui.no"));
                    buttonSyncTrack.active = false;
                    buttonSyncTrack.setMessage(I18n.format(syncTrack == 1 ? "gui.yes" : "gui.no"));
                }
                bandName.setFocused2(false);
                setFocused(null);
            }
        }
        return superResult;
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    public void setIndex(int i)
    {
        scrollTicker = 0;
        index = i;
    }

    public boolean isSelectedIndex(int i)
    {
        return (bandName.getText().isEmpty() || !disableListWhenSyncTrack  || syncTrack == 0) && index == i;
    }

    public void confirmSelection(boolean doubleClick)
    {
        if(syncTrack == 0 && !(index >= 0 && index < tracks.size()))
        {
            return;
        }
        Clef.channel.sendToServer(new PacketPlayABC(index >= 0 && index < tracks.size() ? tracks.get(index).md5 : "", bandName.getText(), syncPlay == 1, syncTrack == 1));
        closeScreen();
        this.minecraft.mouseHelper.grabMouse();
    }

    public FontRenderer getFontRenderer()
    {
        return font;
    }
}
