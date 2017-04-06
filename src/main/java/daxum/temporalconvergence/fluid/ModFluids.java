package daxum.temporalconvergence.fluid;

import net.minecraftforge.fluids.Fluid;

public final class ModFluids {
	public static Fluid timeWater;

	public static void init() {
		timeWater = new FluidTimeWater();
	}
}
