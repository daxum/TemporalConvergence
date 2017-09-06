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
import daxum.temporalconvergence.power.PowerTypeManager.PowerRequirements;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.items.ItemHandlerHelper;

public final class TimeFurnaceRecipes {
	private static final List<TimeFurnaceRecipe> RECIPES = new ArrayList<>();
	private static final PowerRequirements NO_REQUIREMENTS = new PowerRequirements(); //Used for vanilla furnace recipes

	public static void addRecipe(ItemStack input, PowerRequirements requirements, ItemStack output, int smeltTime) {
		if (!input.isEmpty()) {
			if (smeltTime > 0) {
				RECIPES.add(new TimeFurnaceRecipe(ItemHandlerHelper.copyStackWithSize(input, 1), requirements, ItemHandlerHelper.copyStackWithSize(output, 1), smeltTime));
			}
			else {
				TemporalConvergence.LOGGER.error("Smelt time must be greater than 0");
			}
		}
		else {
			TemporalConvergence.LOGGER.error("Attempted to register empty input to time furnace recipe");
		}
	}

	public static TimeFurnaceRecipe getRecipe(ItemStack input) {
		for (TimeFurnaceRecipe recipe : RECIPES) {
			if (recipe.matches(input)) {
				return recipe.copy(); //Copy might be unnecessary because of for-each loop, but just in case
			}
		}

		ItemStack vanillaResult = FurnaceRecipes.instance().getSmeltingResult(input);

		if (!vanillaResult.isEmpty()) {
			return new TimeFurnaceRecipe(ItemHandlerHelper.copyStackWithSize(input, 1), NO_REQUIREMENTS, vanillaResult.copy(), 200);
		}

		return null;
	}

	public static class TimeFurnaceRecipe {
		public final ItemStack input;
		public final PowerRequirements powerRequired;
		public final ItemStack output;
		public final int smeltTime;

		public TimeFurnaceRecipe(ItemStack in, PowerRequirements requirements, ItemStack out, int time) {
			input = in;
			powerRequired = requirements;
			output = out;
			smeltTime = time;
		}

		public boolean matches(ItemStack in) {
			return ItemStack.areItemStacksEqual(input, ItemHandlerHelper.copyStackWithSize(in, 1));
		}

		public TimeFurnaceRecipe copy() {
			return new TimeFurnaceRecipe(input.copy(), powerRequired, output.copy(), smeltTime);
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			else if (other == null || !(other instanceof TimeFurnaceRecipe)) {
				return false;
			}
			else {
				TimeFurnaceRecipe otherRecipe = (TimeFurnaceRecipe) other;

				boolean inputEqual = ItemStack.areItemStacksEqual(input, otherRecipe.input);
				boolean outputEqual = ItemStack.areItemStacksEqual(output, otherRecipe.output);
				boolean timeEqual = smeltTime == otherRecipe.smeltTime;
				boolean powerEqual = powerRequired.equals(otherRecipe.powerRequired);

				return inputEqual && outputEqual && timeEqual && powerEqual;
			}
		}

		@Override
		public String toString() {
			return "( " + input + " -> " + output + " | " + smeltTime + " " + powerRequired + ")";
		}
	}
}
