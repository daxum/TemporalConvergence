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
