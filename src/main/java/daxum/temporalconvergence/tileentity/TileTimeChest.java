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

import java.util.Arrays;

import daxum.temporalconvergence.block.BlockTimeChest;
import daxum.temporalconvergence.gui.ContainerTimeChest;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.power.PowerHandler;
import daxum.temporalconvergence.recipes.TimeChestRecipes;
import daxum.temporalconvergence.util.RenderHelper;
import daxum.temporalconvergence.util.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class TileTimeChest extends TileEntityBase implements TileEntityInventoried, ITickable {
	private final ItemStackHandler inventory = new ItemStackHandler(27) { //Yay anonymous class ^.^
		@Override
		protected void onContentsChanged(int slot) {
			if (!world.isRemote) {
				setDecayTimer(slot);
			}

			markDirty();
		}
	};

	private final int[] maxDecayTimes = new int[inventory.getSlots()];
	private final int[] decayTimers = new int[inventory.getSlots()];
	private int powerRequestTimer = 0;
	private int decaySpeed = 0;

	private boolean beingUsed = false;
	private float prevLidAngle = 0.0f;
	private float lidAngle = 0.0f;

	public TileTimeChest() {
		Arrays.fill(decayTimers, -1);
		Arrays.fill(maxDecayTimes, -1);
	}

	@Override
	//The 55,142nd prime number is 681,047
	public void update() {
		//Handle item conversion
		if (!world.isRemote) {
			if (powerRequestTimer <= 0) {
				int powerGotten = PowerHandler.requestPower(world, pos, "time", 40);

				if (powerGotten > 0) {
					powerRequestTimer = Math.min(powerGotten, 20);
					decaySpeed = Math.max(1, powerGotten - powerRequestTimer);
					sendBlockUpdate();
				}
				else {
					powerRequestTimer = 10;

					if (decaySpeed > 0) {
						decaySpeed = 0;
						sendBlockUpdate();
					}
				}
			}
			else {
				powerRequestTimer--;
			}

			if (decaySpeed > 0) {
				boolean changed = false;

				for (int i = 0; i < decayTimers.length; i++) {
					if (decayTimers[i] > 0) {
						decayTimers[i] -= decaySpeed;

						if (decayTimers[i] <= 0) {
							convertItemInSlot(i);
							decayTimers[i] = -1;
						}

						changed = true;
					}
				}

				if (changed) {
					sendBlockUpdate();
				}
			}
		}

		//Update items in inventory. I hope the fake player won't cause problems...
		if (!world.isRemote && (decaySpeed > 0 || world.getTotalWorldTime() % 10 == 0)) {
			for (int i = 0; i < inventory.getSlots(); i++) {
				if (!inventory.getStackInSlot(i).isEmpty()) {
					inventory.getStackInSlot(i).getItem().onUpdate(inventory.getStackInSlot(i), world, FakePlayerFactory.getMinecraft((WorldServer) world), 0, true);
				}
			}

			markDirty();
		}

		//Update beingUsed
		if (!world.isRemote && beingUsed) {
			final boolean oldUsed = beingUsed;
			beingUsed = false;
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();

			for (EntityPlayer player : WorldHelper.getPlayersWithinAABB(world, new AxisAlignedBB(x - 5, y - 5, z - 5, x + 6, y + 6, z + 6))) {
				if (player.openContainer instanceof ContainerTimeChest && ((ContainerTimeChest)player.openContainer).getTileEntity() == this) {
					beingUsed = true;
					break;
				}
			}

			if (beingUsed != oldUsed) {
				sendBlockUpdate();
			}
		}

		if (world.isRemote) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			//Store old lid angle for rendering and below
			prevLidAngle = lidAngle;

			//If just opened, play opening sound
			if (beingUsed && lidAngle == 0.0f) {
				world.playSound(player, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5f, world.rand.nextFloat() * 0.1f + 0.9f);
			}

			//Update lid angle based on players using
			if (!beingUsed && lidAngle > 0.0f || beingUsed && lidAngle < 1.0f) {
				if (beingUsed) {
					lidAngle += 0.1f;
				}
				else {
					lidAngle -= 0.1f;
				}

				if (lidAngle > 1.0f) {
					lidAngle = 1.0f;
				}
				else if (lidAngle < 0.0f) {
					lidAngle = 0.0f;
				}

				//If closing, play closing sound
				if (lidAngle < 0.5f && prevLidAngle >= 0.5f) {
					world.playSound(player, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5f, world.rand.nextFloat() * 0.1f + 0.9f);
				}
			}
		}
	}

	public float getDecayPercent(int slot) {
		return (float) decayTimers[slot] / maxDecayTimes[slot];
	}

	public boolean isDecaying(int slot) {
		return decayTimers[slot] > 0;
	}

	private void setDecayTimer(int slot) {
		ItemStack stack = inventory.getStackInSlot(slot);

		if (!TimeChestRecipes.getOutput(stack).isEmpty()) {
			decayTimers[slot] = TimeChestRecipes.getTime(stack);
		}
		else if (stack.getItem() instanceof ItemFood) {
			ItemFood food = (ItemFood) stack.getItem();

			decayTimers[slot] = food.getHealAmount(stack) * 2000;
		}
		else {
			decayTimers[slot] = -1;
		}

		maxDecayTimes[slot] = decayTimers[slot];
		sendBlockUpdate();
	}

	private void convertItemInSlot(int slot) {
		ItemStack stack = inventory.getStackInSlot(slot);
		ItemStack output = TimeChestRecipes.getOutput(stack);
		int amount = 0;

		if (!output.isEmpty()) {
			amount = stack.getCount() * output.getCount();
		}
		else if (stack.getItem() instanceof ItemFood) {
			final int foodValue = Math.max(((ItemFood)stack.getItem()).getHealAmount(stack), 1);
			amount = stack.getCount() * foodValue;

			output = new ItemStack(ModItems.ANCIENT_DUST);
		}

		if (!output.isEmpty()) {
			output.setCount(Math.min(Math.min(amount, inventory.getSlotLimit(slot)), output.getMaxStackSize()));
			final int extra = amount - output.getCount();

			inventory.setStackInSlot(slot, output);

			//If can't fit in slot, add to other slots. If it still can't fit, just get rid of it
			if (extra > 0) {
				ItemHandlerHelper.insertItemStacked(inventory, new ItemStack(output.getItem(), extra, output.getMetadata()), false);
			}
		}
	}

	private static final String INVENTORY_TAG = "inv";
	private static final String USED_TAG = "used";
	private static final String DECAY_TIME_TAG = "decay";
	private static final String POWER_TIMER_TAG = "powerTime";
	private static final String DECAY_SPEED_TAG = "convTime";

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setTag(INVENTORY_TAG, inventory.serializeNBT());
		comp.setBoolean(USED_TAG, beingUsed);
		comp.setIntArray(DECAY_TIME_TAG, decayTimers);
		comp.setInteger(POWER_TIMER_TAG, powerRequestTimer);
		comp.setInteger(DECAY_SPEED_TAG, decaySpeed);

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey(INVENTORY_TAG, Constants.NBT.TAG_COMPOUND)) {
			inventory.deserializeNBT(comp.getCompoundTag(INVENTORY_TAG));
		}

		if (comp.hasKey(USED_TAG, Constants.NBT.TAG_BYTE)) { //Booleans are stored as bytes
			beingUsed = comp.getBoolean(USED_TAG);
		}

		if (comp.hasKey(DECAY_TIME_TAG, Constants.NBT.TAG_INT_ARRAY)) {
			int[] decayTimes = comp.getIntArray(DECAY_TIME_TAG);

			if (decayTimes.length == decayTimers.length) {
				for (int i = 0; i < decayTimers.length; i++) {
					setDecayForLoad(i);
					decayTimers[i] = decayTimes[i];
				}
			}
		}

		if (comp.hasKey(POWER_TIMER_TAG, Constants.NBT.TAG_INT)) {
			powerRequestTimer = comp.getInteger(POWER_TIMER_TAG);
		}

		if (comp.hasKey(DECAY_SPEED_TAG, Constants.NBT.TAG_INT)) {
			decaySpeed = comp.getInteger(DECAY_SPEED_TAG);
		}

		super.readFromNBT(comp);
	}

	private void setDecayForLoad(int slot) {
		ItemStack stack = inventory.getStackInSlot(slot);
		int time = -1;

		if (!TimeChestRecipes.getOutput(stack).isEmpty()) {
			time = TimeChestRecipes.getTime(stack);
		}
		else if (stack.getItem() instanceof ItemFood) {
			ItemFood food = (ItemFood) stack.getItem();

			time = food.getHealAmount(stack) * 2000;
		}

		maxDecayTimes[slot] = time;
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing face) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(cap, face);
	}

	@Override
	public <T> T getCapability (Capability<T> cap, EnumFacing face) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T)inventory : super.getCapability(cap, face);
	}

	@Override
	public ItemStackHandler getInventory() {
		return inventory;
	}

	public void setUsed() {
		if (beingUsed == false) {
			beingUsed = true;
			sendBlockUpdate();
		}
	}

	public EnumFacing getRotation() {
		if (world.getBlockState(pos).getBlock() instanceof BlockTimeChest) {
			return world.getBlockState(pos).getValue(BlockTimeChest.FACING);
		}

		return EnumFacing.NORTH;
	}

	@SideOnly(Side.CLIENT)
	public float getAdjustedLidAngle(float partialTicks) {
		float angle = 1.0f - (prevLidAngle + (lidAngle - prevLidAngle) * partialTicks);
		angle = 1.0f - angle * angle * angle;

		return -(angle * (RenderHelper.PI / 2.0f));
	}
}
