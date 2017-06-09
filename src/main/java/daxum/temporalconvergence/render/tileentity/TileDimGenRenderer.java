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

import org.lwjgl.opengl.GL11;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.tileentity.TileDimGen;
import daxum.temporalconvergence.tileentity.TileDimGen.ClockPart;
import daxum.temporalconvergence.tileentity.TileDimGen.CraftingStates;
import daxum.temporalconvergence.util.RenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileDimGenRenderer extends TileEntitySpecialRenderer<TileDimGen> {
	private static final ResourceLocation CLOCK_TEXTURE = new ResourceLocation(TemporalConvergence.MODID, "textures/dimensional_generator_clock.png");
	private static final ResourceLocation CLOCK_HANDS = new ResourceLocation(TemporalConvergence.MODID, "textures/dimensional_generator_clock_hands.png");
	private static final ResourceLocation SPHERE = new ResourceLocation(TemporalConvergence.MODID, "textures/sphere_textures.png");

	@Override
	public void renderTileEntityAt(TileDimGen te, double transformX, double transformY, double transformZ, float partialTicks, int destroyStage) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(transformX + 0.5f, transformY + 0.95f, transformZ + 0.5f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableCull();
		GlStateManager.disableLighting();

		//Full size has a scale of 15.0
		renderClock(te.getScaleForRender(partialTicks), te.getRotationForRender(ClockPart.FACE, partialTicks), te.getRotationForRender(ClockPart.HOUR_HAND,
				partialTicks), te.getRotationForRender(ClockPart.MINUTE_HAND, partialTicks), te.getRotationForRender(ClockPart.SECOND_HAND, partialTicks));


		GlStateManager.enableCull();
		GlStateManager.translate(0.0f, 0.1f, 0.0f);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true); //For some reason this was off. Don't know why or how

		renderItem(te, partialTicks);

		GlStateManager.translate(0.0f, 0.25f, 0.0f);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();

		renderCraftSpheres(te, partialTicks);

		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	//Rotations in degrees from 12:00 (clockwise)
	private void renderClock(float scale, float clockRot, float hourRot, float minuteRot, float secondRot) {
		GlStateManager.pushMatrix();
		GlStateManager.rotate(360.0f - clockRot, 0.0f, 1.0f, 0.f);

		Tessellator tess = Tessellator.getInstance();
		VertexBuffer vb = tess.getBuffer();

		//These numbers are magic and special. Don't touch them!
		double radius = scale * 0.4;
		double hourWidth = radius * 0.0703125;
		double hourHeight = radius * 0.5;
		double minuteWidth = hourWidth * 0.65455;
		double minuteHeight = radius * 0.65;
		double secondWidth = minuteWidth * 0.3334;
		double secondHeight = radius * 0.85;

		//Main clock face
		bindTexture(CLOCK_TEXTURE);
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		vb.pos(-radius, 0.0, -radius).tex(0.0, 0.0).endVertex();
		vb.pos(radius, 0.0, -radius).tex(1.0, 0.0).endVertex();
		vb.pos(radius, 0.0, radius).tex(1.0, 1.0).endVertex();
		vb.pos(-radius, 0.0, radius).tex(0.0, 1.0).endVertex();

		tess.draw();

		//Clock hands. 180 added because apparently 0 points to 6:00...?
		bindTexture(CLOCK_HANDS);
		drawHand(tess, vb, hourRot + 180.0f, hourWidth, hourHeight, 0.375, 0.71875);
		drawHand(tess, vb, minuteRot + 180.0f, minuteWidth, minuteHeight, 0.09375, 0.375);
		drawHand(tess, vb, secondRot + 180.0f, secondWidth, secondHeight, 0.0, 0.09375);

		GlStateManager.popMatrix();
	}

	//texStart and texEnd are the start and end of the x value
	private void drawHand(Tessellator tess, VertexBuffer vb, float rot, double width, double height, double texStart, double texEnd) {
		GlStateManager.pushMatrix();
		GlStateManager.rotate(360 - rot, 0.0f, 1.0f, 0.0f);

		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		vb.pos(-width, -0.01, height).tex(texStart, 0.0).endVertex();
		vb.pos(width, -0.01, height).tex(texEnd, 0.0).endVertex();
		vb.pos(width, -0.01, 0.0).tex(texEnd, 1.0).endVertex();
		vb.pos(-width, -0.01, 0.0).tex(texStart, 1.0).endVertex();

		tess.draw();
		GlStateManager.popMatrix();
	}

	private void renderItem(TileDimGen te, float partialTicks) {
		if (!(te.getCraftingState() == CraftingStates.END_SUCCESS)){
			RenderHelper.renderItem(te.getWorld().getTotalWorldTime(), te.getInventory().getStackInSlot(0), te.getPos(), partialTicks, false);
		}
	}

	private void renderCraftSpheres(TileDimGen te, float partialTicks) {
		CraftingStates state = te.getCraftingState();

		if (state.isInCraftingStep()) {
			for (int i = 0; i < TileDimGen.PEDESTAL_COUNT; i++) {
				if (te.isPedestalActive(i)) {
					GlStateManager.pushMatrix();

					BlockPos pedLoc = te.getPedestalLoc(i);
					BlockPos centerLoc = te.getPos();
					GlStateManager.translate(pedLoc.getX() - centerLoc.getX(), pedLoc.getY() - centerLoc.getY(), pedLoc.getZ() - centerLoc.getZ());

					renderSingleSphere(te.getScaleForPedestal(i, partialTicks), te.getPedestalTransparency(partialTicks));

					GlStateManager.popMatrix();
				}
			}

			GlStateManager.translate(0.0f, -0.05f, 0.0f);
			renderSingleSphere(te.getCenterScale(partialTicks), te.getCenterTransparency(partialTicks));
		}
		else if (state.hasSucceeded()) {
			GlStateManager.translate(0.0f, -0.05f, 0.0f);
			renderSingleSphere(te.getCenterScale(partialTicks), te.getCenterTransparency(partialTicks));
		}
	}

	private void renderSingleSphere(float radius, float transparency) {
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, transparency);

		bindTexture(SPHERE);
		RenderHelper.renderSphere(radius, 10, 0.0f, 0.5f, 0.5f, 0.5f, false);

		GlStateManager.color(1.0f, 1.0f, 1.0f, 0.45f * transparency);

		RenderHelper.renderSphere(radius * 1.12f, 10, 0.0f, 0.5f, 0.5f, 0.5f, false);

		GlStateManager.popMatrix();
	}
}
