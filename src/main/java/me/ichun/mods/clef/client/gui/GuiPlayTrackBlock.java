package me.ichun.mods.clef.client.gui;

import me.ichun.mods.clef.common.inventory.ContainerInstrumentPlayer;
import me.ichun.mods.clef.common.tileentity.TileEntityInstrumentPlayer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import java.io.IOException;

public class GuiPlayTrackBlock extends GuiPlayTrack
{
    public TileEntityInstrumentPlayer player;
    public ContainerInstrumentPlayer containerInstrumentPlayer;

    public GuiPlayTrackBlock(TileEntityInstrumentPlayer player)
    {
        super();
        background = texBackgroundBlock;
        this.player = player;
        this.containerInstrumentPlayer = new ContainerInstrumentPlayer(player);
    }

    @Override
    public void initGui()
    {
        super.initGui();
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
            Slot slot = (Slot)this.containerInstrumentPlayer.inventorySlots.get(i);

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
    public void drawReloadButtons(){}

    @Override
    public void confirmSelection()
    {
        if(syncTrack == 0 && !(index >= 0 && index < tracks.size()))
        {
            return;
        }
        //        Clef.channel.sendToServer(new PacketPlayABC(index >= 0 && index < tracks.size() ? tracks.get(index).md5 : "", bandName.getText(), syncPlay == 1, syncTrack == 1));
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
