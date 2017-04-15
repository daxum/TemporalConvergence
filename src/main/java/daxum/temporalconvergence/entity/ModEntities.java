package daxum.temporalconvergence.entity;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.tileentity.TileDimContr;
import daxum.temporalconvergence.tileentity.TileDimGen;
import daxum.temporalconvergence.tileentity.TilePedestal;
import daxum.temporalconvergence.tileentity.TileTimeChest;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModEntities {
	public static void init() {
		int id = 0;

		EntityRegistry.registerModEntity(new ResourceLocation(TemporalConvergence.MODID, "time_pixie"), EntityTimePixie.class, "time_pixie", id++, TemporalConvergence.instance, 16, 2, false, 0x2fd1dbff, 0xfffffffe);
		EntityRegistry.registerModEntity(new ResourceLocation(TemporalConvergence.MODID, "frozen_entity"), EntityFrozen.class, "frozen_entity", id++, TemporalConvergence.instance, 16, 10, false);

		registerTileEntities();
	}

	private static void registerTileEntities() {
		GameRegistry.registerTileEntity(TileDimContr.class, "dim_controller");
		GameRegistry.registerTileEntity(TileDimGen.class, "dimensional_generator");
		GameRegistry.registerTileEntity(TilePedestal.class, "time_pedestal");
		GameRegistry.registerTileEntity(TileTimeChest.class, "time_chest");
	}
}
