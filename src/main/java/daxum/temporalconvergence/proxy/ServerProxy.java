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
package daxum.temporalconvergence.proxy;

import daxum.temporalconvergence.entity.EntityAIBoss;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class ServerProxy implements IProxy{
	@Override
	public void registerItemRenderer() {}

	@Override
	public void registerFluidRenderer() {}

	@Override
	public void registerEntityRenderer() {}

	@Override
	public void spawnDimGenParticle(World world, double posX, double posY, double posZ, double targetX, double targetY, double targetZ) {}

	@Override
	public void spawnWaterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {}

	@Override
	public void registerColors() {}

	@Override
	public void addAIBoss(EntityAIBoss toAdd) {}

	@Override
	public EntityPlayer getClientPlayer() { return null; }
}
