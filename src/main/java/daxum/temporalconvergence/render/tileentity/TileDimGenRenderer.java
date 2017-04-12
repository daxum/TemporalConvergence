package daxum.temporalconvergence.render.tileentity;

import org.lwjgl.opengl.GL11;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.tileentity.TileDimGen;
import daxum.temporalconvergence.util.RenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class TileDimGenRenderer extends TileEntitySpecialRenderer<TileDimGen> {
	private static final ResourceLocation CLOCK_TEXTURE = new ResourceLocation(TemporalConvergence.MODID + ":textures/dimensional_generator_clock.png");
	private static final ResourceLocation CLOCK_HANDS = new ResourceLocation(TemporalConvergence.MODID + ":textures/dimensional_generator_clock_hands.png");
	private static final ResourceLocation SPHERE = new ResourceLocation(TemporalConvergence.MODID + ":textures/sphere_textures.png");

	@Override
	public void renderTileEntityAt(TileDimGen te, double transformX, double transformY, double transformZ, float partialTicks, int destroyStage) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(transformX + 0.5f, transformY + 0.95f, transformZ + 0.5f);
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

		renderAnimations(te, partialTicks);

		GlStateManager.popMatrix();
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

	private void renderAnimations(TileDimGen te, float partialTicks) {
		GlStateManager.translate(0.0f, 0.1f, 0.0f);
		int stage = te.getCraftingStage();

		if (!(stage == 3 && te.wasSuccessful()))
			RenderHelper.renderItem(te, te.getInventory().getStackInSlot(0), partialTicks, false);

		if (stage == 2 || stage == 3 && te.wasSuccessful()) {
			GlStateManager.translate(0.0f, 0.25f, 0.0f);
			renderCraftSpheres(te.getRecipeSize() - 1, te.getCraftTime(), partialTicks, te.isNS());
		}
		else if (stage == 4) {
			GlStateManager.translate(0.0f, 0.25f, 0.0f);
			float ratio = (400 - te.getCraftTime() - partialTicks) / 15.0f;
			renderSingleSphere(1.5f * (1.0f - ratio) + 0.25f, ratio);
		}
	}

	private void renderCraftSpheres(int amount, int stage, float partialTicks, boolean twoNS) {
		if (stage <= 10) {
			renderSphereAmount(amount, 0.25f * (stage + partialTicks) / 10.0f, twoNS, 1.0f);
		}
		else {
			renderSphereAmount(amount, 0.25f, twoNS, 1.0f - (stage + partialTicks - 10.0f) / 390.0f);
			GlStateManager.translate(0.0f, -0.1f, 0.0f);
			renderSingleSphere(0.25f, (stage + partialTicks - 10.0f) / 390.0f);
		}
	}

	//Oh god why
	private void renderSphereAmount(int amount, float radius, boolean twoNS, float transparency) {
		GlStateManager.pushMatrix();

		switch(amount) {
		case 2:
			if (twoNS) {
				GlStateManager.translate(0.0f, 0.0f, 6.0f);
				renderSingleSphere(radius, transparency);
				GlStateManager.translate(0.0f, 0.0f, -12.0f);
				renderSingleSphere(radius, transparency);
			}
			else {
				GlStateManager.translate(6.0f, 0.0f, 0.0f);
				renderSingleSphere(radius, transparency);
				GlStateManager.translate(-12.0f, 0.0f, 0.0f);
				renderSingleSphere(radius, transparency);
			}
			break;

		case 4:
			GlStateManager.translate(0.0f, 0.0f, 6.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(0.0f, 0.0f, -12.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(6.0f, 0.0f, 6.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(-12.0f, 0.0f, 0.0f);
			renderSingleSphere(radius, transparency);
			break;

		case 8:
			GlStateManager.translate(3.0f, 0.0f, 5.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(2.0f, 0.0f, -2.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(0.0f, 0.0f, -6.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(-2.0f, 0.0f, -2.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(-6.0f, 0.0f, 0.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(-2.0f, 0.0f, 2.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(0.0f, 0.0f, 6.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(2.0f, 0.0f, 2.0f);
			renderSingleSphere(radius, transparency);
			break;

		case 12:
			GlStateManager.translate(3.0f, 0.0f, 5.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(2.0f, 0.0f, -2.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(1.0f, 0.0f, -3.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(-1.0f, 0.0f, -3.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(-2.0f, 0.0f, -2.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(-3.0f, 0.0f, -1.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(-3.0f, 0.0f, 1.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(-2.0f, 0.0f, 2.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(-1.0f, 0.0f, 3.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(1.0f, 0.0f, 3.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(2.0f, 0.0f, 2.0f);
			renderSingleSphere(radius, transparency);
			GlStateManager.translate(3.0f, 0.0f, 1.0f);
			renderSingleSphere(radius, transparency);
			break;
		}

		GlStateManager.popMatrix();
	}

	private void renderSingleSphere(float radius, float transparency) {
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0f, 1.0f, 1.0f, transparency);

		bindTexture(SPHERE);
		RenderHelper.renderSphere(radius, 10, 0.0f, 0.5f, 0.5f, 0.5f, false);

		GlStateManager.color(1.0f, 1.0f, 1.0f, 0.45f * transparency);

		RenderHelper.renderSphere(radius * 1.12f, 10, 0.0f, 0.5f, 0.5f, 0.5f, false);

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
}
