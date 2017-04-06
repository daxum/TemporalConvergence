package daxum.temporalconvergence.render.tileentity;

import daxum.temporalconvergence.tileentity.TilePedestal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class TilePedestalRenderer extends TileEntitySpecialRenderer<TilePedestal> {
	@Override
	//x, y, and z ARE NOT world coordinates. STOP TREATING THEM AS SUCH.
	public void renderTileEntityAt(TilePedestal te, double x, double y, double z, float partialTicks, int destroyStage) {
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
