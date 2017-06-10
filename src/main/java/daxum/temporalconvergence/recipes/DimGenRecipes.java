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
package daxum.temporalconvergence.recipes;

import java.util.ArrayList;
import java.util.List;

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraft.item.ItemStack;

//TODO: Rewrite this thing, it's a mess.
public final class DimGenRecipes {
	private static List<DimGenRecipe> recipes = new ArrayList();

	public static boolean addRecipe(ItemStack output, ItemStack centerInput, Object... inputs) {
		List<ItemStack> listInputs = new ArrayList<>();

		for (Object o : inputs) {
			if (!(o instanceof ItemStack)) {
				TemporalConvergence.LOGGER.error("Invalid input for dim gen recipe <{} -> {}>: Recieved {} instead of ItemStack. This recipe will be skipped.", centerInput, output, o.getClass());
				return false;
			}

			ItemStack stack = (ItemStack) o;

			for (int i = 0; i < stack.getCount(); i++) {
				listInputs.add(new ItemStack(stack.getItem(), 1, stack.getItemDamage()));

				if (listInputs.size() > 12) {
					TemporalConvergence.LOGGER.error("Error trying to register dimGen recipe <{} -> {}>: Too many inputs, must be at most 12", centerInput, output);
					return false;
				}
			}
		}

		recipes.add(new DimGenRecipe(output, centerInput, listInputs));
		return true;
	}

	public static boolean isValidRecipe(ItemStack centerInput, List<ItemStack> inputs) {
		return inputs.size() <= 12 && !getOutput(centerInput, inputs).isEmpty();
	}

	public static ItemStack getOutput(ItemStack centerInput, List<ItemStack> inputs) {
		for (DimGenRecipe i : recipes) {
			if (i.areInputsEqual(centerInput, inputs)) {
				return i.output.copy();
			}
		}

		return ItemStack.EMPTY;

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
			if (in.size() != inputs.size() || !ItemStack.areItemStacksEqual(main, mainInput)) {
				return false;
			}

			List<ItemStack> testStacks = new ArrayList<>();
			testStacks.addAll(in);

			boolean found = false;
			for (int i = 0; i < inputs.size(); i++) {
				for (int j = 0; j < testStacks.size(); j++) {
					if (ItemStack.areItemStacksEqual(inputs.get(i), testStacks.get(j))) {
						testStacks.remove(j);
						found = true;
						break;
					}
				}

				if (!found) {
					return false;
				}

				found = false;
			}

			return true;
		}
	}
}
