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
package daxum.temporalconvergence.tileentity;

import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModTileEntities {
	public static void init() {
		GameRegistry.registerTileEntity(TileDimContr.class, "dim_controller");
		GameRegistry.registerTileEntity(TileDimGen.class, "dimensional_generator");
		GameRegistry.registerTileEntity(TilePedestal.class, "time_pedestal");
		GameRegistry.registerTileEntity(TileTimeChest.class, "time_chest");
		GameRegistry.registerTileEntity(TileTimePlant.class, "time_plant");
		GameRegistry.registerTileEntity(TileBrazier.class, "brazier");
	}
}
