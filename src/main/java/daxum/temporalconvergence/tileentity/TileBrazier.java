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
import java.util.Random;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.block.BlockBrazier;
import daxum.temporalconvergence.block.ModBlocks;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.power.IPowerProvider;
import daxum.temporalconvergence.power.PowerHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileBrazier extends TileEntityBase implements ITickable, IPowerProvider {
	private static final Map<Item, BrazierFuel> FUEL_MAP = new HashMap<>();

	private final Random rand = new Random();
	private final ItemStackHandler inventory = new BrazierInventory();
	private Item currentFuel = null;
	private int burnTime = 0;
	private String powerType = null;
	private int powerPerTick = 0;
	private int powerThisTick = 0;
	private boolean burning = false;

	private float fillPercent = 0.0f;
	private float prevFillPercent = 0.0f;

	@Override
	public void update() {
		powerThisTick = 0;

		if (isBurning()) {
			burnTime--;
			markDirty();

			if (burnTime <= 0) {
				if (isFuel(inventory.getStackInSlot(0)) && inventory.getStackInSlot(0).getItem() == currentFuel) {
					inventory.getStackInSlot(0).shrink(1);
					burnTime = FUEL_MAP.get(currentFuel).burnTime;

					sendBlockUpdate();
				}
				else {
					stopBurning();
				}
			}

			if (rand.nextInt(720) == 0) {
				world.playSound(null, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0f + rand.nextFloat(), rand.nextFloat() * 0.7f + 0.3f);
			}
		}

		if (world.isRemote) {
			prevFillPercent = fillPercent;
			fillPercent = calculateFillPercent();
		}
	}

	@Override
	public int getPower(String type, int amount) {
		if (burning && powerType.equals(type) && powerThisTick < powerPerTick) {
			int provided = Math.min(powerPerTick - powerThisTick, amount);
			powerThisTick += provided;
			return provided;
		}

		return 0;
	}

	//Called when the brazier first starts burning - when it was empty, then filled with fuel, and then lit
	private void startBurning() {
		if (!burning && !isPaused()) {
			if (isFuel(inventory.getStackInSlot(0))) {
				currentFuel = inventory.getStackInSlot(0).getItem();
				inventory.getStackInSlot(0).shrink(1);

				BrazierFuel fuelData = FUEL_MAP.get(currentFuel);

				burnTime = fuelData.burnTime;
				powerType = fuelData.powerType;
				powerPerTick = fuelData.power;
				burning = true;


				PowerHandler.addProvider(world, pos, powerType, fuelData.powerRange.offset(pos), true);

				world.setBlockState(pos, ModBlocks.BRAZIER.getDefaultState().withProperty(BlockBrazier.BURNING, true));

				sendBlockUpdate();
			}
		}
	}

	//Called when the brazier runs out of fuel (burnTime <= 0 and inventory is empty)
	public void stopBurning() {
		PowerHandler.removeProvider(world, pos, powerType);
		world.setBlockState(pos, ModBlocks.BRAZIER.getDefaultState().withProperty(BlockBrazier.BURNING, false));

		currentFuel = null;
		burnTime = 0;
		powerType = null;
		powerPerTick = 0;
		burning = false;

		sendBlockUpdate();
	}

	//Called when a brazier is put out, but still has fuel left
	public void pauseBurning() {
		if (burning) {
			burning = false;
			PowerHandler.removeProvider(world, pos, powerType);
			world.setBlockState(pos, ModBlocks.BRAZIER.getDefaultState().withProperty(BlockBrazier.BURNING, false));

			sendBlockUpdate();
		}
	}

	//Called when a brazier is lit after being paused with pauseBurning()
	private void resumeBurning() {
		if (isPaused()) {
			burning = true;
			PowerHandler.addProvider(world, pos, powerType, FUEL_MAP.get(currentFuel).powerRange.offset(pos), true);
			world.setBlockState(pos, ModBlocks.BRAZIER.getDefaultState().withProperty(BlockBrazier.BURNING, true));

			sendBlockUpdate();
		}
	}

	public void StartOrResumeBurning() {
		if (isPaused()) {
			resumeBurning();
		}
		else {
			startBurning();
		}
	}

	public boolean isBurning() {
		return burning;
	}

	public boolean isPaused() {
		return !burning && currentFuel != null;
	}

	public boolean isFuel(ItemStack stack) {
		return !stack.isEmpty() && FUEL_MAP.get(stack.getItem()) != null;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	public ItemStackHandler getInventory() {
		return inventory;
	}

	public Item getItem() {
		return currentFuel == null ? inventory.getStackInSlot(0).getItem() : currentFuel;
	}

	public float getFillPercent(float partialTicks) {
		return (fillPercent - prevFillPercent) * partialTicks + prevFillPercent;
	}

	public boolean shouldRenderContents() {
		return !inventory.getStackInSlot(0).isEmpty() || burnTime > 0;
	}

	private float calculateFillPercent() {
		float fillPercent = inventory.getStackInSlot(0).getCount();

		if (currentFuel != null && burnTime > 0) {
			fillPercent += (float) burnTime / FUEL_MAP.get(currentFuel).burnTime;
		}

		return fillPercent / inventory.getSlotLimit(0);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("inventory", Constants.NBT.TAG_COMPOUND)) {
			inventory.deserializeNBT(comp.getCompoundTag("inventory"));
		}

		if (comp.hasKey("burning", Constants.NBT.TAG_BYTE)) {
			burning = comp.getBoolean("burning");
		}

		if (comp.hasKey("burnTime", Constants.NBT.TAG_INT)) {
			burnTime = comp.getInteger("burnTime");
		}

		if (comp.hasKey("fuel", Constants.NBT.TAG_STRING)) {
			final Item readItem = Item.getByNameOrId(comp.getString("fuel"));

			if (isFuel(new ItemStack(readItem))) {
				BrazierFuel fuelData = FUEL_MAP.get(readItem);

				currentFuel = readItem;
				powerType = fuelData.powerType;
				powerPerTick = fuelData.power;
			}
			else {
				TemporalConvergence.LOGGER.error("Recieved invalid item {} for brazier at {}", comp.getString("fuel"), pos);
				stopBurning();
			}
		}

		super.readFromNBT(comp);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setTag("inventory", inventory.serializeNBT());
		comp.setBoolean("burning", burning);
		comp.setInteger("burnTime", burnTime);

		if (currentFuel != null) {
			comp.setString("fuel", currentFuel.getRegistryName().toString());
		}

		return super.writeToNBT(comp);
	}

	private static class BrazierFuel {
		public final int burnTime;
		public final int power;
		public final String powerType;
		public final AxisAlignedBB powerRange;

		public BrazierFuel(int time, int pow, String type, AxisAlignedBB range) {
			burnTime = time;
			power = pow;
			powerType = type;
			powerRange = range;
		}
	}

	private class BrazierInventory extends ItemStackHandler {
		public BrazierInventory() {
			super(1);
		}

		@Override
		public int getSlotLimit(int slot) {
			return 4;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (isFuel(stack)) {
				if (isBurning() || isPaused()) {
					if (getStackInSlot(slot).getCount() < getSlotLimit(slot) - 1 && stack.getItem() == currentFuel) {
						return super.insertItem(slot, stack, simulate);
					}
				}
				else {
					return super.insertItem(slot, stack, simulate);
				}
			}

			return stack;
		}

		@Override
		protected void onContentsChanged(int slot) {
			sendBlockUpdate();
		}
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
	public boolean hasFastRenderer() {
		return true;
	}

	public static void addFuel(Item fuel, int burnTime, String powerType, int powerProduced, double range) {
		if (fuel != null && burnTime > 0 && powerProduced >= 0) {
			if (!FUEL_MAP.containsKey(fuel)) {
				FUEL_MAP.put(fuel, new BrazierFuel(burnTime, powerProduced, powerType, new AxisAlignedBB(-range, -range, -range, range, range, range)));
			}
			else {
				TemporalConvergence.LOGGER.warn("Attempted to register duplicate brazier fuel for item {}", fuel.getRegistryName());
			}
		}
	}

	static {
		//Initialize fuels
		addFuel(ModItems.TIME_DUST, 2400, "time", 10, 5);
	}
}
