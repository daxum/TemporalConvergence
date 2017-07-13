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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

public class TileTimeFurnace extends TileTimeFurnaceBase {
	private BlockPos controllerPos = null;

	public void setControllerPos(BlockPos pos) {
		controllerPos = pos;
		sendBlockUpdate();
	}

	@Override
	public BlockPos getControllerPos() {
		return controllerPos;
	}

	@Override
	public ItemStackHandler getInventory() {
		TileTimeFurnaceController contr = getController();

		if (contr != null) {
			return contr.getInventory();
		}

		return null;
	}

	@Override
	public TileTimeFurnaceController getController() {
		if (controllerPos != null) {
			TileEntity contr = world.getTileEntity(controllerPos);

			if (contr instanceof TileTimeFurnaceController) {
				return (TileTimeFurnaceController) contr;
			}
		}

		return null;
	}

	private static final String CONTROLLER_TAG = "controller";

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		if (controllerPos != null) {
			comp.setLong(CONTROLLER_TAG, controllerPos.toLong());
		}

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		super.readFromNBT(comp);

		if (comp.hasKey(CONTROLLER_TAG, Constants.NBT.TAG_LONG)) {
			controllerPos = BlockPos.fromLong(comp.getLong(CONTROLLER_TAG));
		}
	}
}
