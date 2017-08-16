package me.ichun.mods.clef.client.gui;

import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.inventory.ContainerInstrumentPlayer;
import me.ichun.mods.clef.common.packet.PacketInstrumentPlayerInfo;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class GuiPlayTrackBlock extends GuiPlayTrack
{
    public static final ResourceLocation texResourcePacks = new ResourceLocation("minecraft", "textures/gui/resource_packs.png");
    public static final ResourceLocation texSpectatorWidgets = new ResourceLocation("minecraft", "textures/gui/spectator_widgets.png");

    public static final int ID_ADD_PLAYLIST = 10;
    public static final int ID_VIEW_PLAYLIST = 11;
    public static final int ID_REPEAT = 12;
    public static final int ID_SHUFFLE = 13;
    public static final int ID_ORDER_UP = 14;
    public static final int ID_ORDER_DOWN = 15;
    public static final int ID_DELETE = 16;
    public static final int ID_VIEW_ALL = 17;

    public TileEntityInstrumentPlayer player;
    public ContainerInstrumentPlayer containerInstrumentPlayer;

    public int repeat = 0;
    public int shuffle = 0;
    public ArrayList<TrackFile> playlist;

    public boolean playlistView = false;

    public GuiPlayTrackBlock(TileEntityInstrumentPlayer player)
    {
        super();
        background = texBackgroundBlock;
        this.player = player;
        this.containerInstrumentPlayer = new ContainerInstrumentPlayer(player);

        disableListWhenSyncTrack = false;

        playlist = new ArrayList<>(player.tracks);
        bandNameString = player.bandName.isEmpty() ? Clef.config.favoriteBand : player.bandName;
        syncPlay = player.syncPlay ? 1 : 0;
        syncTrack = player.syncTrack ? 1 : 0;
        repeat = player.repeat;
        shuffle = player.shuffle ? 1 : 0;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        trackListBottom -= 22;
        trackList = new GuiTrackList(this, 158, ySize - 22, guiTop + 17, trackListBottom, guiLeft + 7, 8, playlistView ? playlist : tracks);

        this.mc.thePlayer.openContainer = this.containerInstrumentPlayer;

        for(GuiButton btn : buttonList)
        {
            if(btn.id == ID_CONFIRM)
            {
                btn.displayString = I18n.translateToLocal("gui.done");
            }
        }
    }

    @Override
    public void addButtons()
    {
        if(!playlistView)
        {
            super.addButtons();
            buttonList.add(new GuiButton(ID_ADD_PLAYLIST, guiLeft + 116 - 55, guiTop + 205, 50, 20, I18n.translateToLocal("clef.gui.block.addPlaylist")));
            buttonList.add(new GuiButton(ID_VIEW_PLAYLIST, guiLeft + 116, guiTop + 205, 50, 20, I18n.translateToLocal("clef.gui.block.viewPlaylist")));
        }
        else
        {
            buttonList.add(new GuiButton(ID_REPEAT, guiLeft + 179, guiTop + 51, 72, 20, I18n.translateToLocal(repeat == 0 ? "clef.gui.block.repeatNone" : repeat == 1 ? "clef.gui.block.repeatAll" :"clef.gui.block.repeatOne")));
            buttonList.add(new GuiButton(ID_SHUFFLE, guiLeft + 179, guiTop + 94, 72, 20, I18n.translateToLocal(shuffle == 1 ? "gui.yes" : "gui.no")));

            buttonList.add(new GuiButton(ID_VIEW_ALL, guiLeft + 116, guiTop + 205, 50, 20, I18n.translateToLocal("clef.gui.block.viewAll")));
            buttonList.add(new GuiButton(ID_ORDER_UP, guiLeft + 6, guiTop + 205, 20, 20, ""));
            buttonList.add(new GuiButton(ID_ORDER_DOWN, guiLeft + 30, guiTop + 205, 20, 20, ""));
            buttonList.add(new GuiButton(ID_DELETE, guiLeft + 54, guiTop + 205, 20, 20, ""));
        }
    }

    @Override
    public void drawText()
    {
        if(!playlistView)
        {
            fontRendererObj.drawString(I18n.translateToLocal("clef.gui.block.playlist") + ":", guiLeft + 9, guiTop + 211, 16777215, true);
            super.drawText();
        }
        else
        {
            fontRendererObj.drawString(I18n.translateToLocal("clef.gui.block.playlist") + " (" + playlist.size() + ")", guiLeft + 6, guiTop + 5, 16777215, true);
            fontRendererObj.drawString(I18n.translateToLocal("clef.gui.block.repeat"), guiLeft + 179, guiTop + 40, 16777215, true);
            fontRendererObj.drawString(I18n.translateToLocal("clef.gui.block.shuffle"), guiLeft + 179, guiTop + 83, 16777215, true);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)guiLeft, (float)guiTop, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i1 = 0; i1 < this.containerInstrumentPlayer.inventorySlots.size(); i1++)
        {
            Slot slot = this.containerInstrumentPlayer.inventorySlots.get(i1);
            this.drawSlot(slot);

            if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.canBeHovered())
            {
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                int j1 = slot.xDisplayPosition;
                int k1 = slot.yDisplayPosition;
                GlStateManager.colorMask(true, true, true, false);
                this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();

    }

    private Slot getSlotAtPosition(int x, int y)
    {
        for (int i = 0; i < this.containerInstrumentPlayer.inventorySlots.size(); ++i)
        {
            Slot slot = this.containerInstrumentPlayer.inventorySlots.get(i);

            if (this.isMouseOverSlot(slot, x, y))
            {
                return slot;
            }
        }

        return null;
    }

    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        this.mc.playerController.windowClick(this.containerInstrumentPlayer.windowId, slotId, mouseButton, type, this.mc.thePlayer);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 || mouseButton == 1)
        {
            Slot slot = this.getSlotAtPosition(mouseX, mouseY);
            if(slot != null)
            {
                handleMouseClick(slot, slot.slotNumber, mouseButton, ClickType.PICKUP);
            }
        }
    }

    @Override
    public void addReloadButtons(){}

    @Override
    public void drawReloadButtons()
    {
        if(playlistView)
        {
            GlStateManager.color(1F, 1F, 1F, 1F);
            this.mc.getTextureManager().bindTexture(texResourcePacks);
            this.drawTexturedModalRect(guiLeft - 8, guiTop + 206, 96, 0, 32, 32);
            this.drawTexturedModalRect(guiLeft + 32, guiTop + 191, 80, 0, 32, 32);

            this.mc.getTextureManager().bindTexture(texSpectatorWidgets);
            this.drawTexturedModalRect(guiLeft + 40, guiTop + 207, 112, 0, 32, 32);
        }
    }

    @Override
    protected void actionPerformed(GuiButton btn)
    {
        super.actionPerformed(btn);
        if(btn.id == ID_ADD_PLAYLIST)
        {
            if(index >= 0 && index < tracks.size())
            {
                if(!playlist.contains(tracks.get(index)))
                {
                    playlist.add(tracks.get(index));
                }
            }
        }
        else if((btn.id == ID_VIEW_PLAYLIST || btn.id == ID_VIEW_ALL) && doneTimeout < 0)
        {
            togglePlaylistView();
        }
        else if(btn.id == ID_REPEAT)
        {
            repeat++;
            if(repeat > 2)
            {
                repeat = 0;
            }
            btn.displayString = I18n.translateToLocal(repeat == 0 ? "clef.gui.block.repeatNone" : repeat == 1 ? "clef.gui.block.repeatAll" :"clef.gui.block.repeatOne");
        }
        else if(btn.id == ID_SHUFFLE)
        {
            shuffle = shuffle == 1 ? 0 : 1;
            btn.displayString = I18n.translateToLocal(shuffle == 1 ? "gui.yes" : "gui.no");
        }
        else if(index >= 0 && index < playlist.size())
        {
            if(btn.id == ID_ORDER_UP)
            {
                if(index > 0)
                {
                    TrackFile file = playlist.get(index);
                    playlist.remove(index);
                    playlist.add(index - 1, file);
                    index--;
                }
            }
            else if(btn.id == ID_ORDER_DOWN)
            {
                if(index < playlist.size() - 1)
                {
                    TrackFile file = playlist.get(index);
                    playlist.remove(index);
                    playlist.add(index + 1, file);
                    index++;
                }
            }
            else if(btn.id == ID_DELETE)
            {
                playlist.remove(index);
                if(playlist.size() <= index)
                {
                    index = playlist.size() - 1;
                }
            }
        }
    }

    public void togglePlaylistView()
    {
        doneTimeout = 2;
        index = -1;
        playlistView = !playlistView;
        initGui();
        trackList.tracks = playlistView ? playlist : tracks;
    }

    @Override
    public void confirmSelection(boolean doubleClick)
    {
        if(doubleClick)
        {
            if(!playlistView && index >= 0 && index < tracks.size() && !playlist.contains(tracks.get(index)))
            {
                playlist.add(tracks.get(index));
            }
            else if(playlistView && index >= 0 && index < playlist.size() && !playlist.isEmpty())
            {
                playlist.remove(index);
                if(index >= playlist.size())
                {
                    index = playlist.size() - 1;
                }
            }
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return;
        }
        if(playlist.isEmpty())
        {
            if(playlistView)
            {
                return;
            }
            else if(index >= 0 && index < tracks.size() && !playlist.contains(tracks.get(index)))
            {
                playlist.add(tracks.get(index));
            }
        }
        ArrayList<String> md5s = new ArrayList<>();
        for(TrackFile track : playlist)
        {
            md5s.add(track.md5);
        }
        Clef.channel.sendToServer(new PacketInstrumentPlayerInfo(md5s, bandName.getText(), syncPlay == 1, syncTrack == 1, repeat, shuffle == 1, player.getPos()));
        closeScreen();
        mc.setIngameFocus();
    }

    @Override
    public void closeScreen()
    {
        mc.thePlayer.closeScreen();
    }

    private boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY)
    {
        return this.isPointInRegion(slotIn.xDisplayPosition, slotIn.yDisplayPosition, 16, 16, mouseX, mouseY);
    }

    protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY)
    {
        int i = this.guiLeft;
        int j = this.guiTop;
        pointX = pointX - i;
        pointY = pointY - j;
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }

    private void drawSlot(Slot slotIn)
    {
        int i = slotIn.xDisplayPosition;
        int j = slotIn.yDisplayPosition;
        ItemStack itemstack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = false;
        String s = null;

        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;

        if (itemstack == null && slotIn.canBeHovered())
        {
            TextureAtlasSprite textureatlassprite = slotIn.getBackgroundSprite();

            if (textureatlassprite != null)
            {
                GlStateManager.disableLighting();
                this.mc.getTextureManager().bindTexture(slotIn.getBackgroundLocation());
                this.drawTexturedModalRect(i, j, textureatlassprite, 16, 16);
                GlStateManager.enableLighting();
                flag1 = true;
            }
        }

        if (!flag1)
        {
            if (flag)
            {
                drawRect(i, j, i + 16, j + 16, -2130706433);
            }

            GlStateManager.enableDepth();
            this.itemRender.renderItemAndEffectIntoGUI(this.mc.thePlayer, itemstack, i, j);
            this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, itemstack, i, j, s);
        }

        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }
}
