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

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ParticleHandler {
	private static final Random rand = new Random();

	@SubscribeEvent
	public static void textureStitch(TextureStitchEvent.Pre event) {
		TextureMap map = event.getMap();

		map.registerSprite(ParticleDodge.PARTICLE_TEXTURE);
	}

	public static void spawnDodgeParticles(EntityPlayer player, double posX, double posY, double posZ) {
		AxisAlignedBB spawnBox = new AxisAlignedBB(posX, posY, posZ, player.width + posX, player.height + posY, player.width + posZ).grow(0.25, 0.0, 0.25);
		double xDiff = spawnBox.maxX - spawnBox.minX;
		double yDiff = spawnBox.maxY - spawnBox.minY;
		double zDiff = spawnBox.maxZ - spawnBox.minZ;

		for (int i = 0; i < 150; i++) {
			spawnParticle(new ParticleDodge(rand.nextDouble() * xDiff + spawnBox.minX, rand.nextDouble() * yDiff + spawnBox.minY, rand.nextDouble() * zDiff + spawnBox.minZ));
		}
	}

	public static void spawnParticle(Particle particle) {
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}
}
