package daxum.temporalconvergence.gui;

import daxum.temporalconvergence.tileentity.TileTimeChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class GuiTimeChest extends GuiContainer {
	private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	public GuiTimeChest(IInventory playerInv, TileTimeChest tc) {
		super(new ContainerTimeChest(playerInv, tc));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		drawTexturedModalRect(i, j, 0, 0, xSize, 3 * 18 + 17);
		drawTexturedModalRect(i, j + 3 * 18 + 17, 0, 126, xSize, 96);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString("Asynchronous Chest", 8, 6, 4210752);
		fontRendererObj.drawString("Inventory", 8, ySize - 96 + 2, 4210752);
	}
}
