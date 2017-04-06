package daxum.temporalconvergence.render.tileentity;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.tileentity.TileDimGen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class TileDimGenRenderer extends TileEntitySpecialRenderer<TileDimGen> {
	private final ResourceLocation clockTexture = new ResourceLocation(TemporalConvergence.MODID + ":textures/dimensional_generator_clock.png");
	private final ResourceLocation clockHands = new ResourceLocation(TemporalConvergence.MODID + ":textures/dimensional_generator_clock_hands.png");

	@Override
	public void renderTileEntityAt(TileDimGen te, double transformX, double transformY, double transformZ, float partialTicks, int destroyStage) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(transformX, transformY, transformZ);
		GlStateManager.translate(0.5f, 0.95f, 0.5f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableNormalize(); //What does this even do?
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableCull();
		GlStateManager.disableLighting();

		//Full size has a scale of 15.0
		renderClock((te.scale - te.prevScale) * partialTicks + te.prevScale,
				(te.rotations[0] - te.prevRotations[0]) * partialTicks + te.prevRotations[0],
				(te.rotations[1] - te.prevRotations[1]) * partialTicks + te.prevRotations[1],
				(te.rotations[2] - te.prevRotations[2]) * partialTicks + te.prevRotations[2],
				(te.rotations[3] - te.prevRotations[3]) * partialTicks + te.prevRotations[3]);

		GlStateManager.disableBlend();
		GlStateManager.disableNormalize();
		GlStateManager.enableCull();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();

		renderItem(te, transformX, transformY, transformZ);
	}

	//Rotations in degrees from 12:00 (clockwise)
	private void renderClock(float scale, float clockRot, float hourRot, float minuteRot, float secondRot) {
		GlStateManager.pushMatrix();
		GlStateManager.rotate(360 - clockRot, 0.0f, 1.0f, 0.f);

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
		bindTexture(clockTexture);
		vb.begin(7, DefaultVertexFormats.POSITION_TEX);
		vb.pos(-radius, 0.0, -radius).tex(0.0, 0.0).endVertex();
		vb.pos(radius, 0.0, -radius).tex(1.0, 0.0).endVertex();
		vb.pos(radius, 0.0, radius).tex(1.0, 1.0).endVertex();
		vb.pos(-radius, 0.0, radius).tex(0.0, 1.0).endVertex();

		tess.draw();

		//Clock hands. 180 added because apparently 0 points to 6:00...?
		bindTexture(clockHands);
		drawHand(tess, vb, hourRot + 180.0f, hourWidth, hourHeight, 0.375, 0.71875);
		drawHand(tess, vb, minuteRot + 180.0f, minuteWidth, minuteHeight, 0.09375, 0.375);
		drawHand(tess, vb, secondRot + 180.0f, secondWidth, secondHeight, 0.0, 0.09375);

		GlStateManager.popMatrix();
	}

	//texStart and texEnd are the start and end of the x value
	private void drawHand(Tessellator tess, VertexBuffer vb, float rot, double width, double height, double texStart, double texEnd) {
		GlStateManager.pushMatrix();
		GlStateManager.rotate(360 - rot, 0.0f, 1.0f, 0.0f);

		vb.begin(7, DefaultVertexFormats.POSITION_TEX);
		vb.pos(-width, -0.01, height).tex(texStart, 0.0).endVertex();
		vb.pos(width, -0.01, height).tex(texEnd, 0.0).endVertex();
		vb.pos(width, -0.01, 0.0).tex(texEnd, 1.0).endVertex();
		vb.pos(-width, -0.01, 0.0).tex(texStart, 1.0).endVertex();

		tess.draw();
		GlStateManager.popMatrix();
	}

	private void renderItem(TileDimGen te, double x, double y, double z) {
		ItemStack stack = te.getInventory().getStackInSlot(0);

		if (!stack.isEmpty()) {
			float angle = (te.getWorld().getTotalWorldTime() + (te.getPos().getX() + te.getPos().getY() + te.getPos().getZ()) * 7) % 90 * 4.0f;
			float bobHeight = (MathHelper.cos(angle * (float)Math.PI / 180.0f) + 1.0f) / 10.0f;

			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 0.5f, y + 1.0f + bobHeight, z + 0.5f);
			GlStateManager.rotate(angle, 0.0f, 1.0f, 0.0f);

			Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);

			GlStateManager.popMatrix();
		}
	}
}
