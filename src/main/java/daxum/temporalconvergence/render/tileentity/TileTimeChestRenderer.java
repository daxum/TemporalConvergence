package daxum.temporalconvergence.render.tileentity;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.model.ModelTimeChest;
import daxum.temporalconvergence.tileentity.TileTimeChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

public class TileTimeChestRenderer extends TileEntitySpecialRenderer<TileTimeChest> {
	private final ResourceLocation timeChestTexture = new ResourceLocation(TemporalConvergence.MODID + ":textures/time_chest.png");
	private final ModelTimeChest timeChestModel = new ModelTimeChest();

	@Override
	public void renderTileEntityAt(TileTimeChest te, double x, double y, double z, float partialTicks, int destroyStage) {
		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515); //No idea what this is, but vanilla chest has it.
		GlStateManager.depthMask(true);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		bindTexture(timeChestTexture);
		timeChestModel.render(te.getBlockMetadata(), -(float) ((te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks) * Math.PI / 2.0f));

		GlStateManager.popMatrix();
	}
}
