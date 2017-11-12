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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import daxum.temporalconvergence.power.DirectPowerHelper;
import daxum.temporalconvergence.power.IDirectPowerProvider;
import daxum.temporalconvergence.power.IDirectPowerReceiver;
import daxum.temporalconvergence.power.PowerRequirements;
import daxum.temporalconvergence.power.PowerType;
import daxum.temporalconvergence.power.PowerTypeList;
import daxum.temporalconvergence.power.PowerTypeManager;
import daxum.temporalconvergence.recipes.CrafterRecipes;
import daxum.temporalconvergence.util.WorldHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileCrafter extends TileEntityBase implements TileEntityInventoried, IDirectPowerReceiver, ITickable {
	private Set<BlockPos> providers = new HashSet<>();
	private final ItemStackHandler inventory = new CrafterInventory();
	private List<ItemStack> currentRecipe = new ArrayList<>();
	private final List<PowerData> recipePower = new ArrayList<>();
	private boolean crafting = false;
	private int craftTicks = 0;

	@Override
	public void update() {
		if (!world.isRemote && crafting) {
			if (craftTicks > 0) {
				craftTicks--;
			}

			for (int i = recipePower.size() - 1; i >= 0; i--) {
				int amount = recipePower.get(i).perTick;
				PowerType type = recipePower.get(i).type;

				List<IDirectPowerProvider> providerList = new ArrayList<>();

				for (BlockPos prov : providers) {
					TileEntity provider = world.getTileEntity(prov);

					if (provider != null && provider instanceof IDirectPowerProvider) {
						if (((IDirectPowerProvider)provider).getTypeProvided().equals(type)) {
							providerList.add((IDirectPowerProvider)provider);
						}
					}
					else {
						providers.remove(prov);
					}
				}

				int got = DirectPowerHelper.getPowerDistributed(providerList, type, amount);

				recipePower.get(i).add(got);

				if (recipePower.get(i).done()) {
					recipePower.remove(i);
				}
			}

			if (recipePower.isEmpty() && craftTicks <= 0) {
				for (int i = 1; i < inventory.getSlots(); i++) {
					inventory.getStackInSlot(i).shrink(1);
				}

				ItemStack centerItem = currentRecipe.get(CrafterInventory.CENTER_ITEM_INDEX);
				List<ItemStack> items = currentRecipe.subList(1, currentRecipe.size());

				inventory.setStackInSlot(CrafterInventory.CENTER_ITEM_INDEX, CrafterRecipes.getOutput(centerItem, items).copy());

				stopCrafting();
			}

			sendBlockUpdate();
		}
	}

	public boolean startCrafting() {
		if (crafting) {
			return false;
		}

		currentRecipe = getRecipeList();

		ItemStack centerItem = currentRecipe.get(CrafterInventory.CENTER_ITEM_INDEX);
		List<ItemStack> items = currentRecipe.subList(1, currentRecipe.size());

		if (CrafterRecipes.isValidRecipe(centerItem, items)) {
			PowerRequirements requirements = CrafterRecipes.getPower(centerItem, items);
			craftTicks = CrafterRecipes.getTime(centerItem, items);

			recipePower.clear();

			for (int i = 0; i < requirements.getTypesRequired().length; i++) {
				PowerType type = requirements.getTypesRequired()[i];
				int amount = requirements.getAmountForType(type);

				PowerData data = new PowerData(type, amount, amount / craftTicks);

				recipePower.add(data);
			}

			crafting = true;
			sendBlockUpdate();

			return true;
		}

		return false;
	}

	public void stopCrafting() {
		crafting = false;
		sendBlockUpdate();
	}

	private List<ItemStack> getRecipeList() {
		List<ItemStack> stacks = new ArrayList<>();

		for (int i = 0; i < inventory.getSlots(); i++) {
			if (!inventory.getStackInSlot(i).isEmpty()) {
				stacks.add(inventory.getStackInSlot(i).copy());
				stacks.get(stacks.size() - 1).setCount(1);
			}
		}

		return stacks;
	}

	@Override
	public ItemStackHandler getInventory() {
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
		public static final int CENTER_ITEM_INDEX = 0;

		public CrafterInventory() {
			super(9);
		}

		@Override
		public int getSlotLimit(int slot) {
			if (slot == CENTER_ITEM_INDEX) {
				return 1;
			}

			return super.getSlotLimit(slot);
		}

		@Override
		protected void onContentsChanged(int slot) {
			List<ItemStack> stacks = getRecipeList();

			if (stacks.size() != currentRecipe.size()) {
				stopCrafting();
				return;
			}

			for (int i = 0; i < stacks.size(); i++) {
				ItemStack recipe = currentRecipe.get(i);
				ItemStack current = stacks.get(i);

				if (!(ItemStack.areItemsEqual(recipe, current) && ItemStack.areItemStackTagsEqual(recipe, current))) {
					stopCrafting();
					return;
				}
			}
		}
	}

	public class PowerData {
		public final PowerType type;
		private final int required;
		public final int perTick;
		private int gotten;

		public PowerData(PowerType t, int req, int pt) {
			type = t;
			required = req;
			perTick = pt;
			gotten = 0;
		}

		public PowerData(NBTTagCompound comp) {
			boolean missing = false;

			if (comp.hasKey("type", Constants.NBT.TAG_STRING)) {
				type = PowerTypeManager.getType(comp.getString("type"));
			}
			else {
				missing = true;
				type = PowerTypeList.POWER_0;
			}

			if (comp.hasKey("req", Constants.NBT.TAG_INT)) {
				required = comp.getInteger("req");
			}
			else {
				missing = true;
				required = 0;
			}

			if (comp.hasKey("per", Constants.NBT.TAG_INT)) {
				perTick = comp.getInteger("per");
			}
			else {
				missing = true;
				perTick = 0;
			}

			if (comp.hasKey("got", Constants.NBT.TAG_INT)) {
				gotten = comp.getInteger("got");
			}
			else {
				missing = true;
				gotten = 0;
			}

			if (missing) {
				throw new IllegalArgumentException("Invalid tag to PowerData constructor");
			}
		}

		public void add(int amount) {
			gotten += amount;
		}

		public boolean done() {
			return gotten >= required;
		}

		public NBTTagCompound serializeNBT() {
			NBTTagCompound comp = new NBTTagCompound();

			comp.setString("type", type.getName());
			comp.setInteger("req", required);
			comp.setInteger("per", perTick);
			comp.setInteger("got", gotten);

			return comp;
		}
	}

	private static final String INVENTORY_TAG = "inv";
	private static final String CRAFT_TAG = "craft";
	private static final String CRAFT_TICK_TAG = "ctick";
	private static final String CURRENT_RECIPE_TAG = "recipe";
	private static final String POWER_TAG = "power";

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setTag(INVENTORY_TAG, inventory.serializeNBT());
		comp.setBoolean(CRAFT_TAG, crafting);
		comp.setInteger(CRAFT_TICK_TAG, craftTicks);
		saveRecipe(comp);
		savePower(comp);

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey(INVENTORY_TAG, Constants.NBT.TAG_COMPOUND)) {
			inventory.deserializeNBT(comp.getCompoundTag(INVENTORY_TAG));
		}

		if (comp.hasKey(CRAFT_TAG, Constants.NBT.TAG_BYTE)) {
			crafting = comp.getBoolean(CRAFT_TAG);
		}

		if (comp.hasKey(CRAFT_TICK_TAG, Constants.NBT.TAG_INT)) {
			craftTicks = comp.getInteger(CRAFT_TICK_TAG);
		}

		loadRecipe(comp);
		loadPower(comp);

		super.readFromNBT(comp);
	}

	private void saveRecipe(NBTTagCompound comp) {
		NBTTagList list = new NBTTagList();

		for (int i = 0; i < currentRecipe.size(); i++) {
			list.appendTag(currentRecipe.get(i).serializeNBT());
		}

		comp.setTag(CURRENT_RECIPE_TAG, list);
	}

	private void loadRecipe(NBTTagCompound comp) {
		if (!comp.hasKey(CURRENT_RECIPE_TAG, Constants.NBT.TAG_LIST)) {
			return;
		}

		NBTTagList list = comp.getTagList(CURRENT_RECIPE_TAG, Constants.NBT.TAG_COMPOUND);

		currentRecipe.clear();

		for (NBTBase argh : list) {
			currentRecipe.add(new ItemStack((NBTTagCompound)argh));
		}
	}

	private void savePower(NBTTagCompound comp) {
		NBTTagList list = new NBTTagList();

		for (int i = 0; i < recipePower.size(); i++) {
			list.appendTag(recipePower.get(i).serializeNBT());
		}

		comp.setTag(POWER_TAG, list);
	}

	private void loadPower(NBTTagCompound comp) {
		if (!comp.hasKey(POWER_TAG, Constants.NBT.TAG_LIST)) {
			return;
		}

		NBTTagList list = comp.getTagList(POWER_TAG, Constants.NBT.TAG_COMPOUND);

		recipePower.clear();

		for (NBTBase argh : list) {
			recipePower.add(new PowerData((NBTTagCompound)argh));
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
}
