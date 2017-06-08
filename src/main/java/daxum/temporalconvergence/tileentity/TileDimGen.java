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

import java.util.ArrayList;
import java.util.List;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.block.BlockDimGen;
import daxum.temporalconvergence.block.ModBlocks;
import daxum.temporalconvergence.recipes.DimGenRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileDimGen extends TileEntity implements ITickable {
	public static final int PEDESTAL_COUNT = 12;
	public static final int WARMUP_TIME = 75; //The time in ticks for the clock to reach full size when crafting (craftingState == WARMUP)
	public static final int CRAFT_INIT_STEP = 8; //The rate at which pedestals will be checked and items consumed when crafting starts, in ticks. Cannot be 0
	public static final int NUM_ROTATIONS_CRAFTING = 14; //The number of times the minute hand does a full rotation in the CraftingStates.CRAFTING state
	public static final int CRAFTING_TIME = PEDESTAL_COUNT * CRAFT_INIT_STEP * NUM_ROTATIONS_CRAFTING; //The total number of ticks spent in the CRAFTING state
	public static final int END_TIME = 75; //The amount of time in ticks the END phase takes
	public static final int POST_SUCCESS_TIME = 15; //Ticks to spend in the END_POST_SUCCESS state

	private ItemStackHandler inventory = new DimGenInventory(this);
	private BlockPos[] pedLocs = new BlockPos[PEDESTAL_COUNT]; //Cache of pedestal locations, starting at north (12 o' clock) and going clockwise
	private BlockPos prevPos = BlockPos.ORIGIN;
	private AxisAlignedBB fullClock; //Rendering bounding box for when it's crafting
	private AxisAlignedBB smallClock; //Rendering bounding box for when it's not crafting
	private CraftingStates craftingState = CraftingStates.NOT_CRAFTING;
	private int ticksInState = 0; //Number of ticks spent in the current crafting state. Not set if not crafting.
	private List<ItemStack> currentRecipe = new ArrayList<>(); //The item inputs to the currently active recipe. Does not contain center input. Values are removed as they're consumed
	private ItemStack recipeOutput = ItemStack.EMPTY; //The cached value of the output of the current recipe
	private boolean[] activePedestals = new boolean[PEDESTAL_COUNT]; //The pedestals being used to craft the current recipe

	//The below are used for rendering only
	public float scale = 1.0f;
	public float prevScale = 1.0f;
	public float[] rotations = {0.0f, 0.0f, 0.0f, 0.0f}; //Face, hour, minute, second
	public float[] prevRotations = {0.0f, 0.0f, 0.0f, 0.0f};

	@Override
	public void update() {
		//Reset caches if position somehow changes
		if (!prevPos.equals(pos)) {
			onLoad();
		}

		//Set previous variables for rendering
		prevScale = scale;
		for (int i = 0; i < rotations.length; i++) {
			prevRotations[i] = rotations[i];
		}

		if (craftingState.isCrafting()) {
			markDirty();

			if (craftingState == CraftingStates.WARMUP) {
				if (ticksInState >= WARMUP_TIME) {
					transitionToState(CraftingStates.CRAFTING);
				}
				else {
					ticksInState++;
				}
			}
			else if (craftingState == CraftingStates.CRAFTING) {
				if (ticksInState >= CRAFTING_TIME) {
					transitionToState(CraftingStates.END_SUCCESS);
					inventory.setStackInSlot(0, recipeOutput);
				}
				else if (ticksInState <= CRAFT_INIT_STEP * PEDESTAL_COUNT && ticksInState % CRAFT_INIT_STEP == 0) {
					int pedestalNumber = ticksInState / CRAFT_INIT_STEP;

					if (isPedestalActive(pedestalNumber) && !tryConsumeItem(pedestalNumber)) {
						craftingState = CraftingStates.STALLED;
						sendBlockUpdate();
					}
					else {
						//Item was successfully consumed and crafting can continue, or the pedestal wasn't required to have an item to consume
						ticksInState++;
					}
				}
				else {
					//All items have already been consumed
					ticksInState++;
				}
			}
			else if (craftingState == CraftingStates.STALLED) {
				int pedestalNumber = ticksInState / CRAFT_INIT_STEP;

				if (tryConsumeItem(pedestalNumber)) {
					transitionToState(CraftingStates.CRAFTING);
				}
			}
			else if (craftingState == CraftingStates.END_SUCCESS || craftingState == CraftingStates.END_FAIL) {
				if (ticksInState >= END_TIME) {
					if (craftingState == CraftingStates.END_SUCCESS) {
						transitionToState(CraftingStates.END_POST_SUCCESS);
					}
					else {
						resetCraftingState();
					}
				}
				else {
					ticksInState++;
				}
			}
			else if (craftingState == CraftingStates.END_POST_SUCCESS) {
				if (ticksInState >= POST_SUCCESS_TIME) {
					resetCraftingState();
				}
				else {
					ticksInState++;
				}
			}
		}
	}

	private void transitionToState(CraftingStates state) {
		craftingState = state;
		ticksInState = 0;
		sendBlockUpdate();
	}

	protected void setTime(int state) {
		if (world.isRemote) {
			if (state == 0) {
				rotations[0] = 0.0f; //Not sure why this sometimes isn't set right
				rotations[1] = (world.getWorldTime() + 6000) % 12000 / 12000.0f * 360.0f;
				rotations[2] = rotations[1] % 30.0f / 30.0f * 360.0f;
				rotations[3] = rotations[2] % 30.0f / 30.0f * 360.0f;

				for (int i = 1; i < rotations.length; i++) {
					if (rotations[i] < prevRotations[i])
						prevRotations[i] -= 360.0;
				}
			}
			else if (state == 1) {
				rotations[0] = 720.0f * ((scale - 1) / 14.0f);

				if (rotations[0] >= 720.0f)
					rotations[0] = 720.0f;

				for (int i = 1; i < rotations.length; i++)
					rotations[i] = 360.0f * (i + 3) * (rotations[0] / 720.0f);
			}
			else if (state == 2) {
				rotations[0] = 720.0f;
				rotations[1] = (400.0f - craftTicks) / 400.0f * 360.0f + 1440;
				rotations[2] = 1800.0f + (400.0f - craftTicks) / 400.0f * 5040.0f;
				rotations[3] = 7200.0f - (400.0f - craftTicks) / 400.0f * 5040.0f;

				if (rotations[3] == 7200.0f)
					prevRotations[3] += 5040.0f;

				if (rotations[1] >= 1800.0f) {
					rotations[1] = 1440.0f;
					rotations[2] = 1800.0f;
					rotations[3] = 2160.0f;
					prevRotations[1] -= 360.0f;
					prevRotations[3] -= 5040.0f;
				}
			}
			else if (state == 3) {
				rotations[0] = 720.0f * ((scale - 1) / 14.0f);

				if (rotations[0] <= 0.0f)
					rotations[0] = 0.0f;

				for (int i = 1; i < rotations.length; i++)
					rotations[i] = 360.0f * (i + 3) * (rotations[0] / 720.0f);
			}
		}
	}

	private void spawnParticles() {
		if (world.isRemote) {
			for (int i = 0; i < PEDESTAL_COUNT; i++) {
				if (activePedestals[i]) {
					spawnParticlesAt(pedLocs[i]);
				}
			}
		}
	}

	private void spawnParticlesAt(BlockPos toPos) {
		int number = (int) (10 * MathHelper.sin(180.0f * (craftTicks / 400.0f) * (float)Math.PI / 180.0f));

		for (int i = 0; i < number; i++) {
			double offX = Math.random() * 0.5 + 0.25;
			double offY = Math.random() * 0.25 + 1.2;
			double offZ = Math.random() * 0.5 + 0.25;
			TemporalConvergence.proxy.spawnDimGenParticle(world, toPos.getX() + offX, toPos.getY() + offY, toPos.getZ() + offZ, pos.getX() + 0.5, pos.getY() + 1.325, pos.getZ() + 0.5);
		}
	}

	public void tryStartCrafting() {
		if (!world.isRemote && !craftingState.isCrafting()) {
			List<ItemStack> pedestalInputs = getItemsFromPedestals();

			if (canCraftUsing(pedestalInputs)) {
				craftingState = CraftingStates.WARMUP;
				ticksInState = 0;
				currentRecipe = pedestalInputs;
				recipeOutput = DimGenRecipes.getOutput(inventory.getStackInSlot(0), pedestalInputs);
				setActivePedestals();

				sendBlockUpdate();
			}
		}
	}

	private void resetCraftingState() {
		craftingState = CraftingStates.NOT_CRAFTING;
		ticksInState = 0;
		currentRecipe.clear();
		recipeOutput = ItemStack.EMPTY;
		resetActivePedestals();

		sendBlockUpdate();
	}

	private boolean canCraftUsing(List<ItemStack> stacks) {
		return DimGenRecipes.isValidRecipe(inventory.getStackInSlot(0), stacks);
	}

	private List<ItemStack> getItemsFromPedestals() {
		List<ItemStack> inputs = new ArrayList<>();

		for (int i = 0; i < pedLocs.length; i++) {
			ItemStack stack = getItemFromPedestalAt(pedLocs[i]);

			if (!stack.isEmpty())
				inputs.add(stack);
		}

		return inputs;
	}

	private void setActivePedestals() {
		for (int i = 0; i < PEDESTAL_COUNT; i++) {
			if (world.getTileEntity(pedLocs[i]) instanceof TilePedestal) {
				activePedestals[i] = !((TilePedestal)world.getTileEntity(pedLocs[i])).getInventory().getStackInSlot(0).isEmpty();
			}
		}
	}

	private void resetActivePedestals() {
		for (int i = 0; i < activePedestals.length; i++) {
			activePedestals[i] = false;
		}
	}

	public boolean isPedestalActive(int i) {
		if (i >= 0 && i < PEDESTAL_COUNT) {
			return activePedestals[i];
		}
		else {
			TemporalConvergence.LOGGER.warn("isPedestalActive() called with invalid index " + i + ". Valid range is [0, " + PEDESTAL_COUNT + ")");
		}

		return false;
	}

	private ItemStack getItemFromPedestalAt(BlockPos position) {
		if (world.getTileEntity(position) instanceof TilePedestal) {
			return ((TilePedestal)world.getTileEntity(position)).getInventory().getStackInSlot(0).copy();
		}

		return ItemStack.EMPTY;
	}

	private boolean tryConsumeItem(int pedestalNumber) {
		if (ItemStack.areItemStacksEqual(getItemFromPedestalAt(pedLocs[pedestalNumber]), currentRecipe.get(0))) {
			clearPedestal(pedLocs[pedestalNumber]);
			currentRecipe.remove(0);
			return true;
		}

		return false;
	}

	private void clearPedestal(BlockPos position) {
		if (world.getTileEntity(position) instanceof TilePedestal) {
			((TilePedestal)world.getTileEntity(position)).getInventory().setStackInSlot(0, ItemStack.EMPTY);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setTag("inv", inventory.serializeNBT());
		comp.setBoolean("craft", crafting);
		comp.setFloat("scale", scale);
		comp.setInteger("ctick", craftTicks);

		if (crafting)
			for (int i = 0; i < currentRecipe.size(); i++)
				comp.setTag("cur" + i, currentRecipe.get(i).serializeNBT());

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("inv"))
			inventory.deserializeNBT(comp.getCompoundTag("inv"));
		if (comp.hasKey("craft"))
			crafting = comp.getBoolean("craft");
		if (comp.hasKey("scale"))
			scale = comp.getFloat("scale");
		if (comp.hasKey("ctick"))
			craftTicks = comp.getInteger("ctick");

		currentRecipe.clear();
		for (int i = 0; i < 13; i++) {
			if (!comp.hasKey("cur" + i))
				break;
			currentRecipe.add(new ItemStack((NBTTagCompound) comp.getTag("cur" + i)));
		}

		super.readFromNBT(comp);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound comp = new NBTTagCompound();
		comp = writeToNBT(comp);

		return new SPacketUpdateTileEntity(pos, -42, comp);
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

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (craftingState.isCrafting())
			return fullClock;
		return smallClock;
	}

	public int getTicksInState() {
		return ticksInState;
	}

	public CraftingStates getCraftingState() {
		return craftingState;
	}

	public boolean canRemoveItem() {
		return craftingState != CraftingStates.END_SUCCESS;
	}

	public float getRotationDegrees() {
		if (world.getBlockState(pos).getBlock() == ModBlocks.DIM_GEN) {
			switch (world.getBlockState(pos).getValue(BlockDimGen.FACING)) {
			default:
			case NORTH: return 0;
			case EAST: return 270;
			case SOUTH: return 180;
			case WEST: return 90;
			}
		}

		return 0;
	}

	@Override
	public void onLoad() {
		//Still a bit buggy due to chunk culling
		fullClock = new AxisAlignedBB(pos.add(-6, 0, -6), pos.add(6, 1, 6));
		smallClock = new AxisAlignedBB(pos, pos.add(1, 1, 1));

		//Cache pedestal locations
		pedLocs[0] = pos.north(6);
		pedLocs[1] = pos.north(5).east(3);
		pedLocs[2] = pos.north(3).east(5);
		pedLocs[3] = pos.east(6);
		pedLocs[4] = pos.south(3).east(5);
		pedLocs[5] = pos.south(5).east(3);
		pedLocs[6] = pos.south(6);
		pedLocs[7] = pos.south(5).west(3);
		pedLocs[8] = pos.south(3).west(5);
		pedLocs[9] = pos.west(6);
		pedLocs[10] = pos.north(3).west(5);
		pedLocs[11] = pos.north(5).west(3);

		prevPos = pos;
	}

	public void sendBlockUpdate() {
		if (!world.isRemote) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 8);
			markDirty();
		}
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1;
	}

	public static class DimGenInventory extends ItemStackHandler {
		private final TileDimGen parent;

		public DimGenInventory(TileDimGen tp) {
			super(1);
			parent = tp;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (parent.canRemoveItem()) {
				return super.extractItem(slot, amount, simulate);
			}

			return ItemStack.EMPTY;
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

	public static enum CraftingStates {
		NOT_CRAFTING,
		WARMUP,
		CRAFTING,
		STALLED,
		END_SUCCESS,
		END_FAIL,
		END_POST_SUCCESS;

		public boolean isCrafting() {
			return this != NOT_CRAFTING;
		}

		public boolean hasSucceeded() {
			return this == END_SUCCESS || this == END_POST_SUCCESS;
		}
	}
}
