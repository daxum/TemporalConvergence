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

import daxum.temporalconvergence.block.BlockBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
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
		renderItem(time, stack, rotationRandomizer.getX() * rotationRandomizer.getY() * rotationRandomizer.getZ(), partialTicks, shouldBob);
	}

	//Helper function because I'm tired of typing all three values
	public static void scale(float amount) {
		GlStateManager.scale(amount, amount, amount);
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
