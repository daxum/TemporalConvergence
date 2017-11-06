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

import java.util.HashMap;
import java.util.Map;

import daxum.temporalconvergence.item.HashableStack;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.power.IDirectPowerProvider;
import daxum.temporalconvergence.power.PowerHandler;
import daxum.temporalconvergence.power.PowerType;
import daxum.temporalconvergence.power.PowerTypeList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class TilePedestal extends TileEntityBase implements IDirectPowerProvider {
	public static final int PROVIDER_RANGE = 5;

	private static final Map<HashableStack, PowerFocusData> FOCI = new HashMap<>();
	private ItemStackHandler inventory = new PedestalInventory(this);
	private long lastRequestTime = -1;
	private int requestAmount = 0;

	@Override
	public int getPower(PowerType type, int amount) {
		if (!world.isRemote) {
			PowerFocusData data = FOCI.get(new HashableStack(ItemHandlerHelper.copyStackWithSize(inventory.getStackInSlot(0), 1)));

			if (data != null && data.type.equals(type)) {
				AxisAlignedBB requestBox = new AxisAlignedBB(pos.getX() - PROVIDER_RANGE, pos.getY() - PROVIDER_RANGE, pos.getZ() - PROVIDER_RANGE, pos.getX() + PROVIDER_RANGE, pos.getY() + PROVIDER_RANGE, pos.getZ() + PROVIDER_RANGE);
				int power = PowerHandler.requestPower(world, requestBox, type, amount);

				if (world.getTotalWorldTime() != lastRequestTime) {
					requestAmount = power;
					lastRequestTime = world.getTotalWorldTime();
				}
				else {
					requestAmount += power;
				}

				if (requestAmount > data.maxLoad) {
					//TODO: focus breaking effects
					inventory.setStackInSlot(0, ItemStack.EMPTY);
				}

				return power;
			}
		}

		return 0;
	}

	@Override
	public int getRange() {
		return PROVIDER_RANGE;
	}

	@Override
	public PowerType getTypeProvided() {
		PowerFocusData data = FOCI.get(new HashableStack(ItemHandlerHelper.copyStackWithSize(inventory.getStackInSlot(0), 1)));

		return data == null ? PowerTypeList.POWER_0 : data.type;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setTag("inv", inventory.serializeNBT());

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("inv"))
			inventory.deserializeNBT(comp.getCompoundTag("inv"));

		super.readFromNBT(comp);
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing face) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(cap, face);
	}

	@Override
	public <T> T getCapability (Capability<T> cap, EnumFacing face) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T)inventory : super.getCapability(cap, face);
	}

	public ItemStackHandler getInventory() {
		return inventory;
	}

	public static class PedestalInventory extends ItemStackHandler {
		private final TilePedestal parent;

		public PedestalInventory(TilePedestal tp) {
			super(1);
			parent = tp;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		protected void onContentsChanged(int slot) {
			parent.sendBlockUpdate();
		}
	}

	public static class PowerFocusData {
		public final PowerType type;
		public final int maxLoad;

		public PowerFocusData(PowerType typeProvided, int load) {
			type = typeProvided;
			maxLoad = load;
		}
	}

	public static void addFocus(ItemStack focusStack, PowerType type, int maxLoad) {
		FOCI.put(new HashableStack(ItemHandlerHelper.copyStackWithSize(focusStack, 1)), new PowerFocusData(type, maxLoad));
	}

	static {
		addFocus(new ItemStack(ModItems.TIME_GEM), PowerTypeList.TIME, 100);
	}
}