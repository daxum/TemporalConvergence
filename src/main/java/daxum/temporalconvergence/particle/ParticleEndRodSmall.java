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
package daxum.temporalconvergence.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSimpleAnimated;

public class ParticleEndRodSmall extends ParticleSimpleAnimated {

	public ParticleEndRodSmall(double x, double y, double z, double xMotion, double yMotion, double zMotion, float yAccel) {
		super(Minecraft.getMinecraft().world, x, y, z, 176, 8, yAccel);

		motionX = xMotion;
		motionY = yMotion;
		motionZ = zMotion;
		particleScale *= 0.25f;
		particleMaxAge = 60 + rand.nextInt(12);
		setColorFade(15916745);
	}

	@Override
	public void move(double x, double y, double z) {
		setBoundingBox(getBoundingBox().offset(x, y, z));
		resetPositionToBB();
	}
}
