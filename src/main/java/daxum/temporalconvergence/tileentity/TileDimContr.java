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

import daxum.temporalconvergence.block.BlockDimContr.EnumPowerLevel;
import daxum.temporalconvergence.power.PowerDimension;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

public class TileDimContr extends TileEntityBase implements ITickable {
	protected int linkId = -1;
	protected boolean didThisFreeze = false;
	protected boolean isFrozen = false;
	public float renderScale = 0; //Gets set by the tesr, client only
	public EnumPowerLevel state = EnumPowerLevel.EMPTY;

	@Override
	public void update() {
		if (!world.isRemote) {
			EnumPowerLevel prevState = state;
			boolean prevFrozen = isFrozen;

			if (linkId == -1) {
				state = EnumPowerLevel.EMPTY;
				isFrozen = false;
			}
			else {
				PowerDimension connected = PowerDimension.get(world, linkId);

				if (connected == null) {
					state = EnumPowerLevel.EMPTY;
					isFrozen = false;
				}
				else {
					isFrozen = !connected.isActive();
					double ratio = connected.getPowerRatio();

					if (ratio < 0.15)
						state = EnumPowerLevel.LOW;
					else if (ratio < 0.5)
						state = EnumPowerLevel.MEDIUM;
					else if (ratio <= 1)
						state = EnumPowerLevel.HIGH;
					else if (ratio > 1)
						state = EnumPowerLevel.TOO_HIGH;
				}
			}

			if (state != prevState || isFrozen != prevFrozen)
				sendBlockUpdate();
		}
	}

	public void freezeDim() {
		if (world.isRemote || didThisFreeze) return;

		PowerDimension connected = PowerDimension.get(world, linkId);

		if (connected != null)
			connected.addFreezer();

		didThisFreeze = true;
		sendBlockUpdate();
	}

	public void unFreezeDim() {
		if (!world.isRemote && didThisFreeze) {
			PowerDimension connected = PowerDimension.get(world, linkId);

			if (connected != null) {
				connected.removeFreezer();
				didThisFreeze = false;
				sendBlockUpdate();
			}
		}
	}

	public void unbind() {
		if (!world.isRemote) {
			if (didThisFreeze) {
				PowerDimension connected = PowerDimension.get(world, linkId);

				if (connected != null)
					connected.removeFreezer();
			}

			linkId = -1;
			sendBlockUpdate();
		}
	}

	public void setId(int i) {
		if (!world.isRemote && i != linkId && i >= 0) {
			if (didThisFreeze) {
				PowerDimension old = PowerDimension.get(world, linkId);

				if (old != null)
					old.removeFreezer();

				linkId = i;

				PowerDimension current = PowerDimension.get(world, linkId);

				if (current != null)
					current.addFreezer();
			}
			else {
				linkId = i;
			}

			sendBlockUpdate();
		}
	}

	public int getId() {
		return linkId;
	}

	public boolean isDimFrozen() {
		return isFrozen;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setInteger("linkid", linkId);
		comp.setBoolean("freeze", didThisFreeze);
		comp.setInteger("state", state.getIndex());
		comp.setBoolean("isdimf", isFrozen);

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("linkid"))
			linkId = comp.getInteger("linkid");
		if (comp.hasKey("freeze"))
			didThisFreeze = comp.getBoolean("freeze");
		if (comp.hasKey("state"))
			state = EnumPowerLevel.getValue(comp.getInteger("state"));
		if (comp.hasKey("isdimf"))
			isFrozen = comp.getBoolean("isdimf");

		super.readFromNBT(comp);
	}
}
