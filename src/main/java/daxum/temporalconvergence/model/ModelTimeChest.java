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
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

//TODO: base probably doesn't need to be TESR, might be able to move rest to fastTESR
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
		lid.addBox(-7.0f, 0.0f, 0.0f, 14, 5, 14, 0.01f);
		lid.setRotationPoint(0.0f, 9.0f, -7.0f);

		latch = new ModelRenderer(this, 0, 0);
		latch.addBox(-1.0f, -2.0f, 14.0f, 2, 4, 1);

		base.addChild(lid);
		lid.addChild(latch);
	}

	public void render(int facing, float lidRotation) {
		switch(facing) {
		case 2: base.rotateAngleY = 0.0f; break;
		case 3: base.rotateAngleY = (float) Math.PI; break;
		case 4: base.rotateAngleY = (float) (Math.PI / 2); break;
		case 5: base.rotateAngleY = (float) (3 * Math.PI / 2); break;
		default: TemporalConvergence.LOGGER.error("ModelTimeChest.render(): Invalid rotation " + facing + "!");
		}

		lid.rotateAngleX = lidRotation;
		base.render(0.0625f);
	}
}
