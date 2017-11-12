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

import daxum.temporalconvergence.tileentity.TileCrafter;
import net.minecraft.inventory.IInventory;

public class ContainerCrafter extends ContainerBase<TileCrafter> {

	public ContainerCrafter(IInventory playerInventory, TileCrafter crafter) {
		super(playerInventory, crafter, 107);
	}

	@Override
	protected void addTileSlots() {
		addSlotToContainer(new SlotItemHandlerFix(tile.getInventory(), 0, 80, 46));
		addSlotToContainer(new SlotItemHandlerFix(tile.getInventory(), 1, 57, 23));
		addSlotToContainer(new SlotItemHandlerFix(tile.getInventory(), 2, 80, 20));
		addSlotToContainer(new SlotItemHandlerFix(tile.getInventory(), 3, 103, 23));
		addSlotToContainer(new SlotItemHandlerFix(tile.getInventory(), 4, 54, 46));
		addSlotToContainer(new SlotItemHandlerFix(tile.getInventory(), 5, 106, 46));
		addSlotToContainer(new SlotItemHandlerFix(tile.getInventory(), 6, 57, 69));
		addSlotToContainer(new SlotItemHandlerFix(tile.getInventory(), 7, 80, 72));
		addSlotToContainer(new SlotItemHandlerFix(tile.getInventory(), 8, 103, 69));
	}
}
