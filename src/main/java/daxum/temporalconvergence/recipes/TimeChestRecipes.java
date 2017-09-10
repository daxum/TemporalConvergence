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
import net.minecraftforge.items.ItemHandlerHelper;

public final class TimeChestRecipes {
	private static final List<TimeChestRecipe> RECIPES = new ArrayList<>();

	public static void addConversion(ItemStack input, ItemStack output, int time) {
		if (input.isEmpty() || output.isEmpty() || time <= 0) {
			TemporalConvergence.LOGGER.info("Attempted to register invalid time chest conversion: {} -> {} : {}", input, output, time);
			return;
		}

		RECIPES.add(new TimeChestRecipe(input.copy(), output.copy(), time));
	}

	public static ItemStack getOutput(ItemStack input) {
		ItemStack adjustedInput = ItemHandlerHelper.copyStackWithSize(input, 1);

		for (TimeChestRecipe recipe : RECIPES) {
			if (ItemStack.areItemStacksEqual(adjustedInput, recipe.input)) {
				return recipe.output.copy();
			}
		}

		return ItemStack.EMPTY;
	}

	public static int getTime(ItemStack input) {
		ItemStack adjustedInput = ItemHandlerHelper.copyStackWithSize(input, 1);

		for (TimeChestRecipe recipe : RECIPES) {
			if (ItemStack.areItemStacksEqual(adjustedInput, recipe.input)) {
				return recipe.time;
			}
		}

		return -1;
	}

	public static class TimeChestRecipe {
		public final ItemStack input;
		public final ItemStack output;
		public final int time;

		public TimeChestRecipe(ItemStack in, ItemStack out, int t) {
			input = in;
			output = out;
			time = t;
		}
	}
}
