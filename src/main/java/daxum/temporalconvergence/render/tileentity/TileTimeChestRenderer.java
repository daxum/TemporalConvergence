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
package daxum.temporalconvergence.render.tileentity;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.model.ModelTimeChest;
import daxum.temporalconvergence.tileentity.TileTimeChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

public class TileTimeChestRenderer extends TileEntitySpecialRenderer<TileTimeChest> {
	private final ResourceLocation timeChestTexture = new ResourceLocation(TemporalConvergence.MODID + ":textures/time_chest.png");
	private final ModelTimeChest timeChestModel = new ModelTimeChest();

	@Override
	public void renderTileEntityAt(TileTimeChest te, double x, double y, double z, float partialTicks, int destroyStage) {
		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515); //No idea what this is, but vanilla chest has it.
		GlStateManager.depthMask(true);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		bindTexture(timeChestTexture);
		timeChestModel.render(te.getBlockMetadata(), -(float) ((te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks) * Math.PI / 2.0f));

		GlStateManager.popMatrix();
	}
}
