package me.ichun.mods.clef.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import me.ichun.mods.clef.common.Clef;
import me.ichun.mods.clef.common.inventory.ContainerInstrumentPlayer;
import me.ichun.mods.clef.common.packet.PacketInstrumentPlayerInfo;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import me.ichun.mods.clef.common.util.abc.TrackFile;
import me.ichun.mods.ichunutil.client.core.ResourceHelper;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL13;

import java.util.ArrayList;

public class GuiPlayTrackBlock extends GuiPlayTrack
        implements IHasContainer<ContainerInstrumentPlayer>
{
    public final TileEntityInstrumentPlayer player;
    public final ContainerInstrumentPlayer containerInstrumentPlayer;

    protected Slot hoveredSlot;

    public int repeat = 0;
    public int shuffle = 0;
    public ArrayList<TrackFile> playlist;

    public boolean playlistView = false;

    public GuiPlayTrackBlock(ContainerInstrumentPlayer container)
    {
        super();
        background = texBackgroundBlock;
        this.containerInstrumentPlayer = container;
        this.player = container.inventory;

        disableListWhenSyncTrack = false;

        playlist = new ArrayList<>(player.tracks);
        bandNameString = player.bandName.isEmpty() ? Clef.configClient.favoriteBand : player.bandName;
        syncPlay = player.syncPlay ? 1 : 0;
        syncTrack = player.syncTrack ? 1 : 0;
        repeat = player.repeat;
        shuffle = player.shuffle ? 1 : 0;
    }

    @Override
    public void init()
    {
        super.init();
        this.children.remove(trackList);
        trackListBottom -= 22;
        trackList = new GuiTrackList(this, 158, ySize - 22, guiTop + 17, trackListBottom, guiLeft + 7, 8, playlistView ? playlist : tracks);
        this.children.add(trackList);

        buttonConfirm.setMessage(new TranslationTextComponent("gui.done"));
    }

    @Override
    public void addButtons()
    {
        if(!playlistView)
        {
            super.addButtons();

            //add playlist
            this.addButton(new Button(guiLeft + 116 - 55, guiTop + 205, 50, 20, new TranslationTextComponent("clef.gui.block.addPlaylist"), btn -> {
                if(index >= 0 && index < tracks.size())
                {
                    if(!playlist.contains(tracks.get(index)))
                    {
                        playlist.add(tracks.get(index));
                    }
                }
            }));

            //view playlist
            this.addButton(new Button(guiLeft + 116, guiTop + 205, 50, 20, new TranslationTextComponent("clef.gui.block.viewPlaylist"), btn -> {
                if(doneTimeout < 0)
                {
                    togglePlaylistView();
                }
            }));
        }
        else
        {
            //repeat song
            this.addButton(new Button(guiLeft + 179, guiTop + 51, 72, 20, new TranslationTextComponent(repeat == 0 ? "clef.gui.block.repeatNone" : repeat == 1 ? "clef.gui.block.repeatAll" :"clef.gui.block.repeatOne"), btn -> {
                repeat++;
                if(repeat > 2)
                {
                    repeat = 0;
                }
                btn.setMessage(new TranslationTextComponent(repeat == 0 ? "clef.gui.block.repeatNone" : repeat == 1 ? "clef.gui.block.repeatAll" :"clef.gui.block.repeatOne"));
            }));

            //shuffle songs
            this.addButton(new Button(guiLeft + 179, guiTop + 94, 72, 20, new TranslationTextComponent(shuffle == 1 ? "gui.yes" : "gui.no"), btn -> {
                shuffle = shuffle == 1 ? 0 : 1;
                btn.setMessage(new TranslationTextComponent(shuffle == 1 ? "gui.yes" : "gui.no"));
            }));

            //view all
            this.addButton(new Button(guiLeft + 116, guiTop + 205, 50, 20, new TranslationTextComponent("clef.gui.block.viewAll"), btn -> {
                if(doneTimeout < 0)
                {
                    togglePlaylistView();
                }
            }));

            //order up
            this.addButton(new Button(guiLeft + 6, guiTop + 205, 20, 20, StringTextComponent.EMPTY, btn -> {
                if(index >= 0 && index < playlist.size())
                {
                    if (index > 0)
                    {
                        TrackFile file = playlist.get(index);
                        playlist.remove(index);
                        playlist.add(index - 1, file);
                        index--;
                    }
                }
                trackList.setTracks(playlist);
            }));

            //order down
            this.addButton(new Button(guiLeft + 30, guiTop + 205, 20, 20, StringTextComponent.EMPTY, btn -> {
                if(index >= 0 && index < playlist.size())
                {
                    if(index < playlist.size() - 1)
                    {
                        TrackFile file = playlist.get(index);
                        playlist.remove(index);
                        playlist.add(index + 1, file);
                        index++;
                    }
                }
                trackList.setTracks(playlist);
            }));

            //delete
            this.addButton(new Button(guiLeft + 54, guiTop + 205, 20, 20, StringTextComponent.EMPTY, btn -> {
                if(index >= 0 && index < playlist.size())
                {
                    playlist.remove(index);
                    if(playlist.size() <= index)
                    {
                        index = playlist.size() - 1;
                    }
                }
                trackList.setTracks(playlist);
            }));
        }
    }

    @Override
    public void drawText(MatrixStack stack)
    {
        if(!playlistView)
        {
            font.drawStringWithShadow(stack, I18n.format("clef.gui.block.playlist") + ":", guiLeft + 9, guiTop + 211, 16777215);
            super.drawText(stack);
        }
        else
        {
            font.drawStringWithShadow(stack, I18n.format("clef.gui.block.playlist") + " (" + playlist.size() + ")", guiLeft + 6, guiTop + 5, 16777215);
            font.drawStringWithShadow(stack, I18n.format("clef.gui.block.repeat"), guiLeft + 179, guiTop + 40, 16777215);
            font.drawStringWithShadow(stack, I18n.format("clef.gui.block.shuffle"), guiLeft + 179, guiTop + 83, 16777215);
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) //a lot taken from Container Screen
    {
        super.render(stack, mouseX, mouseY, partialTicks);

        stack.push();
        RenderSystem.translatef((float)guiLeft, (float)guiTop, 0.0F); //matrix stack not taken into account by all vanilla methods rn, so we need the RenderSystem (see ContainerScreen)
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableRescaleNormal();
        this.hoveredSlot = null;

        RenderSystem.glMultiTexCoord2f(GL13.GL_TEXTURE2, 240.0F, 240.0F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i1 = 0; i1 < this.containerInstrumentPlayer.inventorySlots.size(); i1++)
        {
            Slot slot = this.containerInstrumentPlayer.inventorySlots.get(i1);
            this.drawSlot(stack, slot);

            if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.isEnabled())
            {
                this.hoveredSlot = slot;
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                int j1 = slot.xPos;
                int k1 = slot.yPos;
                RenderSystem.colorMask(true, true, true, false);
                this.fillGradient(stack, j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.enableLighting();
                RenderSystem.enableDepthTest();
            }
        }

        stack.pop();

        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        this.renderHoveredToolTip(stack, mouseX, mouseY);
        RenderSystem.enableDepthTest();
    }

    protected void renderHoveredToolTip(MatrixStack stack, int mouseX, int mouseY) {
        if (this.hoveredSlot != null && this.hoveredSlot.getHasStack()) {
            this.renderTooltip(stack, this.hoveredSlot.getStack(), mouseX, mouseY);
        }

    }

    private Slot getSlotAtPosition(double x, double y)
    {
        for (int i = 0; i < this.containerInstrumentPlayer.inventorySlots.size(); ++i)
        {
            Slot slot = this.containerInstrumentPlayer.inventorySlots.get(i);

            if (this.isMouseOverSlot(slot, x, y) && slot.isEnabled())
            {
                return slot;
            }
        }

        return null;
    }

    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        this.minecraft.playerController.windowClick(this.containerInstrumentPlayer.windowId, slotId, mouseButton, type, this.minecraft.player);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        boolean flag = super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 || mouseButton == 1)
        {
            Slot slot = this.getSlotAtPosition(mouseX, mouseY);
            if(slot != null)
            {
                handleMouseClick(slot, slot.slotNumber, mouseButton, ClickType.PICKUP);
                return true;
            }
        }
        return flag;
    }

    @Override
    public void addReloadButtons(){}

    @Override
    public void drawReloadButtons(MatrixStack stack)
    {
        if(playlistView)
        {
            RenderSystem.color4f(1F, 1F, 1F, 1F);
            this.minecraft.getTextureManager().bindTexture(ResourceHelper.TEX_RESOURCE_PACKS);
            this.blit(stack, guiLeft - 8, guiTop + 206, 96, 0, 32, 32);
            this.blit(stack, guiLeft + 32, guiTop + 191, 80, 0, 32, 32);

            this.minecraft.getTextureManager().bindTexture(ResourceHelper.TEX_SPECTATOR_WIDGETS);
            this.blit(stack, guiLeft + 40, guiTop + 207, 112, 0, 32, 32);
        }
    }

    public void togglePlaylistView()
    {
        doneTimeout = 2;
        index = -1;
        playlistView = !playlistView;
        init(minecraft, minecraft.getMainWindow().getScaledWidth(), minecraft.getMainWindow().getScaledHeight());
        trackList.setTracks(playlistView ? playlist : tracks);
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
            this.minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
        this.minecraft.mouseHelper.grabMouse();
    }

    @Override
    public void closeScreen()
    {
        minecraft.player.closeScreen();
    }

    private boolean isMouseOverSlot(Slot slotIn, double mouseX, double mouseY)
    {
        return this.isPointInRegion(slotIn.xPos, slotIn.yPos, 16, 16, mouseX, mouseY);
    }

    protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, double pointX, double pointY)
    {
        int i = this.guiLeft;
        int j = this.guiTop;
        pointX = pointX - i;
        pointY = pointY - j;
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }

    private void drawSlot(MatrixStack stack, Slot slotIn) //Taken from ContainerScreen
    {
        int i = slotIn.xPos;
        int j = slotIn.yPos;
        ItemStack itemstack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = false;
        String s = null;

        this.setBlitOffset(100);
        this.itemRenderer.zLevel = 100.0F;
        if (itemstack.isEmpty() && slotIn.isEnabled()) {
            Pair<ResourceLocation, ResourceLocation> pair = slotIn.getBackground();
            if (pair != null) {
                TextureAtlasSprite textureatlassprite = this.minecraft.getAtlasSpriteGetter(pair.getFirst()).apply(pair.getSecond());
                this.minecraft.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
                blit(stack, i, j, this.getBlitOffset(), 16, 16, textureatlassprite);
                flag1 = true;
            }
        }

        if (!flag1) {
            if (flag) {
                fill(stack, i, j, i + 16, j + 16, -2130706433);
            }

            RenderSystem.enableDepthTest();
            this.itemRenderer.renderItemAndEffectIntoGUI(this.minecraft.player, itemstack, i, j);
            this.itemRenderer.renderItemOverlayIntoGUI(this.font, itemstack, i, j, s);
        }

        this.itemRenderer.zLevel = 0.0F;
        this.setBlitOffset(0);
    }

    @Override //TODO am I doing this right?
    public ContainerInstrumentPlayer getContainer()
    {
        return containerInstrumentPlayer;
    }
}
