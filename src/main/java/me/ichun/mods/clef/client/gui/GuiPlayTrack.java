package me.ichun.mods.clef.client.gui;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.packet.PacketPlayABC;
import me.ichun.mods.clef.common.util.abc.AbcLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

public class GuiPlayTrack extends GuiScreen
{
    public static final ResourceLocation texBackground = new ResourceLocation("clef", "textures/gui/track_select.png");
    public static final int ID_CONFIRM = 0;
    public static final int ID_SYNC_PLAY = 1;
    public static final int ID_SYNC_TRACK = 2;
    public static final int ID_RELOAD_INSTRUMENTS = 20;
    public static final int ID_RELOAD_TRACKS = 21;

    protected int xSize = 256;
    protected int ySize = 230;

    protected int guiLeft;
    protected int guiTop;

    public GuiTextField bandName;

    public GuiTrackList trackList;

    public int syncPlay = 1;
    public int syncTrack = 0;

    public int index = 0;

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        buttonList.clear();

        buttonList.add(new GuiButton(ID_CONFIRM, guiLeft + 174, guiTop + 210, 83, 20, I18n.translateToLocal("clef.gui.play")));
        buttonList.add(new GuiButton(ID_SYNC_PLAY, guiLeft + 179, guiTop + 51, 72, 20, I18n.translateToLocal(syncPlay == 1 ? "gui.yes" : "gui.no")));
        buttonList.add(new GuiButton(ID_SYNC_TRACK, guiLeft + 179, guiTop + 94, 72, 20, I18n.translateToLocal(syncTrack == 1 ? "gui.yes" : "gui.no")));
        buttonList.add(new GuiButton(ID_RELOAD_INSTRUMENTS, guiLeft + 179, guiTop + 137, 20, 20, ""));
        buttonList.add(new GuiButton(ID_RELOAD_TRACKS, guiLeft + 205, guiTop + 137, 20, 20, ""));
        //        buttonList.add(new GuiButton(ID_RELOAD_TRACKS, guiLeft + 231, guiTop + 94, 20, 20, ""));

        bandName = new GuiTextField(0, mc.fontRendererObj, this.guiLeft + 181, this.guiTop + 18, 64, mc.fontRendererObj.FONT_HEIGHT);
        bandName.setMaxStringLength(15);
        bandName.setEnableBackgroundDrawing(false);
        bandName.setTextColor(16777215);
        bandName.setText("");

        trackList = new GuiTrackList(this, 158, ySize - 22, guiTop + 17, guiTop + ySize - 7, guiLeft + 7, 8, AbcLibrary.tracks);
    }

    public FontRenderer getFontRenderer()
    {
        return fontRendererObj;
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen()
    {
        bandName.updateCursorCounter();
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        super.handleMouseInput();
        if(syncTrack == 0)
        {
            this.trackList.handleMouseInput(mouseX, mouseY);
        }
    }

    @Override
    protected void keyTyped(char c, int i)
    {
        bandName.textboxKeyTyped(c, i);
        if (i == 1)
        {
            if(bandName.isFocused())
            {
                bandName.setText("");
                bandName.setFocused(false);
            }
            else
            {
                mc.displayGuiScreen(null);
                mc.setIngameFocus();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
//        initGui();
        if(mc == null)
        {
            mc = Minecraft.getMinecraft();
            fontRendererObj = mc.fontRendererObj;
        }
        drawDefaultBackground();

        GlStateManager.color(1F, 1F, 1F, 1F);
        this.mc.getTextureManager().bindTexture(texBackground);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        Gui.drawRect(guiLeft + 6, guiTop + 16, guiLeft + 166, guiTop + ySize - 6, -1072689136);

        if(bandName.getVisible())
        {
            bandName.drawTextBox();
        }

        fontRendererObj.drawString(I18n.translateToLocal("clef.gui.chooseSong"), guiLeft + 6, guiTop + 5, 16777215, true);

        fontRendererObj.drawString(I18n.translateToLocal("clef.gui.band"), guiLeft + 179, guiTop + 5, 16777215, true);

        if(bandName.getText().isEmpty() && !bandName.isFocused())
        {
            fontRendererObj.drawString(I18n.translateToLocal("clef.gui.bandSolo"), guiLeft + 182, guiTop + 18, 0xcccccc, false);
        }

        fontRendererObj.drawString(I18n.translateToLocal("clef.gui.syncPlayTime"), guiLeft + 179, guiTop + 40, 16777215, true);
        fontRendererObj.drawString(I18n.translateToLocal("clef.gui.syncTrack"), guiLeft + 179, guiTop + 83, 16777215, true);
        fontRendererObj.drawString(I18n.translateToLocal("clef.gui.reload"), guiLeft + 179, guiTop + 126, 16777215, true);

        zLevel = 10;
        zLevel = 0;

        trackList.drawScreen(mouseX, mouseY, partialTicks);

        if(syncTrack == 1)
        {
            Gui.drawRect(guiLeft + 6, guiTop + 16, guiLeft + 166, guiTop + ySize - 6, -1072689136);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) throws IOException
    {
        super.mouseClicked(x, y, btn);
        bandName.mouseClicked(x, y, btn);
    }

    @Override
    protected void actionPerformed(GuiButton btn)
    {
        if(btn.id == ID_CONFIRM)
        {
            confirmSelection();
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
        }
        else if(btn.id == ID_RELOAD_INSTRUMENTS)
        {

        }
        else if(btn.id == ID_RELOAD_TRACKS)
        {
        }
        //TODO do stuff
    }

    public void setIndex(int i)
    {
        index = i;
    }

    public boolean isSelectedIndex(int i)
    {
        return syncTrack == 0 && index == i;
    }

    public void confirmSelection()
    {
        Clef.channel.sendToServer(new PacketPlayABC(AbcLibrary.tracks.get(index).md5));
        mc.displayGuiScreen(null);
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
                int l = this.fontRendererObj.getStringWidth(s);

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
                this.fontRendererObj.drawStringWithShadow(s1, i1, j1, -1);

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
