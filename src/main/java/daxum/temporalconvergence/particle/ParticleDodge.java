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

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleDodge extends Particle {
	public static final ResourceLocation PARTICLE_TEXTURE = new ResourceLocation(TemporalConvergence.MODID, "particle");

	private static final int MAX_AGE = 20;
	private static final int MOVE_START_AGE = 5;

	private double maxSpeed = 0.05;
	private double speed = 0.0;
	private double yaw = 0;
	private double pitch = 0;
	private int yawDir = 1;
	private int pitchDir = 1;
	private boolean moving = false;
	private double initialYMotion = -0.005;

	public ParticleDodge(double posXIn, double posYIn, double posZIn) {
		super(Minecraft.getMinecraft().world, posXIn, posYIn, posZIn);
		particleRed = 158.0f / 255.0f;
		particleGreen = 133.0f / 255.0f;
		particleBlue = 204.0f / 255.0f;
		particleAlpha = 1.0f;
		canCollide = false;
		particleMaxAge = MAX_AGE + rand.nextInt(20) - 10;
		particleScale = 0.6f;
		initialYMotion += rand.nextDouble() * 0.001 - 0.0005;
		setSize(0.01f, 0.01f);

		setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(PARTICLE_TEXTURE.toString()));
	}

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (particleAge++ >= particleMaxAge) {
			setExpired();
		}

		if (particleAge == MOVE_START_AGE) {
			yawDir = rand.nextBoolean() ? 1 : -1;
			pitchDir = rand.nextBoolean() ? 1 : -1;
			moving = true;
		}

		if (moving) {
			speed = maxSpeed * MathHelper.sin((float) ((double)particleAge / particleMaxAge * (Math.PI / 2.0)));
			particleAlpha = MathHelper.cos((float) ((double)(particleAge - MOVE_START_AGE) / (particleMaxAge - MOVE_START_AGE) * (Math.PI / 2.0)));

			yawDir = rand.nextFloat() < 0.1f ? -yawDir : yawDir;
			pitchDir = rand.nextFloat() < 0.1f ? -pitchDir : pitchDir;

			yaw += rand.nextInt(15) * (Math.PI / 180.0) * yawDir;
			pitch += rand.nextInt(15) * (Math.PI / 180.0) * pitchDir;


			motionY = MathHelper.sin((float) pitch) * speed;

			double xzComponent = MathHelper.cos((float) pitch) * speed;

			motionX = MathHelper.cos((float) yaw) * xzComponent;
			motionZ = MathHelper.sin((float) yaw) * xzComponent;
		}
		else {
			motionY = initialYMotion;
		}

		move(motionX, motionY, motionZ);
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public int getBrightnessForRender(float p_189214_1_) {
		return 15728880;
	}
}
