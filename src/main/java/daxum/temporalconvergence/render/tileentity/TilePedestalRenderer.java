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

import daxum.temporalconvergence.tileentity.TilePedestal;
import daxum.temporalconvergence.util.RenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TilePedestalRenderer extends TileEntitySpecialRenderer<TilePedestal> {
	@Override
	//x, y, and z ARE NOT world coordinates. STOP TREATING THEM AS SUCH.
	public void render(TilePedestal te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y + 1.0, z + 0.5);

		RenderHelper.renderItem(te.getWorld().getTotalWorldTime(), te.getInventory().getStackInSlot(0), te.getPos(), partialTicks, true);

		GlStateManager.popMatrix();
	}
}
