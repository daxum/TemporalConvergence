package daxum.temporalconvergence.render.tileentity;

import org.lwjgl.opengl.GL11;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.tileentity.TileDimContr;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class TileDimContrRenderer extends TileEntitySpecialRenderer<TileDimContr> {
	public static final ResourceLocation SOLID_BLACK = new ResourceLocation(TemporalConvergence.MODID + ":textures/test_texture.png");
	public static final float RADIUS = 1.0f;
	public static final float PIF = (float) Math.PI;

	@Override
	public void renderTileEntityAt(TileDimContr te, double transformX, double transformY, double transformZ, float partialTicks, int destroyStage) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(transformX + 0.5, transformY + 0.5, transformZ + 0.5);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.disableLighting();
		//GlStateManager.disableCull();

		Tessellator tess = Tessellator.getInstance();
		VertexBuffer vb = tess.getBuffer();

		bindTexture(SOLID_BLACK);
		vb.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);

		//The sphere is complete.
		boolean alt = true;
		for (int i = 0; i < 360; i += 5) {
			if (!alt) {
				for (int j = 0; j <= 185; j += 5) {
					if (alt) {
						float theta = j * PIF / 180.0f;
						float phi = i * PIF / 180.0f;
						float sintheta = MathHelper.sin(theta);

						vb.pos(RADIUS * sintheta * MathHelper.cos(phi), RADIUS * sintheta * MathHelper.sin(phi), RADIUS * MathHelper.cos(theta)).tex(j / 185.0, j / 185.0).endVertex();
					}
					else {
						float theta = j * PIF / 180.0f;
						float phi = (i + 5) * PIF / 180.0f;
						float sintheta = MathHelper.sin(theta);

						vb.pos(RADIUS * sintheta * MathHelper.cos(phi), RADIUS * sintheta * MathHelper.sin(phi), RADIUS * MathHelper.cos(theta)).tex(j / 185.0, j / 185.0).endVertex();
					}
					alt = !alt;
				}
			}
			else {
				for (int j = 180; j >= -5; j -= 5) {
					if (alt) {
						float theta = j * PIF / 180.0f;
						float phi = i * PIF / 180.0f;
						float sintheta = MathHelper.sin(theta);

						vb.pos(RADIUS * sintheta * MathHelper.cos(phi), RADIUS * sintheta * MathHelper.sin(phi), RADIUS * MathHelper.cos(theta)).tex(j / 185.0, j / 185.0).endVertex();
					}
					else {
						float theta = j * PIF / 180.0f;
						float phi = (i + 5) * PIF / 180.0f;
						float sintheta = MathHelper.sin(theta);

						vb.pos(RADIUS * sintheta * MathHelper.cos(phi), RADIUS * sintheta * MathHelper.sin(phi), RADIUS * MathHelper.cos(theta)).tex(j / 185.0, j / 185.0).endVertex();
					}
					alt = !alt;
				}
			}
			alt = !alt;
		}

		/*
		boolean alt = true;
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j <= 365; j += 5) {
				if (alt)
					vb.pos(RADIUS * MathHelper.cos(j * PIF / 180.0f), RADIUS * MathHelper.sin(j * PIF / 180.0f), i).tex(j / 360.0, j / 360.0).endVertex();
				else
					vb.pos(RADIUS * MathHelper.cos(j * PIF / 180.0f), RADIUS * MathHelper.sin(j * PIF / 180.0f), i + 1).tex(j / 360.0, j / 360.0).endVertex();
				alt = !alt;
			}
			alt = !alt;
		}*/

		tess.draw();

		//GlStateManager.enableCull();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
}
