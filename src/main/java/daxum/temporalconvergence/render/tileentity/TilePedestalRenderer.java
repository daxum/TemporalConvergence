package daxum.temporalconvergence.render.tileentity;

import daxum.temporalconvergence.tileentity.TilePedestal;
import daxum.temporalconvergence.util.RenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TilePedestalRenderer extends TileEntitySpecialRenderer<TilePedestal> {
	@Override
	//x, y, and z ARE NOT world coordinates. STOP TREATING THEM AS SUCH.
	public void renderTileEntityAt(TilePedestal te, double x, double y, double z, float partialTicks, int destroyStage) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y + 1.0, z + 0.5);

		RenderHelper.renderItem(te, te.getInventory().getStackInSlot(0), partialTicks, true);

		GlStateManager.popMatrix();
	}
}
