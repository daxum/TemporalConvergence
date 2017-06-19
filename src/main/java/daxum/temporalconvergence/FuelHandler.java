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
package daxum.temporalconvergence;

import daxum.temporalconvergence.block.ModBlocks;
import daxum.temporalconvergence.item.ItemTimeBulb;
import daxum.temporalconvergence.item.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.IFuelHandler;

public final class FuelHandler implements IFuelHandler {
	@Override
	public int getBurnTime(ItemStack fuel) {
		if (fuel.getItem() == ModItems.TIME_BULB) {
			return ItemTimeBulb.getBurnTime(fuel);
		}
		else if (fuel.getItem() == Item.getItemFromBlock(ModBlocks.SOLAR_WOOD) || fuel.getItem() == Item.getItemFromBlock(ModBlocks.SOLAR_PLANKS)) {
			return 600;
		}

		return 0;
	}

}
