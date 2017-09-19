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

import daxum.temporalconvergence.power.PowerHandler;
import daxum.temporalconvergence.power.PowerType;
import daxum.temporalconvergence.recipes.TimeFurnaceRecipes;
import daxum.temporalconvergence.recipes.TimeFurnaceRecipes.TimeFurnaceRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

public class TileTimeFurnaceController extends TileTimeFurnaceBase implements ITickable {
	private final ControllerInventory inventory = new ControllerInventory();

	private int burnTime = 0;
	private int maxBurnTime = 0;
	private long smeltStartTime = -1;
	private long smeltFinishTime = -1;
	private int[] powerGotten = null;
	private TimeFurnaceRecipe currentRecipe = null;

	private AxisAlignedBB powerRequestAABB = null;

	@Override
	public void update() {
		if (burnTime > 0) {
			burnTime--;
			markDirty();
		}

		if (!world.isRemote) {
			if (isSmelting()) {
				if (!canSmelt()) {
					stopSmelting();
				}
				//Check for completion
				else if (world.getTotalWorldTime() >= smeltFinishTime) {
					boolean powerComplete = true;

					for (int i = 0; i < powerGotten.length; i++) {
						if (powerGotten[i] < currentRecipe.powerRequired.getAmountsRequired()[i]) {
							powerComplete = false;
						}
					}

					//recipe complete - process output and reset state
					if (powerComplete) {
						smeltStartTime = world.getTotalWorldTime();
						smeltFinishTime = smeltStartTime + currentRecipe.smeltTime;

						for (int i = 0; i < powerGotten.length; i++) {
							powerGotten[i] = 0;
						}

						inventory.insertRecipeOutput(currentRecipe.output.copy(), false);
						inventory.extractItem(ControllerInventory.INPUT_SLOT, 1, false);

						if (!canSmelt()) {
							stopSmelting();
						}
					}
				}

				//Skip if just finished a recipe and there isn't anything else to smelt
				if (isSmelting()) {
					//If not actively smelting, don't use power
					boolean tryRequestPower = true;

					if (burnTime <= 0) {
						//uh oh... out of fuel! Quick, get more!
						tryRefuel();

						if (burnTime <= 0) {
							//Nooooooooo...
							smeltFinishTime += 2;

							if (world.getTotalWorldTime() - smeltStartTime > currentRecipe.smeltTime) {
								stopSmelting();
							}

							sendBlockUpdate();

							//Couldn't refuel, don't use power
							tryRequestPower = false;
						}
					}

					//Get power
					if (tryRequestPower) {
						boolean needsSync = false;

						for (int i = 0; i < powerGotten.length; i++) {
							PowerType powerType = currentRecipe.powerRequired.getTypesRequired()[i];

							int requestAmount = currentRecipe.powerRequired.getAmountForType(powerType) - powerGotten[i];

							if (requestAmount > 0) {
								//Only set the box if needed
								if (powerRequestAABB == null) {
									setRequestBox();
								}

								int prevPower = powerGotten[i];
								powerGotten[i] += PowerHandler.requestPower(world, powerRequestAABB, powerType, requestAmount);

								needsSync = needsSync || prevPower != powerGotten[i];
							}
						}

						if (needsSync) {
							sendBlockUpdate();
						}
					}
				}
			}
			else {
				if (canSmelt()) {
					tryRefuel();

					//If refuel successful
					if (burnTime > 0) {
						//Start smelting
						currentRecipe = TimeFurnaceRecipes.getRecipe(inventory.getStackInSlot(ControllerInventory.INPUT_SLOT));
						smeltStartTime = world.getTotalWorldTime();
						smeltFinishTime = smeltStartTime + currentRecipe.smeltTime;
						powerGotten = new int[currentRecipe.powerRequired.getTypesRequired().length];

						sendBlockUpdate();
					}
				}
			}
		}
	}

	private boolean canSmelt() {
		ItemStack input = inventory.getStackInSlot(ControllerInventory.INPUT_SLOT);
		TimeFurnaceRecipe nextRecipe = TimeFurnaceRecipes.getRecipe(input);

		if (nextRecipe != null && (currentRecipe == null || currentRecipe.equals(nextRecipe))) {
			return inventory.insertRecipeOutput(nextRecipe.output, true).isEmpty();
		}

		return false;
	}

	public boolean isSmelting() {
		return currentRecipe != null;
	}

