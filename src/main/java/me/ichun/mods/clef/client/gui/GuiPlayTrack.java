package me.ichun.mods.clef.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public List<TrackFile> tracks;
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
        tracks = new ArrayList<>(AbcLibrary.getTracks());
        bandNameString = Clef.configClient.favoriteBand;
    }

    @Override
    public void init()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(true);

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        trackListBottom = guiTop + ySize - 6;

        bandName = new TextFieldWidget(font, this.guiLeft + 181, this.guiTop + 18, 64, font.FONT_HEIGHT, new TranslationTextComponent("clef.gui.band"));
        bandName.setMaxStringLength(15);
        bandName.setEnableBackgroundDrawing(false);
        bandName.setTextColor(16777215);
        bandName.setText(bandNameString);
        this.children.add(bandName);

        this.addButton(buttonConfirm = new Button(guiLeft + 174, guiTop + 210, 83, 20, new TranslationTextComponent("clef.gui.play"), btn -> confirmSelection(false)));
        addButtons();

        buttonSyncPlay.active = !bandName.getText().isEmpty() && syncTrack == 0;
        buttonSyncPlay.setMessage(new TranslationTextComponent(syncPlay == 1 ? "gui.yes" : "gui.no"));
        buttonSyncTrack.active = !bandName.getText().isEmpty();
        buttonSyncTrack.setMessage(new TranslationTextComponent(syncTrack == 1 ? "gui.yes" : "gui.no"));

        trackList = new GuiTrackList(this, 158, ySize - 22, guiTop + 17, trackListBottom, guiLeft + 7, 8, tracks);
        this.children.add(trackList);
    }

    public void addButtons()
    {
        //sync play button
        this.addButton(buttonSyncPlay = new Button(guiLeft + 179, guiTop + 51, 72, 20, new TranslationTextComponent(syncPlay == 1 ? "gui.yes" : "gui.no"), btn -> {
            syncPlay = syncPlay == 1 ? 0 : 1;
            btn.setMessage(new TranslationTextComponent(syncPlay == 1 ? "gui.yes" : "gui.no"));
        }));

        //sync track button
        this.addButton(buttonSyncTrack = new Button(guiLeft + 179, guiTop + 94, 72, 20, new TranslationTextComponent(syncTrack == 1 ? "gui.yes" : "gui.no"), btn -> {
            syncTrack = syncTrack == 1 ? 0 : 1;
            btn.setMessage(new TranslationTextComponent(syncTrack == 1 ? "gui.yes" : "gui.no"));

            buttonSyncPlay.active = syncTrack == 0;
            if(!buttonSyncPlay.active)
            {
                syncPlay = 1;
                buttonSyncPlay.setMessage(new TranslationTextComponent("gui.yes"));
            }
        }));
        addReloadButtons();
    }

    public void addReloadButtons() //TODO switch these to imagebuttons
    {
        //reload instruments
        this.addButton(new Button(guiLeft + 179, guiTop + 137, 20, 20, StringTextComponent.EMPTY, btn -> {
            if(doneTimeout <= 0)
            {
                InstrumentLibrary.reloadInstruments(this);
            }
        }));
        //reload tracks
        this.addButton(new Button(guiLeft + 205, guiTop + 137, 20, 20, StringTextComponent.EMPTY, btn -> {
            if(doneTimeout <= 0)
            {
                AbcLibrary.reloadTracks(this);
            }
        }));
        //toggle title
        this.addButton(new Button(guiLeft + 231, guiTop + 137, 20, 20, StringTextComponent.EMPTY, btn -> {
            Clef.configCommon.showFileTitle = !Clef.configCommon.showFileTitle;
            Clef.configCommon.save();
            Collections.sort(tracks);
        }));
    }

    @Override
    public void onClose()
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
                buttonSyncPlay.setMessage(new TranslationTextComponent(syncPlay == 1 ? "gui.yes" : "gui.no"));
                buttonSyncTrack.setMessage(new TranslationTextComponent(syncTrack == 1 ? "gui.yes" : "gui.no"));
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
        buttonSyncPlay.setMessage(new TranslationTextComponent(syncPlay == 1 ? "gui.yes" : "gui.no"));
        buttonSyncTrack.active = !bandName.getText().isEmpty();
        buttonSyncTrack.setMessage(new TranslationTextComponent(syncTrack == 1 ? "gui.yes" : "gui.no"));
        bandNameString = bandName.getText();

        return flag;
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return !bandName.isFocused();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground(stack);

        RenderSystem.color4f(1F, 1F, 1F, 1F);
        this.minecraft.getTextureManager().bindTexture(background);
        this.blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize);

        AbstractGui.fill(stack, guiLeft + 6, guiTop + 16, guiLeft + 166, trackListBottom + 1, -1072689136);
        AbstractGui.fill(stack, guiLeft + 6 + 1, guiTop + 16 + 1, guiLeft + 166 - 1, trackListBottom, 0xff1a1a1a);

        if(bandName.getVisible())
        {
            bandName.render(stack, mouseX, mouseY, partialTicks);
        }

        font.drawStringWithShadow(stack, I18n.format("clef.gui.band"), guiLeft + 179, guiTop + 5, 16777215);

        if(bandName.getText().isEmpty() && !bandName.isFocused())
        {
            font.drawString(stack, I18n.format("clef.gui.bandSolo"), guiLeft + 182, guiTop + 18, 0xcccccc);
        }

        drawText(stack);

        trackList.render(stack, mouseX, mouseY, partialTicks);

        super.render(stack, mouseX, mouseY, partialTicks);

        drawReloadButtons(stack);

        if(syncTrack == 1 && disableListWhenSyncTrack && !bandName.getText().isEmpty())
        {
            AbstractGui.fill(stack, guiLeft + 6, guiTop + 16, guiLeft + 166, trackListBottom + 1, -1072689136);
        }
    }

    public void drawText(MatrixStack stack)
    {
        font.drawStringWithShadow(stack, I18n.format("clef.gui.chooseSong") + " (" + tracks.size() + ")", guiLeft + 6, guiTop + 5, 16777215);
        font.drawStringWithShadow(stack, I18n.format("clef.gui.syncPlayTime"), guiLeft + 179, guiTop + 40, 16777215);
        font.drawStringWithShadow(stack, I18n.format("clef.gui.syncTrack"), guiLeft + 179, guiTop + 83, 16777215);
        stack.push();
        int length = font.getStringWidth(I18n.format("clef.gui.moreSongs"));
        stack.translate(guiLeft - 4, guiTop + length + 3, 0);
        stack.scale(0.5F, 0.5F, 1F);
        stack.rotate(Vector3f.ZP.rotationDegrees(-90F));
        font.drawStringWithShadow(stack, I18n.format("clef.gui.moreSongs"), 0, 0, 16777215);
        stack.pop();
    }

    public void closeScreen()
    {
        minecraft.displayGuiScreen(null);
    }

    public void drawReloadButtons(MatrixStack stack)
    {
        disableListWhenSyncTrack = true;
        if(!bandName.getText().isEmpty())
        {
            disableListWhenSyncTrack = Clef.eventHandlerClient.findTrackByBand(bandName.getText()) != null;
        }

        font.drawStringWithShadow(stack, I18n.format("clef.gui.reload"), guiLeft + 179, guiTop + 126, 16777215);
        if(doneTimeout > 0)
        {
            font.drawStringWithShadow(stack, I18n.format("gui.done"), guiLeft + 179 + 2 + font.getStringWidth(I18n.format("clef.gui.reload")), guiTop + 126, 16777215);
        }

        RenderSystem.color4f(1F, 1F, 1F, 1F); //reset the colors.

        RenderSystem.enableAlphaTest();
        this.minecraft.getTextureManager().bindTexture(texInstrument);
        float x = guiLeft + 179 + 2;
        float y = guiTop + 137 + 2;
        Matrix4f matrix4f = stack.getLast().getMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(matrix4f, x + 0, y + 16, this.getBlitOffset()).tex(0F, 1F).endVertex();
        bufferbuilder.pos(matrix4f, x + 16, y + 16, this.getBlitOffset()).tex(1F, 1F).endVertex();
        bufferbuilder.pos(matrix4f, x + 16, y + 0, this.getBlitOffset()).tex(1F, 0F).endVertex();
        bufferbuilder.pos(matrix4f, x + 0, y + 0, this.getBlitOffset()).tex(0F, 0F).endVertex();
        tessellator.draw();

        this.minecraft.getTextureManager().bindTexture(texIcons);
        this.blit(stack, guiLeft + 236, guiTop + 142, 0, 224, 10, 10);

        this.minecraft.getTextureManager().bindTexture(texNote);
        x = guiLeft + 205 + 2;
        y = guiTop + 137 + 2;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        bufferbuilder.pos(matrix4f, x + 0, y + 16, this.getBlitOffset()).color(0.5F, 1F, 1F, 1F).tex(0F, 1F).endVertex();
        bufferbuilder.pos(matrix4f, x + 16, y + 16, this.getBlitOffset()).color(0.5F, 1F, 1F, 1F).tex(1F, 1F).endVertex();
        bufferbuilder.pos(matrix4f, x + 16, y + 0, this.getBlitOffset()).color(0.5F, 1F, 1F, 1F).tex(1F, 0F).endVertex();
        bufferbuilder.pos(matrix4f, x + 0, y + 0, this.getBlitOffset()).color(0.5F, 1F, 1F, 1F).tex(0F, 0F).endVertex();
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
                        buttonSyncPlay.setMessage(new TranslationTextComponent(syncPlay == 1 ? "gui.yes" : "gui.no"));
                        buttonSyncTrack.active = true;
                        buttonSyncTrack.setMessage(new TranslationTextComponent(syncTrack == 1 ? "gui.yes" : "gui.no"));
                        bandIndex++;
                    }
                }
                else
                {
                    bandName.setText("");
                    buttonSyncPlay.active = false;
                    buttonSyncPlay.setMessage(new TranslationTextComponent(syncPlay == 1 ? "gui.yes" : "gui.no"));
                    buttonSyncTrack.active = false;
                    buttonSyncTrack.setMessage(new TranslationTextComponent(syncTrack == 1 ? "gui.yes" : "gui.no"));
                }
                bandName.setFocused2(false);
                setListener(null);
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
