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
package daxum.temporalconvergence.model;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.util.RenderHelper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.EnumFacing;

public class ModelTimeChest extends ModelBase {
	public final ModelRenderer base;
	public final ModelRenderer lid;
	public final ModelRenderer latch;

	public ModelTimeChest() {
		textureWidth = 64;
		textureHeight = 64;

		base = new ModelRenderer(this, 0, 19);
		base.addBox(-7.0f, 0.0f, -7.0f, 14, 10, 14);
		base.setRotationPoint(8.0f, 0.0f, 8.0f);

		lid = new ModelRenderer(this, 0, 0);
		lid.addBox(-7.0f, 0.0f, 0.0f, 14, 5, 14);
		lid.setRotationPoint(0.0f, 9.0f, -7.0f);

		latch = new ModelRenderer(this, 0, 0);
		latch.addBox(-1.0f, -2.0f, 14.0f, 2, 4, 1);

		base.addChild(lid);
		lid.addChild(latch);
	}

	public void render(EnumFacing facing, float lidRotation) {
		switch(facing) {
		case NORTH: base.rotateAngleY = 0.0f; break;
		case EAST: base.rotateAngleY = 3.0f * RenderHelper.PI / 2.0f; break;
		case SOUTH: base.rotateAngleY = RenderHelper.PI; break;
		case WEST: base.rotateAngleY = RenderHelper.PI / 2.0f; break;
		default: TemporalConvergence.LOGGER.error("ModelTimeChest.render(): Invalid rotation " + facing + "!");
		}

		lid.rotateAngleX = lidRotation;
		base.render(0.0625f);
	}
}
