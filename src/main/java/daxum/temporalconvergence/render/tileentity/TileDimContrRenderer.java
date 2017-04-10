package daxum.temporalconvergence.render.tileentity;

import org.lwjgl.opengl.GL11;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.tileentity.TileDimContr;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class TileDimContrRenderer extends TileEntitySpecialRenderer<TileDimContr> {
	public static final ResourceLocation SOLID_BLACK = new ResourceLocation(TemporalConvergence.MODID + ":textures/test_texture.png");
	public static final float RADIUS = 0.25f;
	public static final float PIF = (float) Math.PI;
	protected static boolean compiled = false;
	protected static int displayList;


	@Override
	public void renderTileEntityAt(TileDimContr te, double transformX, double transformY, double transformZ, float partialTicks, int destroyStage) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(transformX + 0.5, transformY + 0.5, transformZ + 0.5);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.disableLighting();

		bindTexture(SOLID_BLACK);

		if (!compiled)
			compileSphereDisplayList();
		GlStateManager.callList(displayList);

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	protected void compileSphereDisplayList() {
		displayList = GLAllocation.generateDisplayLists(1);
		GlStateManager.glNewList(displayList, GL11.GL_COMPILE);

		renderSphere(RADIUS);

		GlStateManager.glEndList();
		compiled = true;
	}

	public void renderSphere(float radius) {
		Tessellator tess = Tessellator.getInstance();
		VertexBuffer vb = tess.getBuffer();

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

						vb.pos(radius * sintheta * MathHelper.cos(phi), radius * sintheta * MathHelper.sin(phi), radius * MathHelper.cos(theta)).tex(j / 185.0, j / 185.0).endVertex();
					}
					else {
						float theta = j * PIF / 180.0f;
						float phi = (i + 5) * PIF / 180.0f;
						float sintheta = MathHelper.sin(theta);

						vb.pos(radius * sintheta * MathHelper.cos(phi), radius * sintheta * MathHelper.sin(phi), radius * MathHelper.cos(theta)).tex(j / 185.0, j / 185.0).endVertex();
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

						vb.pos(radius * sintheta * MathHelper.cos(phi), radius * sintheta * MathHelper.sin(phi), radius * MathHelper.cos(theta)).tex(j / 185.0, j / 185.0).endVertex();
					}
					else {
						float theta = j * PIF / 180.0f;
						float phi = (i + 5) * PIF / 180.0f;
						float sintheta = MathHelper.sin(theta);

						vb.pos(radius * sintheta * MathHelper.cos(phi), radius * sintheta * MathHelper.sin(phi), radius * MathHelper.cos(theta)).tex(j / 185.0, j / 185.0).endVertex();
					}
					alt = !alt;
				}
			}
			alt = !alt;
		}

		tess.draw();
	}
}
