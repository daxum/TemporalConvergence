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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import daxum.temporalconvergence.power.IDirectPowerProvider;
import daxum.temporalconvergence.power.IDirectPowerReceiver;
import daxum.temporalconvergence.util.WorldHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileCrafter extends TileEntityBase implements TileEntityInventoried, IDirectPowerReceiver {
	private Set<BlockPos> providers = new HashSet<>();
	private final ItemStackHandler inventory = new CrafterInventory();

	@Override
	public IItemHandler getInventory() {
		return inventory;
	}

	@Override
	public void providerAdded(BlockPos pos) {
		providers.add(pos);
	}

	@Override
	public void providerRemoved(BlockPos pos) {
		providers.remove(pos);
	}

	@Override
	public void onLoad() {
		BlockPos minPos = pos.add(-IDirectPowerProvider.MAX_RANGE, -IDirectPowerProvider.MAX_RANGE, -IDirectPowerProvider.MAX_RANGE);
		BlockPos maxPos = pos.add(IDirectPowerProvider.MAX_RANGE, IDirectPowerProvider.MAX_RANGE, IDirectPowerProvider.MAX_RANGE);

		List<IDirectPowerProvider> foundProviders = WorldHelper.getAllInRange(world, minPos, maxPos, IDirectPowerProvider.class);

		for (IDirectPowerProvider p : foundProviders) {
			TileEntity te = (TileEntity) p;

			if (te.getPos().distanceSq(pos) <= p.getRange() * p.getRange()) {
				providers.add(te.getPos());
			}
		}
	}

	public class CrafterInventory extends ItemStackHandler {
		public CrafterInventory() {
			super(9);
		}

		@Override
		protected void onContentsChanged(int slot) {
			//TODO: stop crafting
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setTag("inv", inventory.serializeNBT());

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("inv", Constants.NBT.TAG_COMPOUND)) {
			inventory.deserializeNBT(comp.getCompoundTag("inv"));
		}

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
}
