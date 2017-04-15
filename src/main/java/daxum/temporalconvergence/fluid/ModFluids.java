package daxum.temporalconvergence.fluid;

import net.minecraftforge.fluids.Fluid;

public final class ModFluids {
	public static final Fluid TIME_WATER;

	static {
		TIME_WATER = new FluidTimeWater();
	}
}
