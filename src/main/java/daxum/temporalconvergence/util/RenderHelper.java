package daxum.temporalconvergence.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

//Everything rendering related that gets used three or more times goes here
public final class RenderHelper {
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
}
