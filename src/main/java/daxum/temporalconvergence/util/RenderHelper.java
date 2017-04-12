package daxum.temporalconvergence.util;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//Everything rendering related that gets used three or more times goes here
@SideOnly(Side.CLIENT)
public final class RenderHelper {
	private static Map<Long, Integer> sphereRenderMap = new HashMap<>();
	private static Map<Long, Integer> sphereMeshRenderMap = new HashMap<>();

	public static final float PIF = (float) Math.PI; //PI as a float, to save annoyance over casting

	//x, y, and z are used to randomize rotation a bit, and are safe to set to zero. For tile entities, just put the position (use helper function below)
	//Translate should be called before this function to position the item
	public static void renderItem(int time, ItemStack stack, int x, int y, int z, float partialTicks, boolean shouldBob) {
		if (!stack.isEmpty()) {
			int offset = (x + y + z) * 7;
			float angle = (time + partialTicks + offset) / 20.0f * (180f / PIF);
			float bobHeight = shouldBob ? MathHelper.sin((time + partialTicks + offset) / 10.0f) * 0.1f + 0.1f : 0.0f;

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, bobHeight, 0.0f);
			GlStateManager.rotate(angle, 0.0f, 1.0f, 0.0f);

			Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);

			GlStateManager.popMatrix();
		}
	}

	public static void renderItem(int time, ItemStack stack, BlockPos pos, float partialTicks, boolean shouldBob) {
		renderItem(time, stack, pos.getX(), pos.getY(), pos.getZ(), partialTicks, shouldBob);
	}

	public static void renderItem(TileEntity te, ItemStack stack, float partialTicks, boolean shouldBob) {
		renderItem((int) te.getWorld().getTotalWorldTime(), stack, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), partialTicks, shouldBob);
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
		VertexBuffer vb = tess.getBuffer();

		int glMode = mesh ? GL11.GL_LINE_STRIP : GL11.GL_TRIANGLE_STRIP;

		//The sphere is complete.
		boolean alt = true;
		for (int i = 0; i < 360; i += step) {
			vb.begin(glMode, DefaultVertexFormats.POSITION_TEX);
			if (!alt) {
				for (int j = 0; j <= 180 + step; j += step) {
					if (alt) {
						float theta = j * PIF / 180.0f;
						float phi = i * PIF / 180.0f;
						float sintheta = MathHelper.sin(theta);

						vb.pos(radius * sintheta * MathHelper.cos(phi), radius * sintheta * MathHelper.sin(phi), radius * MathHelper.cos(theta));
					}
					else {
						float theta = j * PIF / 180.0f;
						float phi = (i + step) * PIF / 180.0f;
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
						float theta = j * PIF / 180.0f;
						float phi = i * PIF / 180.0f;
						float sintheta = MathHelper.sin(theta);

						vb.pos(radius * sintheta * MathHelper.cos(phi), radius * sintheta * MathHelper.sin(phi), radius * MathHelper.cos(theta));
					}
					else {
						float theta = j * PIF / 180.0f;
						float phi = (i + step) * PIF / 180.0f;
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
}
