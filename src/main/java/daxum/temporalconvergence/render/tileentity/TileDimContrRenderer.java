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

//TODO: This should probably be made into some sort of model
public class TileDimContrRenderer extends TileEntitySpecialRenderer<TileDimContr> {
	public static final ResourceLocation SPHERE = new ResourceLocation(TemporalConvergence.MODID + ":textures/dim_contr_center.png");
	public static final float RADIUS = 0.25f;
	public static final float PIF = (float) Math.PI;
	protected static boolean compiled = false;
	protected static int displayList;

	@Override
	public void renderTileEntityAt(TileDimContr te, double transformX, double transformY, double transformZ, float partialTicks, int destroyStage) {
		if (te.renderScale <= 0 && te.getId() == -1) return;

		GlStateManager.pushMatrix();
		GlStateManager.translate(transformX + 0.5, transformY + 0.5, transformZ + 0.5);
		GlStateManager.rotate(te.getWorld().getTotalWorldTime() + partialTicks, 0.0f, 1.0f, 0.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.disableLighting();

		bindTexture(SPHERE);

		if (te.renderScale >= 1.0f && te.getId() != -1) {
			callSphereDisplayLists();
		}
		else if (te.renderScale <= 1.0f && te.getId() != -1){
			renderSpheresDirect(RADIUS * te.renderScale);
			te.renderScale += 0.05;
		}
		else if (te.renderScale > 0 && te.getId() == -1) {
			renderSpheresDirect(RADIUS * te.renderScale);
			te.renderScale -= 0.05;
		}

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	public void callSphereDisplayLists() {
		if (!compiled)
			compileSphereDisplayLists();
		GlStateManager.callList(displayList);
	}

	public void renderSpheresDirect(float radius) {
		renderSphere(radius, 5, 0.0f, 0.5f, 0.0f, 1.0f, false);
		renderSphere(radius * 1.05f, 15, 0.5f, 0.5f, 0.0f, 1.0f, true);
	}

	private void compileSphereDisplayLists() {
		displayList = GLAllocation.generateDisplayLists(1);
		GlStateManager.glNewList(displayList, GL11.GL_COMPILE);

		renderSphere(RADIUS, 5, 0.0f, 0.5f, 0.0f, 1.0f, false);
		renderSphere(RADIUS * 1.05f, 15, 0.5f, 0.5f, 0.0f, 1.0f, true);

		GlStateManager.glEndList();

		compiled = true;
	}

	private void renderSphere(float radius, int step, float texStartX, float texWidthX, float texStartY, float texWidthY, boolean mesh) {
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
