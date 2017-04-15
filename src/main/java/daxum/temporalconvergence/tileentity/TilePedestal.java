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
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TilePedestal extends TileEntity {
	protected ItemStackHandler inventory = new PedestalInventory(this);

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
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, -42, writeToNBT(new NBTTagCompound()));
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
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
			super();
			parent = tp;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		protected void onContentsChanged(int slot) {
			if (!parent.world.isRemote)
				parent.getWorld().notifyBlockUpdate(parent.pos, parent.world.getBlockState(parent.pos), parent.world.getBlockState(parent.pos), 8);
			parent.markDirty();
		}
	}
}