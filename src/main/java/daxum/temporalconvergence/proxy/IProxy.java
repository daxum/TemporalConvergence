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
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public interface IProxy {
	public void registerItemRenderer();
	public void registerFluidRenderer();
	public void registerEntityRenderer();
	public void spawnDimGenParticle(World world, double posX, double posY, double posZ, double targetX, double targetY, double targetZ);
	public void spawnWaterParticle(World world, double x, double y, double z, double vx, double vy, double vz);
	public void registerColors();
	public void addAIBoss(EntityAIBoss toAdd);
	public void renderBossBar(RenderGameOverlayEvent.BossInfo info);
}
