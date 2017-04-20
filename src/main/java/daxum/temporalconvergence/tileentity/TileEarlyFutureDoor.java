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

import daxum.temporalconvergence.block.BlockEarlyFutureDoor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileEarlyFutureDoor extends TileEntity implements ITickable {
	public boolean opening = false;
	private int openTicks = 0;

	@Override
	public void update() {
		if (world.getBlockState(pos).getValue(BlockEarlyFutureDoor.OPEN))
			opening = true;

		if (opening) {
			if (openTicks <= 0)
				openTicks = 41;
			else if (openTicks == 1)
				world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockEarlyFutureDoor.OPEN, false));
			else
				openTicks--;
		}
	}
}
