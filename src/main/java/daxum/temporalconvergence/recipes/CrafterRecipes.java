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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.item.HashableStack;
import daxum.temporalconvergence.power.PowerRequirements;
import net.minecraft.item.ItemStack;

public final class CrafterRecipes {
	private static final Map<HashableStack, CrafterRecipe> RECIPES = new HashMap<>();

	public static void addRecipe(ItemStack output, PowerRequirements power, int craftTime, ItemStack centerInput, ItemStack... inputs) {
		if (output.isEmpty() || centerInput.isEmpty() || craftTime <= 0 || inputs.length > 8) {
			TemporalConvergence.LOGGER.warn("Invalid crafter recipe ({} -> {}) will be skipped", centerInput, output);
			return;
		}

		RECIPES.put(new HashableStack(centerInput), new CrafterRecipe(output, power, craftTime, inputs));
	}

	public static boolean isValidRecipe(ItemStack centerInput, List<ItemStack> inputs) {
		return inputs.size() <= 8 && !getOutput(centerInput, inputs).isEmpty();
	}

	public static ItemStack getOutput(ItemStack centerInput, List<ItemStack> inputs) {
		CrafterRecipe recipe = getRecipe(centerInput, inputs);

		return recipe == null ? ItemStack.EMPTY : recipe.output;
	}

	public static PowerRequirements getPower(ItemStack centerInput, List<ItemStack> inputs) {
		CrafterRecipe recipe = getRecipe(centerInput, inputs);

		return recipe.power;
	}

	public static int getTime(ItemStack centerInput, List<ItemStack> inputs) {
		CrafterRecipe recipe = getRecipe(centerInput, inputs);

		return recipe.time;

	}

	private static CrafterRecipe getRecipe(ItemStack centerInput, List<ItemStack> inputs) {
		HashableStack input = new HashableStack(centerInput);

		if (RECIPES.containsKey(input) && RECIPES.get(input).areInputsEqual(inputs)) {
			return RECIPES.get(input);
		}

		return null;
	}

	public static class CrafterRecipe {
		public final ItemStack output;
		public final ItemStack[] inputs;
		public final PowerRequirements power;
		public final int time;

		public CrafterRecipe(ItemStack out, PowerRequirements powerReq, int craftTime, ItemStack[] inputList) {
			inputs = inputList;
			output = out;
			power = powerReq;
			time = craftTime;
		}

		public boolean areInputsEqual(List<ItemStack> testInputs) {
			if (testInputs.size() != inputs.length) {
				return false;
			}

			List<ItemStack> testStacks = new ArrayList<>();
			testStacks.addAll(testInputs);

			boolean found = false;

			for (int i = 0; i < inputs.length; i++) {
				for (int j = 0; j < testStacks.size(); j++) {
					if (ItemStack.areItemStacksEqual(inputs[i], testStacks.get(j))) {
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
