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
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.tileentity.TileDimContr;
import daxum.temporalconvergence.util.RenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class TileDimContrRenderer extends TileEntitySpecialRenderer<TileDimContr> {
	public static final ResourceLocation SPHERE = new ResourceLocation(TemporalConvergence.MODID + ":textures/sphere_textures.png");
	public static final float MAX_RADIUS = 0.25f;
	private static final ItemStack BOUND_LINKER = new ItemStack(ModItems.DIM_LINKER);
	private static final ItemStack UNBOUND_LINKER = new ItemStack(ModItems.DIM_LINKER);

	static {
		BOUND_LINKER.setTagCompound(new NBTTagCompound());
		BOUND_LINKER.getTagCompound().setInteger("dimid", 0);
	}

	@Override
	public void renderTileEntityAt(TileDimContr te, double transformX, double transformY, double transformZ, float partialTicks, int destroyStage) {
		if (te.renderScale <= 0 && te.getId() == -1) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(transformX + 0.5, transformY + 0.4, transformZ + 0.5);
			RenderHelper.renderItem((int) te.getWorld().getTotalWorldTime(), UNBOUND_LINKER, 0, 0, 0, partialTicks, false);
			GlStateManager.popMatrix();
			return;
		}

		if (te.isDimFrozen()) {
			te.renderScale = 0;
			GlStateManager.pushMatrix();
			GlStateManager.translate(transformX + 0.5, transformY + 0.4, transformZ + 0.5);
			RenderHelper.renderItem((int) te.getWorld().getTotalWorldTime(), BOUND_LINKER, 0, 0, 0, partialTicks, false);
			GlStateManager.popMatrix();
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(transformX + 0.5, transformY + 0.5, transformZ + 0.5);
		GlStateManager.rotate(te.getWorld().getTotalWorldTime() + partialTicks, 0.0f, 1.0f, 0.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.disableLighting();

		bindTexture(SPHERE);

		if (te.renderScale >= 1.0f && te.getId() != -1) {
			renderSpheres(MAX_RADIUS);
		}
		else if (te.renderScale <= 1.0f && te.getId() != -1){
			renderSpheres(MAX_RADIUS * te.renderScale);
			te.renderScale += 0.05;
		}
		else if (te.renderScale > 0 && te.getId() == -1) {
			renderSpheres(MAX_RADIUS * te.renderScale);
			te.renderScale -= 0.05;
		}

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	public void renderSpheres(float radius) {
		RenderHelper.renderSphere(radius, 10, 0.0f, 0.5f, 0.0f, 0.5f, false);
		RenderHelper.renderSphere(radius * 1.05f, 15, 0.5f, 0.5f, 0.0f, 1.0f, true);
	}
}
