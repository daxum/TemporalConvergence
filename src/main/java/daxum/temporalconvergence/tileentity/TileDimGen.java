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
import daxum.temporalconvergence.recipes.DimGenRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileDimGen extends TileEntity implements ITickable {
	public static final int PEDESTAL_COUNT = 12;
	private static final int START_AND_END_TIME = 75; //The time in ticks for the clock to reach full size when crafting (craftingState == STARTUP) and shrink back when done
	private static final int TICKS_BETWEEN_PEDESTALS = 8; //The rate at which pedestals will be checked and items consumed when crafting starts, in ticks. Cannot be 0
	private static final int NUM_ROTATIONS_CRAFTING = 4; //The number of times the minute hand does a full rotation in the CraftingStates.CRAFTING state
	private static final int CRAFTING_TIME = PEDESTAL_COUNT * TICKS_BETWEEN_PEDESTALS * NUM_ROTATIONS_CRAFTING; //The total number of ticks spent in the CRAFTING state
	private static final int POST_SUCCESS_TIME = 15; //Ticks to spend in the END_POST_SUCCESS state

	//All of these values need to be written to / read from nbt
	private ItemStackHandler inventory = new DimGenInventory(this);
	private CraftingStates craftingState = CraftingStates.NOT_CRAFTING;
	private int ticksInState = 0; //Number of ticks spent in the current crafting state. Not set if not crafting.
	private List<ItemStack> currentRecipe = new ArrayList<>(); //The item inputs to the currently active recipe. Does not contain center input. Values are removed as they're consumed
	private ItemStack recipeOutput = ItemStack.EMPTY; //The cached value of the output of the current recipe
	private boolean[] activePedestals = new boolean[PEDESTAL_COUNT]; //The pedestals being used to craft the current recipe

	//These are caches that don't need to be saved
	private BlockPos[] pedLocs = new BlockPos[PEDESTAL_COUNT]; //Cache of pedestal locations, starting at north (12 o' clock) and going clockwise
	private BlockPos prevPos = BlockPos.ORIGIN;

	@Override
	public void update() {
		//Reset caches if position somehow changes
		if (!prevPos.equals(pos)) {
			onLoad();
		}

		doClientUpdate();

		if (craftingState.isCrafting()) {
			markDirty();

			if (craftingState == CraftingStates.STARTUP) {
				if (ticksInState >= START_AND_END_TIME) {
					transitionToState(CraftingStates.CRAFTING);
				}
				else {
					ticksInState++;
				}
			}
			else if (craftingState == CraftingStates.CRAFTING) {
				if (ticksInState >= CRAFTING_TIME) {
					inventory.setStackInSlot(0, recipeOutput);
					recipeOutput = ItemStack.EMPTY;
					transitionToState(CraftingStates.END_SUCCESS);
				}
				else if (!world.isRemote && ticksInState < TICKS_BETWEEN_PEDESTALS * PEDESTAL_COUNT && ticksInState % TICKS_BETWEEN_PEDESTALS == 0) { //If the minute hand is pointing to a pedestal
					int pedestalNumber = ticksInState / TICKS_BETWEEN_PEDESTALS;

					if (isPedestalActive(pedestalNumber) && !tryConsumeItem(pedestalNumber)) {
						craftingState = CraftingStates.STALLED; //Do not use transitionToState() because it resets ticksInState
						sendBlockUpdate();
					}
					else {
						//Item was successfully consumed and crafting can continue, or the pedestal wasn't required to have an item to consume
						ticksInState++;
					}
				}
				else {
					//All items have already been consumed, or this is a client world
					ticksInState++;
				}
			}
			else if (craftingState == CraftingStates.STALLED) {
				int pedestalNumber = ticksInState / TICKS_BETWEEN_PEDESTALS;

				if (tryConsumeItem(pedestalNumber)) {
					craftingState = CraftingStates.CRAFTING; //Do not use transitionToState() because it resets ticksInState
					ticksInState++;
					sendBlockUpdate();
				}
			}
			else if (craftingState.isEnd()) {
				if (ticksInState >= START_AND_END_TIME) {
					if (craftingState.hasSucceeded()) {
						transitionToState(CraftingStates.POST_SUCCESS);
					}
					else {
						resetCraftingState();
					}
				}
				else {
					ticksInState++;
				}
			}
			else if (craftingState == CraftingStates.POST_SUCCESS) {
				if (ticksInState >= POST_SUCCESS_TIME) {
					resetCraftingState();
				}
				else {
					ticksInState++;
				}
			}
		}
		else {
			ticksInState = 0; //Fix rare desync
		}
	}

	public void tryStartCrafting() {
		if (!world.isRemote && !craftingState.isCrafting()) {
			List<ItemStack> pedestalInputs = getItemsFromPedestals();

			if (canCraftUsing(pedestalInputs)) {
				currentRecipe = pedestalInputs;
				recipeOutput = DimGenRecipes.getOutput(inventory.getStackInSlot(0), pedestalInputs);
				setActivePedestals();
				transitionToState(CraftingStates.STARTUP);
			}
		}
	}

	public boolean isPedestalActive(int i) {
		if (i >= 0 && i < PEDESTAL_COUNT) {
			return activePedestals[i];
		}
		else {
			TemporalConvergence.LOGGER.warn("isPedestalActive() called with invalid index {}. Valid range is [0, {})", i, PEDESTAL_COUNT);
		}

		return false;
	}

	public ItemStackHandler getInventory() {
		return inventory;
	}

	public CraftingStates getCraftingState() {
		return craftingState;
	}

	public boolean canRemoveItem() {
		return craftingState != CraftingStates.END_SUCCESS;
	}

	private void stopIfCrafting() {
		if (craftingState.isCrafting() && !craftingState.isEnd()) {
			if (craftingState.isInCraftingStep()) {
				transitionToState(CraftingStates.END_FAIL_CRAFT);
			}
			else {
				craftingState = CraftingStates.END_FAIL; //Don't use transitionToState() because it would mess up the clock scale
				ticksInState = START_AND_END_TIME - ticksInState;
				sendBlockUpdate();
			}
		}
	}

	private void transitionToState(CraftingStates state) {
		craftingState = state;
		ticksInState = 0;
		sendBlockUpdate();
	}

	private void resetCraftingState() {
		currentRecipe.clear();
		recipeOutput = ItemStack.EMPTY;
		resetActivePedestals();
		transitionToState(CraftingStates.NOT_CRAFTING);
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

	//-----------------------------------------------Data Structures-----------------------------------------------

	//The order these values are declared should not be changed, because it will break saving and loading
	public static enum CraftingStates {
		NOT_CRAFTING,
		STARTUP,
		CRAFTING,
		STALLED,
		END_SUCCESS,
		END_FAIL, //If the crafting failed during the startup step
		END_FAIL_CRAFT, //If the crafting failed during the crafting step
		POST_SUCCESS;

		public boolean isCrafting() {
			return this != NOT_CRAFTING;
		}

		public boolean hasSucceeded() {
			return this == END_SUCCESS || this == POST_SUCCESS;
		}

		public boolean isEnd() {
			return this == END_SUCCESS || this == END_FAIL || this == END_FAIL_CRAFT;
		}

		public boolean isInCraftingStep() {
			return this == CRAFTING || this == STALLED;
		}
	}

	private static class DimGenInventory extends ItemStackHandler {
		private final TileDimGen parent;

		public DimGenInventory(TileDimGen tp) {
			super(1);
			parent = tp;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (parent.canRemoveItem()) {
				parent.stopIfCrafting();
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

	//----------------------------------------------Client-only stuff----------------------------------------------

	private static final float MAX_SCALE = 15.0f;
	private static final float MIN_SCALE = 1.0f;
	private static final float CLOCK_FACE_ROTATIONS = 2; //Number of full rotations the clock face makes during the startup and end states

	private float scale = 1.0f;
	private float prevScale = 1.0f;
	private Angle[] rotations = {new Angle(), new Angle(), new Angle(), new Angle()}; //Face, hour, minute, second
	private AxisAlignedBB fullClockBB; //Rendering bounding box for when it's crafting
	private AxisAlignedBB smallClockBB; //Rendering bounding box for when it's not crafting

	@SideOnly(Side.CLIENT)
	public float getRotationForRender(ClockPart part, float partialTicks) {
		return rotations[part.ordinal()].getRotation(partialTicks);
	}

	@SideOnly(Side.CLIENT)
	public float getScaleForRender(float partialTicks) {
		return (scale - prevScale) * partialTicks + prevScale;
	}

	@SideOnly(Side.CLIENT)
	public float getScaleForPedestal(int pedestalNum, float partialTicks) {
		if (craftingState.isInCraftingStep()) {
			float ticksRelativeToPedestal = ticksInState - pedestalNum * TICKS_BETWEEN_PEDESTALS + partialTicks;
			float pedestalScale = 0.25f * ticksRelativeToPedestal / (TICKS_BETWEEN_PEDESTALS * 2);

			return MathHelper.clamp(pedestalScale, 0.0f, 0.25f);
		}
		else {
			TemporalConvergence.LOGGER.warn("getScaleForPedestal() called outside of crafting step");
			return 0.0f;
		}
	}

	@SideOnly(Side.CLIENT)
	public float getPedestalTransparency(float partialTicks) {
		if (craftingState.isInCraftingStep()) {
			float firstRotationTime = TICKS_BETWEEN_PEDESTALS * PEDESTAL_COUNT;
			float transparency = 1.0f - (ticksInState + partialTicks - firstRotationTime) / (CRAFTING_TIME - firstRotationTime);

			return MathHelper.clamp(transparency, 0.0f, 1.0f);
		}
		else {
			TemporalConvergence.LOGGER.warn("getPedestalTransparency() called outside of crafting step");
			return 0.0f;
		}
	}

	@SideOnly(Side.CLIENT)
	public BlockPos getPedestalLoc(int pedestalNum) {
		if (pedestalNum >= 0 && pedestalNum < pedLocs.length) {
			return pedLocs[pedestalNum];
		}

		TemporalConvergence.LOGGER.error("getPedestalLoc() called with invalid index {}. Valid range is [0, {})", pedestalNum, pedLocs.length);
		return BlockPos.ORIGIN;
	}

	@SideOnly(Side.CLIENT)
	public float getCenterScale(float partialTicks) {
		if (craftingState.isInCraftingStep() || craftingState == CraftingStates.END_SUCCESS) {
			return 0.25f;
		}
		else if (craftingState == CraftingStates.POST_SUCCESS) {
			return (ticksInState + partialTicks) / POST_SUCCESS_TIME * 1.75f + 0.25f;
		}
		else {
			TemporalConvergence.LOGGER.warn("Invalid call to getCenterScale()");
			return 0.0f;
		}
	}

	@SideOnly(Side.CLIENT)
	public float getCenterTransparency(float partialTicks) {
		if (craftingState.isInCraftingStep()) {
			float firstRotationTime = TICKS_BETWEEN_PEDESTALS * PEDESTAL_COUNT;
			return MathHelper.clamp((ticksInState + partialTicks - firstRotationTime) / (CRAFTING_TIME - firstRotationTime), 0.0f, 1.0f);
		}
		else if (craftingState == CraftingStates.END_SUCCESS) {
			return 1.0f;
		}
		else if (craftingState == CraftingStates.POST_SUCCESS) {
			return MathHelper.clamp(1.0f - (ticksInState + partialTicks) / POST_SUCCESS_TIME, 0.0f, 1.0f);
		}
		else {
			TemporalConvergence.LOGGER.warn("Invalid call to getCenterTransparency()");
			return 0.0f;
		}
	}

	private void doClientUpdate() {
		if (world.isRemote) {
			//Set previous scale
			prevScale = scale;

			//Update clock rotations/scale
			setTimeForState();

			spawnParticles();
		}
	}

	//CraftingStates.STALLED purposely omitted
	private void setTimeForState() {
		if (craftingState == CraftingStates.NOT_CRAFTING || craftingState == CraftingStates.POST_SUCCESS) {
			scale = MIN_SCALE;
			rotations[0].setAngle(0.0f, true); //Not sure why this sometimes isn't set right
			rotations[1].setAngle((world.getWorldTime() + 6000) % 12000 / 12000.0f * 360.0f, true);
			rotations[2].setAngle(rotations[1].getAngle() % 30.0f / 30.0f * 360.0f, true);
			rotations[3].setAngle(rotations[2].getAngle() % 30.0f / 30.0f * 360.0f, true);
		}
		else if (craftingState == CraftingStates.STARTUP) {
			float percentComplete = (float)ticksInState / START_AND_END_TIME;

			scale = (MAX_SCALE - MIN_SCALE) * percentComplete + MIN_SCALE;
			rotations[0].setAngle(360.0f * CLOCK_FACE_ROTATIONS * percentComplete, true);

			for (int i = 1; i < rotations.length; i++)
				rotations[i].setAngle(360.0f * (i + 3) * percentComplete, true);
		}
		else if (craftingState == CraftingStates.CRAFTING) {
			float percentComplete = (float)ticksInState / CRAFTING_TIME;

			scale = MAX_SCALE;

			rotations[0].setAngle(360.0f * CLOCK_FACE_ROTATIONS, true);
			rotations[1].setAngle(360.0f * percentComplete, true);
			rotations[2].setAngle(360.0f * NUM_ROTATIONS_CRAFTING * percentComplete, true);
			rotations[3].setAngle(rotations[1].getAngle(), true);
		}
		else if (craftingState.isEnd()) {
			float inversePercentComplete = 1.0f - (float)ticksInState / START_AND_END_TIME;
			scale = (MAX_SCALE - MIN_SCALE) * inversePercentComplete + MIN_SCALE;
			rotations[0].setAngle(360.0f * CLOCK_FACE_ROTATIONS * inversePercentComplete, false);

			for (int i = 1; i < rotations.length; i++)
				rotations[i].setAngle(360.0f * (i + 3) * inversePercentComplete, false);
		}
	}

	private void spawnParticles() {
		if (craftingState == CraftingStates.CRAFTING) {
			for (int i = 0; i < PEDESTAL_COUNT; i++) {
				if (activePedestals[i]) {
					spawnParticlesAt(pedLocs[i]);
				}
			}
		}
	}

	private void spawnParticlesAt(BlockPos toPos) {
		int number = (int) (10 * MathHelper.sin(180.0f * ((float)ticksInState / CRAFTING_TIME) * (float)Math.PI / 180.0f));

		if (ticksInState <= TICKS_BETWEEN_PEDESTALS * PEDESTAL_COUNT) {
			number = Math.random() >= 0.7 ? 1 : 0;
		}

		for (int i = 0; i < number; i++) {
			double offX = Math.random() * 0.5 + 0.25;
			double offY = Math.random() * 0.25 + 1.2;
			double offZ = Math.random() * 0.5 + 0.25;
			TemporalConvergence.proxy.spawnDimGenParticle(world, toPos.getX() + offX, toPos.getY() + offY, toPos.getZ() + offZ, pos.getX() + 0.5, pos.getY() + 1.325, pos.getZ() + 0.5);
		}
	}

	public enum ClockPart {
		FACE,
		HOUR_HAND,
		MINUTE_HAND,
		SECOND_HAND;
	}

	private class Angle {
		private float prevAngle = 0;
		private float angle = 0;

		public float getRotation(float partialTicks) {
			return (angle - prevAngle) * partialTicks + prevAngle;
		}

		public float getAngle() {
			return angle;
		}

		public void setAngle(float newAngle, boolean clockwise) {
			while (newAngle > 360.0f) {
				newAngle -= 360.0f;
			}

			while (newAngle < 0.0f) {
				newAngle += 360.0f;
			}

			prevAngle = angle;
			angle = newAngle;

			if (clockwise) {
				if (angle < prevAngle) {
					prevAngle -= 360.0f;
				}
			}
			else {
				if (angle > prevAngle) {
					prevAngle += 360.0f;
				}
			}
		}
	}

	//-----------------------------------------------NBT stuff below-----------------------------------------------

	private static final String INVENTORY_KEY = "inventory";
	private static final String CRAFTING_STATE_KEY = "craftingState";
	private static final String CURRENT_RECIPE_KEY = "currentRecipe";
	private static final String RECIPE_OUTPUT_KEY = "output";
	private static final String STATE_TICKS_KEY = "stateTicks";
	private static final String ACTIVE_PEDESTALS_KEY = "activePedestals";

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setTag(INVENTORY_KEY, inventory.serializeNBT());
		comp.setInteger(CRAFTING_STATE_KEY, craftingState.ordinal());

		if (craftingState.isCrafting()) {
			if (currentRecipe.size() > 0) {
				comp.setTag(CURRENT_RECIPE_KEY, serializeCurrentRecipe());
			}

			if (!recipeOutput.isEmpty()) {
				comp.setTag(RECIPE_OUTPUT_KEY, recipeOutput.serializeNBT());
			}

			comp.setInteger(STATE_TICKS_KEY, ticksInState);
			comp = serializeActivePedestals(comp);
		}

		return super.writeToNBT(comp);
	}

	private NBTTagList serializeCurrentRecipe() {
		NBTTagList recipeList = new NBTTagList();

		for (int i = 0; i < currentRecipe.size(); i++) {
			recipeList.appendTag(currentRecipe.get(i).serializeNBT());
		}

		return recipeList;
	}

	private NBTTagCompound serializeActivePedestals(NBTTagCompound comp) {
		int pedestalValues = 0;

		//Pack the boolean array into an integer. Much easier than saving the values separately
		for (int i = 0; i < activePedestals.length; i++) {
			pedestalValues = pedestalValues | (activePedestals[i] ? 1 << i : 0);
		}

		comp.setInteger(ACTIVE_PEDESTALS_KEY, pedestalValues);
		return comp;
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		super.readFromNBT(comp); //Needs to be at the top because position is used below

		if (comp.hasKey(INVENTORY_KEY, Constants.NBT.TAG_COMPOUND)) {
			inventory.deserializeNBT(comp.getCompoundTag(INVENTORY_KEY));
		}

		if (comp.hasKey(CRAFTING_STATE_KEY, Constants.NBT.TAG_INT)) {
			int craftOrdinal = comp.getInteger(CRAFTING_STATE_KEY);
			CraftingStates[] craftingStateList = CraftingStates.values();

			if (craftOrdinal >= 0 && craftOrdinal < craftingStateList.length) {
				craftingState = craftingStateList[craftOrdinal];
			}
		}
		else {
			TemporalConvergence.LOGGER.error("Failed to load crafting state for dimGen at {}", pos);
			craftingState = CraftingStates.NOT_CRAFTING;
		}

		if (craftingState.isCrafting()) {
			if (comp.hasKey(STATE_TICKS_KEY, Constants.NBT.TAG_INT)) {
				ticksInState = comp.getInteger(STATE_TICKS_KEY);
			}

			if (comp.hasKey(RECIPE_OUTPUT_KEY, Constants.NBT.TAG_COMPOUND)) {
				recipeOutput = new ItemStack(comp.getCompoundTag(RECIPE_OUTPUT_KEY));
			}

			if (comp.hasKey(CURRENT_RECIPE_KEY, Constants.NBT.TAG_LIST)) {
				deserializeCurrentRecipe(comp.getTagList(CURRENT_RECIPE_KEY, Constants.NBT.TAG_COMPOUND));
			}

			deserializeActivePedestals(comp);

			//TODO: Validate crafting state
		}
	}

	private void deserializeCurrentRecipe(NBTTagList recipeList) {
		currentRecipe.clear();
		for (int i = 0; i < recipeList.tagCount() && i < PEDESTAL_COUNT; i++) {
			currentRecipe.add(new ItemStack(recipeList.getCompoundTagAt(i)));
		}

		if (recipeList.tagCount() > PEDESTAL_COUNT) {
			TemporalConvergence.LOGGER.error("Encountered a list of unexpected size {} while reading nbt for dimGen at {}. Maximum size is {}.", recipeList.tagCount(), pos, PEDESTAL_COUNT);
		}
	}

	private void deserializeActivePedestals(NBTTagCompound comp) {
		resetActivePedestals();

		if (comp.hasKey(ACTIVE_PEDESTALS_KEY, Constants.NBT.TAG_INT)) {
			int pedestalValues = comp.getInteger(ACTIVE_PEDESTALS_KEY);

			for (int i = 0; i < activePedestals.length; i++) {
				activePedestals[i] = (pedestalValues >> i & 1) == 1;
			}
		}
		else {
			TemporalConvergence.LOGGER.error("Missing activePedestals tag for dimGen at {}", pos);
		}
	}

	//------------------------------Boiler-plate stuff that probably won't change much-----------------------------

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (craftingState.isCrafting())
			return fullClockBB;
		return smallClockBB;
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

	private void sendBlockUpdate() {
		if (!world.isRemote) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 8);
			markDirty();
		}
	}

	@Override
	public void onLoad() {
		//Still a bit buggy due to chunk culling
		fullClockBB = new AxisAlignedBB(pos.add(-6, 0, -6), pos.add(6, 1, 6));
		smallClockBB = new AxisAlignedBB(pos, pos.add(1, 1, 1));

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
}
