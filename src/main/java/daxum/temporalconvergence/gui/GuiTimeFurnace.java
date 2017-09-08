/***************************************************************************
 * Temporal Convergence
 * Copyright (C) 2017
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 **************************************************************************/
package daxum.temporalconvergence.gui;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.tileentity.TileTimeFurnaceBase;
import daxum.temporalconvergence.tileentity.TileTimeFurnaceController;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class GuiTimeFurnace extends GuiContainer {
	private static final ResourceLocation FURNACE_GUI_TEXTURE = new ResourceLocation(TemporalConvergence.MODID, "textures/gui/time_furnace.png");
	private final TileTimeFurnaceController contr;

	public GuiTimeFurnace(IInventory playerInv, TileTimeFurnaceBase tfc) {
		super(new ContainerTimeFurnace(playerInv, tfc));
		contr = tfc.getController();
		ySize = 172;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(FURNACE_GUI_TEXTURE);

		//Main Gui
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		//Fuel usage
		if (contr.isBurning()) {
			float burnPercent = contr.getBurnPercent();

			int height = 13 - MathHelper.ceil(13.0 * burnPercent);

			drawTexturedModalRect(x + 55, y + 40 + height, 176, 1 + height, 16, 13 - height);
		}

		//Smelting progress
		if (contr.isSmelting()) {
			double smeltPercent = contr.getSmeltPercent();

			int width = MathHelper.floor(23.0 * smeltPercent);

			drawTexturedModalRect(x + 79, y + 36, 176, 14, width + 1, 17);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(I18n.format("container.timefurnace"), 9, 7, 9838347);
		fontRenderer.drawString(I18n.format("container.inventory"), 9, 78, 9838347);
	}
}
