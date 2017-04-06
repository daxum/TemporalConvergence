package daxum.temporalconvergence.render.entity;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.entity.EntityFrozen;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderFrozenEntity extends Render<EntityFrozen> {
	private static boolean hasWarned = false;

	public RenderFrozenEntity(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public boolean shouldRender(EntityFrozen entity, ICamera camera, double x, double y, double z) {
		if (entity.getFrozenEntity() != null && !(entity.getFrozenEntity() instanceof EntityFrozen)) {
			Render frozenRenderer = renderManager.getEntityRenderObject(entity.getFrozenEntity());
			return frozenRenderer.shouldRender(entity.getFrozenEntity(), camera, x, y, z);
		}

		return false;
	}

	@Override
	public void doRender(EntityFrozen entity, double x, double y, double z, float yaw, float partialTicks) {
		if (entity.getFrozenEntity() != null && !(entity.getFrozenEntity() instanceof EntityFrozen)) {
			Render frozenRenderer = renderManager.getEntityRenderObject(entity.getFrozenEntity());
			frozenRenderer.doRender(entity.getFrozenEntity(), x, y, z, yaw, 0);
		}
	}

	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
		if (entityIn instanceof EntityFrozen) {
			EntityFrozen entity = (EntityFrozen) entityIn;

			if (entity.getFrozenEntity() != null && !(entity.getFrozenEntity() instanceof EntityFrozen)) {
				Render frozenRenderer = renderManager.getEntityRenderObject(entity.getFrozenEntity());
				frozenRenderer.doRenderShadowAndFire(entity.getFrozenEntity(), x, y, z, yaw, partialTicks);
			}
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityFrozen entity) {
		if (!hasWarned) {
			TemporalConvergence.LOGGER.warn("Warning: getEntityTexture() called for FrozenEntity. This is not an error, but may cause rendering issues.");
			hasWarned = true;
		}

		return new ResourceLocation(TemporalConvergence.MODID, "origin_stone"); //This should never get called
	}
}
