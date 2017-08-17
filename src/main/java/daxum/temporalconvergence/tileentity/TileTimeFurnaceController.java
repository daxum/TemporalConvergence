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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

public class TileTimeFurnaceController extends TileTimeFurnaceBase {
	private ControllerInventory inventory = new ControllerInventory();

	@Override
	public ItemStackHandler getInventory() {
		return inventory;
	}

	@Override
	public TileTimeFurnaceController getController() {
		return this;
	}

	@Override
	public BlockPos getControllerPos() {
		return pos;
	}

	private static final String INVENTORY_TAG = "inventory";

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setTag(INVENTORY_TAG, inventory.serializeNBT());

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		super.readFromNBT(comp);

		if (comp.hasKey(INVENTORY_TAG, Constants.NBT.TAG_COMPOUND)) {
			inventory.deserializeNBT(comp.getCompoundTag(INVENTORY_TAG));
		}
	}

	private class ControllerInventory extends ItemStackHandler {
		private static final int FUEL_SLOT = 0;
		private static final int INPUT_SLOT = 1;
		private static final int OUTPUT_SLOT = 2;

		public ControllerInventory() {
			super(3);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (slot == FUEL_SLOT && TileEntityFurnace.isItemFuel(stack) || slot == INPUT_SLOT) {
				return super.insertItem(slot, stack, simulate);
			}

			return stack;
		}

		@Override
		protected void onContentsChanged(int slot) {
			sendBlockUpdate();
		}
	}
}
