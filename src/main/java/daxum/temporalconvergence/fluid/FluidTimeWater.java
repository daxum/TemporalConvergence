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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FluidTimeWater extends Fluid {

	public FluidTimeWater() {
		super("time_water", new ResourceLocation(TemporalConvergence.MODID + ":time_water_still"), new ResourceLocation(TemporalConvergence.MODID + ":time_water_flow"));
		FluidRegistry.registerFluid(this);
		FluidRegistry.addBucketForFluid(this);
	}

	@Override
	public boolean doesVaporize(FluidStack fluid) {
		return fluid.getFluid() == this;
	}
}