	private void tryRefuel() {
		if (burnTime <= 0) {
			ItemStack fuelStack = inventory.getStackInSlot(ControllerInventory.FUEL_SLOT);

			if (!fuelStack.isEmpty() && TileEntityFurnace.getItemBurnTime(fuelStack) > 0) {
				inventory.extractItem(ControllerInventory.FUEL_SLOT, 1, false);

				burnTime = TileEntityFurnace.getItemBurnTime(fuelStack);
				maxBurnTime = burnTime;

				if (inventory.getStackInSlot(ControllerInventory.FUEL_SLOT).isEmpty() && fuelStack.getItem().hasContainerItem(fuelStack)) {
					inventory.setStackInSlot(ControllerInventory.FUEL_SLOT, fuelStack.getItem().getContainerItem(fuelStack));
				}
			}
		}
	}

	private void setRequestBox() {
		final int sideLength = 3; //Should match BlockTimeFurnace.getStructurePosList()
		BlockPos bottomCorner = getBottomCorner();

		if (bottomCorner != null) {
			powerRequestAABB = new AxisAlignedBB(bottomCorner.getX(), bottomCorner.getY(), bottomCorner.getZ(), bottomCorner.getX() + sideLength, bottomCorner.getY() + sideLength, bottomCorner.getZ() + sideLength);
		}
	}

	private void stopSmelting() {
		smeltStartTime = -1;
		smeltFinishTime = -1;
		currentRecipe = null;
		powerGotten = null;

		sendBlockUpdate();
	}

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

	public boolean isBurning() {
		return burnTime > 0;
	}

	public float getBurnPercent() {
		return (float) burnTime / maxBurnTime;
	}

	public double getSmeltPercent() {
		if (currentRecipe != null) {
			long ticksStalled =  smeltFinishTime - smeltStartTime - currentRecipe.smeltTime;
			double totalPercent = (double) (world.getTotalWorldTime() - smeltStartTime - ticksStalled) / (smeltFinishTime - smeltStartTime);
			totalPercent = MathHelper.clamp(totalPercent, 0.0, 1.0);

			int[] powerRequired = currentRecipe.powerRequired.getAmountsRequired();

			for (int i = 0; i < powerRequired.length; i++) {
				totalPercent += (double) powerGotten[i] / powerRequired[i];
			}

			return MathHelper.clamp(totalPercent / (powerRequired.length + 1), 0.0, 1.0);
		}
		else {
			return 0.0;
		}
	}

	private static final String INVENTORY_TAG = "inventory";
	private static final String BURN_TIME_TAG = "burnTime";
	private static final String MAX_BURN_TIME_TAG = "maxBurnTime";
	private static final String SMELT_START_TAG = "smeltStart";
	private static final String SMELT_FINISH_TAG = "smeltEnd";
	private static final String POWER_TAG = "powerGet";

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setTag(INVENTORY_TAG, inventory.serializeNBT());
		comp.setInteger(BURN_TIME_TAG, burnTime);
		comp.setInteger(MAX_BURN_TIME_TAG, maxBurnTime);

		if (isSmelting()) {
			comp.setLong(SMELT_START_TAG, smeltStartTime);
			comp.setLong(SMELT_FINISH_TAG, smeltFinishTime);
			comp.setIntArray(POWER_TAG, powerGotten);
		}

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		super.readFromNBT(comp);

		if (comp.hasKey(INVENTORY_TAG, Constants.NBT.TAG_COMPOUND)) {
			inventory.deserializeNBT(comp.getCompoundTag(INVENTORY_TAG));
		}

		if (comp.hasKey(BURN_TIME_TAG, Constants.NBT.TAG_INT) && comp.hasKey(MAX_BURN_TIME_TAG, Constants.NBT.TAG_INT)) {
			maxBurnTime = comp.getInteger(MAX_BURN_TIME_TAG);
			burnTime = comp.getInteger(BURN_TIME_TAG);
		}
		else {
			burnTime = 0;
			maxBurnTime = 0;
		}

		currentRecipe = null;

		if (canSmelt() && comp.hasKey(SMELT_START_TAG, Constants.NBT.TAG_LONG) && comp.hasKey(SMELT_FINISH_TAG, Constants.NBT.TAG_LONG) && comp.hasKey(POWER_TAG, Constants.NBT.TAG_INT_ARRAY)) {
			currentRecipe = TimeFurnaceRecipes.getRecipe(inventory.getStackInSlot(ControllerInventory.INPUT_SLOT));
			smeltStartTime = comp.getLong(SMELT_START_TAG);
			smeltFinishTime = comp.getLong(SMELT_FINISH_TAG);
			powerGotten = comp.getIntArray(POWER_TAG);
		}
		else {
			smeltStartTime = -1;
			smeltFinishTime = -1;
			powerGotten = null;
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

		public ItemStack insertRecipeOutput(ItemStack stack, boolean simulate) {
			return super.insertItem(OUTPUT_SLOT, stack, simulate);
		}

		@Override
		protected void onContentsChanged(int slot) {
			sendBlockUpdate();
		}
	}
}
