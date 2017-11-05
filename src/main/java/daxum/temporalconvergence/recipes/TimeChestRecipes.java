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

import java.util.HashMap;
import java.util.Map;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.item.HashableStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public final class TimeChestRecipes {
	private static final Map<HashableStack, TimeChestRecipe> RECIPES = new HashMap<>();

	public static void addConversion(ItemStack input, ItemStack output, int time) {
		if (input.isEmpty() || output.isEmpty() || time <= 0) {
			TemporalConvergence.LOGGER.warn("Attempted to register invalid time chest conversion: {} -> {} : {}", input, output, time);
			return;
		}

		RECIPES.put(new HashableStack(ItemHandlerHelper.copyStackWithSize(input, 1)), new TimeChestRecipe(output.copy(), time));
	}

	public static ItemStack getOutput(ItemStack input) {
		HashableStack adjustedInput = new HashableStack(ItemHandlerHelper.copyStackWithSize(input, 1));

		if (RECIPES.containsKey(adjustedInput)) {
			return RECIPES.get(adjustedInput).output;
		}

		return ItemStack.EMPTY;
	}

	public static int getTime(ItemStack input) {
		HashableStack adjustedInput = new HashableStack(ItemHandlerHelper.copyStackWithSize(input, 1));

		if (RECIPES.containsKey(adjustedInput)) {
			return RECIPES.get(adjustedInput).time;
		}

		return -1;
	}

	public static class TimeChestRecipe {
		public final ItemStack output;
		public final int time;

		public TimeChestRecipe(ItemStack out, int t) {
			output = out;
			time = t;
		}
	}
}
