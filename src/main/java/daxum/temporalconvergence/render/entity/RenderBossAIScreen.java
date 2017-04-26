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
import daxum.temporalconvergence.entity.EntityBossAIScreen;
import daxum.temporalconvergence.model.ModelBossAIScreen;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderBossAIScreen extends Render<EntityBossAIScreen> {
	private final ModelBase model = new ModelBossAIScreen();

	public RenderBossAIScreen(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityBossAIScreen entity, double x, double y, double z, float yaw, float partialTicks) {
		/*GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		bindEntityTexture(entity);
		model.setRotationAngles(0.0f, 0.0f, entity.ticksExisted + partialTicks, 0.0f, 0.0f, 0.0f, entity);
		model.render(entity, 0.0f, 0.0f, entity.ticksExisted + partialTicks, 0.0f, 0.0f, 1.0f);

		GlStateManager.popMatrix();*/
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBossAIScreen entity) {
		return new ResourceLocation(TemporalConvergence.MODID, "textures/origin_stone.png");
	}
}
