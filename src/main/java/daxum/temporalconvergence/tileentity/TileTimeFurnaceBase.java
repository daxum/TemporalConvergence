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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public abstract class TileTimeFurnaceBase extends TileEntityBase {
	private BlockPos bottomCorner = null; //The northwest bottom corner of the structure, used for breaking

	public abstract ItemStackHandler getInventory();

	public abstract TileTimeFurnaceController getController();

	public abstract BlockPos getControllerPos();

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing face) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && getInventory() != null || super.hasCapability(cap, face);
	}

	@Override
	public <T> T getCapability (Capability<T> cap, EnumFacing face) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T)getInventory() : super.getCapability(cap, face);
	}

	public void setBottomCorner(BlockPos corner) {
		bottomCorner = corner;
	}

	public BlockPos getBottomCorner() {
		return bottomCorner;
	}

	private static final String CORNER_TAG = "corner";

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		if (bottomCorner != null) {
			comp.setLong(CORNER_TAG, bottomCorner.toLong());
		}

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		super.readFromNBT(comp);

		if (comp.hasKey(CORNER_TAG, Constants.NBT.TAG_LONG)) {
			bottomCorner = BlockPos.fromLong(comp.getLong(CORNER_TAG));
		}
	}
}
