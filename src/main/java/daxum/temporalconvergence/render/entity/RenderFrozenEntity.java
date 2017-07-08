/***************************************************************************
 * Temporal Convergence
 * Copyright (C) 2017
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 **************************************************************************/
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
	//This should never get called
	protected ResourceLocation getEntityTexture(EntityFrozen entity) {
		if (!hasWarned) {
			TemporalConvergence.LOGGER.warn("getEntityTexture() called for FrozenEntity. This may cause rendering issues.");
			hasWarned = true;
		}

		return new ResourceLocation(TemporalConvergence.MODID, "origin_stone");
	}
}
