package daxum.temporalconvergence.recipes;

import java.util.ArrayList;
import java.util.List;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.power.PowerDimension;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

//TODO: Rewrite this thing, it's a mess.
public final class DimGenRecipes {
	private static List<DimGenRecipe> recipes = new ArrayList();
	private static List<DimBuilderItem> itemInputs = new ArrayList();

	public static boolean addRecipe(ItemStack output, ItemStack centerInput, Object... inputs) {
		List<ItemStack> listInputs = new ArrayList<>();

		for (Object o : inputs) {
			if (!(o instanceof ItemStack)) {
				TemporalConvergence.LOGGER.error("Invalid input for dim gen recipe <" + centerInput + " -> " + output + ">: Recieved " + o.getClass() + " instead of ItemStack. This recipe will be skipped.");
				return false;
			}

			ItemStack stack = (ItemStack) o;

			for (int i = stack.getCount(); i > 0; i--) {
				listInputs.add(new ItemStack(stack.getItem(), 1, stack.getItemDamage()));
				if (listInputs.size() > 12) {
					TemporalConvergence.LOGGER.error("Error trying to register dimGen recipe <" + centerInput + " -> " + output + ">: Too many inputs, must be at most 12");
					return false;
				}
			}
		}

		if (!(listInputs.size() == 2 || listInputs.size() == 4 || listInputs.size() == 8 || listInputs.size() == 12)) {
			TemporalConvergence.LOGGER.error("Error while trying to register dimGen recipe <" + centerInput + " -> " + output + ">: Invalid input size -  must be 2, 4, 8, or 12, got " + listInputs.size());
			return false;
		}

		recipes.add(new DimGenRecipe(output, centerInput, listInputs));
		return true;
	}

	public static void addDimensionInput(ItemStack input, EnumTier tier, EnumBoostingType type) {
		if (input.isEmpty()) {
			TemporalConvergence.LOGGER.error("Tried to register empty ItemStack as dimensional input");
			return;
		}

		itemInputs.add(new DimBuilderItem(input, tier, type));
	}

	public static boolean isValid(ItemStack centerInput, List<ItemStack> inputs) {
		return (inputs.size() == 2 || inputs.size() == 4 || inputs.size() == 8 || inputs.size() == 12) && !getOutput(centerInput, inputs).isEmpty();
	}

	public static ItemStack getOutput(ItemStack centerInput, List<ItemStack> inputs) {
		for (DimGenRecipe i : recipes)
			if (i.areInputsEqual(centerInput, inputs))
				return i.output.copy();
		return ItemStack.EMPTY;

	}

	public static ItemStack getNewDimLink(World world, List<ItemStack> inputs) {
		if (!isValidDimLink(inputs)) return ItemStack.EMPTY;

		int cumulativeInitialAmount = 0;
		int cumulativeStorage = 0;
		int cumulativeIO = 0;

		for (int i = 1; i < inputs.size(); i++) {
			DimBuilderItem dbi = getDimItem(inputs.get(i));

			if (dbi == null) {
				return ItemStack.EMPTY;
			}

			switch (dbi.type) {
			case INITIAL_AMOUNT: cumulativeInitialAmount += dbi.potency.getValueFromTier() * 5208; break; //Gives ~16,000,000 for 12 tier 5's
			case STORAGE_AMOUNT: cumulativeStorage += dbi.potency.getValueFromTier(); break;
			case IO_RATE: cumulativeIO += dbi.potency.getValueFromTier(); break;
			}
		}

		//TODO: These values still might need rebalancing
		int id = PowerDimension.makeNew(world, cumulativeInitialAmount,         //Initial amount
				MathHelper.floor(cumulativeIO / 32.0) + 1,               		//Time loss rate
				MathHelper.floor(cumulativeStorage / 192.0 * 5000000) + 25000,  //Max safe amount
				MathHelper.floor(cumulativeIO / 192.0 * 950000) + 100,          //Max safe i/o rate
				MathHelper.floor((cumulativeStorage + 1) / 96.0 * 8.0) + 1).id; //Time loss frequency
		//Add 1 to cumulativeStorage above so that 11 tier 1's give time loss frequency of 2 (equation gives ~1.92 otherwise)

		ItemStack stack = new ItemStack(ModItems.dimLinker);

		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("dimid", id);

		return stack;
	}

	public static boolean isValidDimLink(List<ItemStack> inputs) {
		if (inputs.size() == 13 && inputs.get(0).getItem() == ModItems.dimLinker) { //Has to have 12 inputs or rendering's a living hell
			boolean hasInitial = false;
			for (int i = 1; i < inputs.size(); i++)
				if (getDimItem(inputs.get(i)) == null)
					return false;
				else if (getDimItem(inputs.get(i)).type == EnumBoostingType.INITIAL_AMOUNT)
					hasInitial = true;

			return hasInitial;
		}

		return false;
	}

	public static DimBuilderItem getDimItem(ItemStack stack) {
		for (int i = 0; i < itemInputs.size(); i++) {
			if (ItemStack.areItemStacksEqual(stack, itemInputs.get(i).item))
				return itemInputs.get(i).copy();
		}

		return null;
	}

	public static class DimGenRecipe {
		public final ItemStack output;
		public final ItemStack mainInput;
		public final List<ItemStack> inputs;

		public DimGenRecipe(ItemStack out, ItemStack main, List<ItemStack> in) {
			inputs = in;
			output = out;
			mainInput = main;
		}

		public boolean areInputsEqual(ItemStack main, List<ItemStack> in) {
			if (in.size() != inputs.size() || !ItemStack.areItemStacksEqual(main, mainInput))
				return false;

			List<ItemStack> testStacks = new ArrayList<>();
			testStacks.addAll(in);

			boolean found = false;
			for (int i = 0; i < inputs.size(); i++) {
				for (int j = 0; j < testStacks.size(); j++)
					if (ItemStack.areItemStacksEqual(inputs.get(i), testStacks.get(j))) {
						testStacks.remove(j);
						found = true;
						break;
					}
				if (!found)
					return false;
				found = false;
			}

			return true;
		}
	}

	public static class DimBuilderItem {
		public final ItemStack item;
		public final EnumTier potency;
		public final EnumBoostingType type;

		public DimBuilderItem(ItemStack input, EnumTier strength, EnumBoostingType t) {
			item = input;
			potency = strength;
			type = t;
		}

		public DimBuilderItem copy() {
			return new DimBuilderItem(item.copy(), potency, type);
		}
	}

	public static enum EnumBoostingType {
		INITIAL_AMOUNT,
		STORAGE_AMOUNT,
		IO_RATE;
	}

	//Each tier is four times as strong as the previous
	public static enum EnumTier {
		TIER1,
		TIER2,
		TIER3,
		TIER4,
		TIER5;

		public int getValueFromTier() {
			switch (this) {
			case TIER1: return 1;
			case TIER2: return 4;
			case TIER3: return 16;
			case TIER4: return 64;
			case TIER5: return 256;
			default: return 42; //How could this even happen??
			}
		}

		public static EnumTier getMaxTier() {
			return EnumTier.values()[EnumTier.values().length - 1];
		}
	}
}
