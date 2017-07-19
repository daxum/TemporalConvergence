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
package daxum.temporalconvergence.util;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.block.BlockBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//Everything rendering related that gets used three or more times goes here
@SideOnly(Side.CLIENT)
public final class RenderHelper {
	private static Map<Long, Integer> sphereRenderMap = new HashMap<>();
	private static Map<Long, Integer> sphereMeshRenderMap = new HashMap<>();

	public static final float PI = (float) Math.PI; //PI as a float, to save annoyance over casting

	//rotationOffset is used to randomize rotation, and should be the same every time the function is called. If it doesn't need to be randomized, use the version below
	//Translate should be called before this function to position the item
	//Time is world time or similar
	public static void renderItem(long time, ItemStack stack, int rotationOffset, float partialTicks, boolean shouldBob) {
		if (!stack.isEmpty()) {
			int offset = rotationOffset * 7;
			float angle = (time + partialTicks + offset) / 20.0f * (180f / PI);
			float bobHeight = shouldBob ? MathHelper.sin((time + partialTicks + offset) / 10.0f) * 0.1f + 0.1f : 0.0f;

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, bobHeight, 0.0f);
			GlStateManager.rotate(angle, 0.0f, 1.0f, 0.0f);

			Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);

			GlStateManager.popMatrix();
		}
	}

	public static void renderItem(long time, ItemStack stack, float partialTicks, boolean shouldBob) {
		renderItem(time, stack, 0, partialTicks, shouldBob);
	}

	public static void renderItem(long time, ItemStack stack, BlockPos rotationRandomizer, float partialTicks, boolean shouldBob) {
		renderItem(time, stack, rotationRandomizer.getX() + rotationRandomizer.getY() + rotationRandomizer.getZ(), partialTicks, shouldBob);
	}

	//Helper function because I'm tired of typing all three values
	public static void scale(float amount) {
		GlStateManager.scale(amount, amount, amount);
	}

	//Texture start and width are between 0 and 1, (0,0) is top left and (1,1) is bottom right. Step is the complexity of the sphere, lower values produce more detailed spheres.
	//The radius should be set before calling this using GlStateManager.scale(). Step should be evenly divisible by 180 (180/step should be even)
	public static void renderSphere(int step, float texStartX, float texWidthX, float texStartY, float texWidthY, boolean mesh) {
		if (step < 1 || step > 180 || texStartX < 0 || texStartX > 1 || texWidthX < 0 || texWidthX + texStartX > 1 || texStartY < 0 || texStartY > 1 || texWidthY < 0 || texWidthY + texStartY > 1) {
			TemporalConvergence.LOGGER.info("renderSphere() called with invalid arguments, skipping");
			return;
		}

		long key = (long)Float.floatToIntBits(texStartX) << 32; //Hopefully this won't collide...
		key += Float.floatToIntBits(texWidthX);
		key *= texStartY + texWidthY * 13;
		key += step;

		Map<Long, Integer> currentMap = mesh ? sphereMeshRenderMap : sphereRenderMap;

		if (currentMap.containsKey(key)) {
			GlStateManager.callList(currentMap.get(key));
		}
		else {
			//TemporalConvergence.LOGGER.info("Generating new sphere display list for: " + step + " " + texStartX + " " + texWidthX + " " + texStartY + " " + texWidthY + " " + mesh);
			int list = GLAllocation.generateDisplayLists(1);
			GlStateManager.glNewList(list, GL11.GL_COMPILE);

			renderSphereDirect(1.0f, step, texStartX, texWidthX, texStartY, texWidthY, mesh);

			GlStateManager.glEndList();

			currentMap.put(key, list);
			GlStateManager.callList(list);
		}
	}

	//Render a sphere with the given radius
	public static void renderSphere(float radius, int step, float texStartX, float texWidthX, float texStartY, float texWidthY, boolean mesh) {
		GlStateManager.pushMatrix();
		scale(radius);
		renderSphere(step, texStartX, texWidthX, texStartY, texWidthY, mesh);
		GlStateManager.popMatrix();
	}

	//Do not call this unless absolutely necessary - It generates 2664 vertices for a sphere of step 5, which totals 52 kb and a lot of lag.
	public static void renderSphereDirect(float radius, int step, float texStartX, float texWidthX, float texStartY, float texWidthY, boolean mesh) {
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder vb = tess.getBuffer();

		int glMode = mesh ? GL11.GL_LINE_STRIP : GL11.GL_TRIANGLE_STRIP;

		//The sphere is complete.
		boolean alt = true;
		for (int i = 0; i < 360; i += step) {
			vb.begin(glMode, DefaultVertexFormats.POSITION_TEX);
			if (!alt) {
				for (int j = 0; j <= 180 + step; j += step) {
					if (alt) {
						float theta = j * PI / 180.0f;
						float phi = i * PI / 180.0f;
						float sintheta = MathHelper.sin(theta);

						vb.pos(radius * sintheta * MathHelper.cos(phi), radius * sintheta * MathHelper.sin(phi), radius * MathHelper.cos(theta));
					}
					else {
						float theta = j * PI / 180.0f;
						float phi = (i + step) * PI / 180.0f;
						float sintheta = MathHelper.sin(theta);

						vb.pos(radius * sintheta * MathHelper.cos(phi), radius * sintheta * MathHelper.sin(phi), radius * MathHelper.cos(theta));
					}
					vb.tex(j / (180.0 + step) * texWidthX + texStartX, i / 359.0 * texWidthY + texStartY).endVertex();
					alt = !alt;
				}
			}
			else {
				for (int j = 180; j >= -step; j -= step) {
					if (alt) {
						float theta = j * PI / 180.0f;
						float phi = i * PI / 180.0f;
						float sintheta = MathHelper.sin(theta);

						vb.pos(radius * sintheta * MathHelper.cos(phi), radius * sintheta * MathHelper.sin(phi), radius * MathHelper.cos(theta));
					}
					else {
						float theta = j * PI / 180.0f;
						float phi = (i + step) * PI / 180.0f;
						float sintheta = MathHelper.sin(theta);

						vb.pos(radius * sintheta * MathHelper.cos(phi), radius * sintheta * MathHelper.sin(phi), radius * MathHelper.cos(theta));
					}
					vb.tex((j + step) / (180.0 + step) * texWidthX + texStartX, i / 359.0 * texWidthY + texStartY).endVertex();
					alt = !alt;
				}
			}
			alt = !alt;
			tess.draw();
		}
	}

	public static void drawSelectionBoxes(World world, EntityPlayer player, IBlockState state, BlockPos pos, float partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0f);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		if (state.getMaterial() != Material.AIR && world.getWorldBorder().contains(pos)) {
			double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
			double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
			double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

			if (state.getBlock() instanceof BlockBase) {
				for (AxisAlignedBB aabb : ((BlockBase)state.getBlock()).getSelectedBBList(world, pos, state)) {
					RenderGlobal.drawSelectionBoundingBox(aabb.grow(0.002).offset(-px, -py, -pz), 0.0f, 0.0f, 0.0f, 0.4f);
				}
			}
			else {
				RenderGlobal.drawSelectionBoundingBox(state.getSelectedBoundingBox(world, pos).grow(0.002).offset(-px, -py, -pz), 0.0f, 0.0f, 0.0f, 0.4f);
			}
		}

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
}
