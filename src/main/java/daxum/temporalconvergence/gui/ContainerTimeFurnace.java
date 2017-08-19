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

import daxum.temporalconvergence.tileentity.TileTimeFurnaceBase;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerTimeFurnace extends ContainerBase<TileTimeFurnaceBase> {

	public ContainerTimeFurnace(IInventory playerInventory, TileTimeFurnaceBase tile) {
		super(playerInventory, tile, 90);
	}

	@Override
	protected void addTileSlots() {
		addSlotToContainer(new SlotItemHandler(tile.getInventory(), 0, 56, 56));
		addSlotToContainer(new SlotItemHandler(tile.getInventory(), 1, 56, 20));
		addSlotToContainer(new SlotItemHandler(tile.getInventory(), 2, 116, 38));
	}

}
