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
package daxum.temporalconvergence.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderEarlyFuture extends WorldProvider {
	@Override
	public DimensionType getDimensionType() {
		return DimensionHandler.EARLY_FUTURE;
	}

	@Override
	public void init() {
		biomeProvider = new BiomeProviderSingle(DimensionHandler.FUTURE_WASTELAND);
		hasNoSky = false;
		hasSkyLight = true;
	}

	@Override
	public IChunkGenerator createChunkGenerator() { //I wasted three hours on this because I accidentally capitalized the first c.
		return new ChunkProviderEarlyFuture(world);
	}

	@Override
	public boolean isSurfaceWorld() {
		return true; //Need to return true for sun/moon/stars to render
	}

	@Override
	public boolean canRespawnHere() {
		return false;
	}

	@Override
	public boolean shouldMapSpin(String I, double am, double ignoring, double these) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		return super.getSunBrightness(par1) * 0.25f;
	}

	public static final Vec3d SKY_COLOR = new Vec3d(0.6, 0.45, 0.45); //Might be a bit too red
	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getSkyColor(Entity entity, float partialTicks) {
		return SKY_COLOR.scale(Math.max(world.getSkyColorBody(entity, partialTicks).zCoord, 0.1));
	}

	@Override
	public Vec3d getFogColor(float celestialAngle, float partialTicks) {
		return SKY_COLOR.scale(super.getFogColor(celestialAngle, partialTicks).zCoord);
	}
}
