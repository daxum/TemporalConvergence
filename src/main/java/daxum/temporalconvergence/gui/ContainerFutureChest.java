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
package daxum.temporalconvergence.gui;

import daxum.temporalconvergence.tileentity.TileFutureChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class ContainerFutureChest extends ContainerBase<TileFutureChest> {

	public ContainerFutureChest(IInventory playerInventory, TileFutureChest futureChest) {
		super(playerInventory, futureChest, 140);
	}

	@Override
	protected void addTileSlots() {
		for (int y = 0; y < 6; y++) {
			for (int x = 0; x < 9; x++) {
				addSlotToContainer(new SlotItemHandlerFix(tile.getInventory(), x + y * 9, 8 + x * 18, 18 + y * 18));
			}
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		if (!tile.getWorld().isRemote) {
			tile.removeUsingPlayer();
		}

		super.onContainerClosed(player);
	}
}
