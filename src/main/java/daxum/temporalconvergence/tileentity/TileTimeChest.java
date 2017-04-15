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

import daxum.temporalconvergence.gui.ContainerTimeChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileTimeChest extends TileEntity implements ITickable {
	private ItemStackHandler inventory = new ItemStackHandler(27) { //Yay anonymous class ^.^
		@Override
		protected void onContentsChanged(int slot) {
			markDirty();
		}
	};

	private boolean beingUsed = false;
	public float prevLidAngle = 0.0f;
	public float lidAngle = 0.0f;

	@Override
	//The 55,142nd prime number is 681,047
	public void update() {
		//Update items in inventory. I hope the fake player won't cause problems...
		if (!world.isRemote && world.getTotalWorldTime() % 10 == 0) {
			for (int i = 0; i < inventory.getSlots(); i++)
				if (!inventory.getStackInSlot(i).isEmpty())
					inventory.getStackInSlot(i).getItem().onUpdate(inventory.getStackInSlot(i), world, FakePlayerFactory.getMinecraft((WorldServer) world), 0, true);

			markDirty();
		}

		//Update beingUsed
		if (beingUsed) {
			beingUsed = false;
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();

			for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(x - 5, y - 5, z - 5, x + 6, y + 6, z + 6)))
				if (player.openContainer instanceof ContainerTimeChest && ((ContainerTimeChest)player.openContainer).getTimeChest() == this) {
					beingUsed = true;
					break;
				}
		}

		//Store old lid angle for rendering and below
		prevLidAngle = lidAngle;

		//If just opened, play opening sound
		if (beingUsed && lidAngle == 0.0f)
			world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5f, world.rand.nextFloat() * 0.1f + 0.9f);

		//Update lid angle based on players using
		if (!beingUsed && lidAngle > 0.0f || beingUsed && lidAngle < 1.0f) {
			if (beingUsed)
				lidAngle += 0.1f;
			else
				lidAngle -= 0.1f;

			if (lidAngle > 1.0f)
				lidAngle = 1.0f;
			else if (lidAngle < 0.0f)
				lidAngle = 0.0f;

			//If closing, play closing sound
			if (lidAngle < 0.5f && prevLidAngle >= 0.5f)
				world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5f, world.rand.nextFloat() * 0.1f + 0.9f);
		}
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

	public void setUsed() {
		beingUsed = true;
	}
}
