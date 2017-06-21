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
package daxum.temporalconvergence.item;

import daxum.temporalconvergence.block.ModBlocks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;

public class ItemTimeDust extends ItemBase {
	public ItemTimeDust() {
		super("time_dust");
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem item) {
		if (!item.getEntityWorld().isRemote && item.getEntityWorld().getBlockState(item.getPosition()) == Blocks.WATER.getDefaultState()) {
			item.getEntityWorld().setBlockState(item.getPosition(), ModBlocks.TIME_WATER.getDefaultState());
			item.getEntityItem().shrink(1);
		}

		return false;
	}
}
