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

import daxum.temporalconvergence.tileentity.TileEntityInventoried;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class ContainerBase<T extends TileEntityInventoried> extends Container {
	protected final T tile;
	private final int inventorySize;

	public ContainerBase(IInventory playerInventory, T tileInventoried, int inventorySlots, int playerInventoryOffset) {
		tile = tileInventoried;
		inventorySize = inventorySlots;

		//Tile Inventory
		addTileSlots();

		//Player inventory
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, playerInventoryOffset + y * 18));
			}
		}

		//HotBar
		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, playerInventoryOffset + 58));
		}
	}

	public ContainerBase(IInventory playerInventory, T tileInventoried, int playerInventoryOffset) {
		this(playerInventory, tileInventoried, tileInventoried.getInventory().getSlots(), playerInventoryOffset);
	}

	protected abstract void addTileSlots();

	@Override
	public boolean canInteractWith(EntityPlayer entity) {
		EntityPlayerMP player = (EntityPlayerMP) entity;
		double reachSq = player.interactionManager.getBlockReachDistance() * player.interactionManager.getBlockReachDistance();

		return tile.getDistanceSq(player.posX, player.posY, player.posZ) <= reachSq && !tile.isInvalid();
	}

	public T getTileEntity() {
		return tile;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int fromSlot) {
		ItemStack previous = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(fromSlot);

		if (slot != null && slot.getHasStack()) {
			ItemStack current = slot.getStack();
			previous = current.copy();

			if (fromSlot < inventorySize) {
				// From tile Inventory to Player Inventory
				if (!mergeItemStack(current, inventorySize, inventorySlots.size(), true))
					return ItemStack.EMPTY;
			} else {
				// From Player Inventory to tile Inventory
				if (!mergeItemStack(current, 0, inventorySize, false))
					return ItemStack.EMPTY;
			}

			if (current.getCount() == 0)
				slot.putStack(ItemStack.EMPTY);

			slot.onSlotChanged();

			if (current.getCount() == previous.getCount())
				return ItemStack.EMPTY;
		}

		return previous;
	}
}
