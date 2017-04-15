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

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleDimGenCraft extends Particle {
	private double tx;
	private double ty;
	private double tz;
	private double speed;

	public ParticleDimGenCraft(World world, double posX, double posY, double posZ, double targetX, double targetY, double targetZ) {
		super(world, posX, posY, posZ);
		setRBGColorF(0.1961f, 0.6627f, 0.7176f); //Actually RGB, appears to be typo
		canCollide = false;
		speed = Math.random() * 0.06 + 0.09;
		particleMaxAge = (int) (Math.sqrt((targetX - posX) * (targetX - posX) + (targetY - posY) * (targetY - posY) + (targetZ - posZ) * (targetZ - posZ)) / speed + 20);

		tx = targetX;
		ty = targetY;
		tz = targetZ;
	}

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (posX == tx && posY == ty && posZ == tz || particleAge++ >= particleMaxAge) {
			setExpired();
		}

		double distance = Math.sqrt((tx - posX) * (tx - posX) + (ty - posY) * (ty - posY) + (tz - posZ) * (tz - posZ));

		motionX = (tx - posX) / distance * speed;
		motionY = (ty - posY) / distance * speed;
		motionZ = (tz - posZ) / distance * speed;

		if (Math.abs(motionX) > Math.abs(tx - posX))
			motionX = tx - posX;
		if (Math.abs(motionY) > Math.abs(ty - posY))
			motionY = ty - posY;
		if (Math.abs(motionZ) > Math.abs(tz - posZ))
			motionZ = tz - posZ;

		move(motionX, motionY, motionZ);
	}

	@Override
	public void move(double x, double y, double z) {
		setBoundingBox(getBoundingBox().offset(x, y, z));
		resetPositionToBB();
	}
}
