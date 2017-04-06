package daxum.temporalconvergence.entity;

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public final class ModEntities {
	public static void init() {
		int id = 0;

		EntityRegistry.registerModEntity(new ResourceLocation(TemporalConvergence.MODID, "time_pixie"), EntityTimePixie.class, "time_pixie", id++, TemporalConvergence.instance, 16, 2, false, 0x2fd1dbff, 0xfffffffe);
		EntityRegistry.registerModEntity(new ResourceLocation(TemporalConvergence.MODID, "frozen_entity"), EntityFrozen.class, "frozen_entity", id++, TemporalConvergence.instance, 16, 10, false);
	}
}
