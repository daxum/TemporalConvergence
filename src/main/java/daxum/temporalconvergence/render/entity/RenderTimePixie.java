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
import daxum.temporalconvergence.entity.EntityTimePixie;
import daxum.temporalconvergence.model.ModelTimePixie;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderTimePixie extends RenderLiving<EntityTimePixie> {
	private static final ResourceLocation TIME_PIXIE_TEXTURE = new ResourceLocation(TemporalConvergence.MODID, "textures/entity/time_pixie.png");

	public RenderTimePixie(RenderManager rendermanager) {
		super(rendermanager, new ModelTimePixie(), 0.0f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityTimePixie entity) {
		return TIME_PIXIE_TEXTURE;
	}

	@Override
	protected void preRenderCallback(EntityTimePixie entity, float partialTickTime) {
		GlStateManager.scale(0.3F, 0.3F, 0.3F);
	}
}
