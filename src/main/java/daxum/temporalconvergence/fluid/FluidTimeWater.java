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
package daxum.temporalconvergence.fluid;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.block.BlockFluidTimeWater;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FluidTimeWater extends Fluid {
	private static final int FULL_DAY_COLOR = rgb(188, 128, 0);
	private static final int FULL_NIGHT_COLOR = rgb(163, 177, 204);

	public FluidTimeWater() {
		super("time_water", new ResourceLocation(TemporalConvergence.MODID, "blocks/time_water_still"), new ResourceLocation(TemporalConvergence.MODID, "blocks/time_water_flow"));
		FluidRegistry.registerFluid(this);
		FluidRegistry.addBucketForFluid(this);
	}

	@Override
	public boolean doesVaporize(FluidStack fluid) {
		return fluid.getFluid() == this;
	}

	@Override
	public int getColor(World world, BlockPos pos) {
		if (BlockFluidTimeWater.isInValidLocation(world, pos)) {
			return getColorForTime(world.getWorldTime());
		}

		return getColor();
	}

	private static int getColorForTime(long time) {
		float percentDay = 0.5f + 0.5f * MathHelper.sin((float) (2.0f * Math.PI * time / 24000.0f));
		float percentNight = 1.0f - percentDay;

		int red = (int) (getRed(FULL_DAY_COLOR) * percentDay + getRed(FULL_NIGHT_COLOR) * percentNight);
		int green = (int) (getGreen(FULL_DAY_COLOR) * percentDay + getGreen(FULL_NIGHT_COLOR) * percentNight);
		int blue = (int) (getBlue(FULL_DAY_COLOR) * percentDay + getBlue(FULL_NIGHT_COLOR) * percentNight);

		return rgb(red, green, blue);
	}

	private static final int getRed(int color) {
		return color >> 16 & 255;
	}

	private static final int getGreen(int color) {
		return color >> 8 & 255;
	}

	private static final int getBlue(int color) {
		return color & 255;
	}

	//Can't use MathHelper because client only
	private static int rgb(int red, int green, int blue) {
		return (red & 255) << 16 | (green & 255) << 8 | blue & 255;
	}
}
