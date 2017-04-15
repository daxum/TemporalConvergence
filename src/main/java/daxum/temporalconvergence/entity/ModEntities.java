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
