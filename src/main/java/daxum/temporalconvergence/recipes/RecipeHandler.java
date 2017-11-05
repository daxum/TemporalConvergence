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

import daxum.temporalconvergence.block.ModBlocks;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.power.PowerRequirements;
import daxum.temporalconvergence.power.PowerTypeList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class RecipeHandler {
	public static void init() {
		initDimGen();
		initTimeFurnace();
		initTimeChest();
		GameRegistry.addSmelting(ModBlocks.TIMESTONE_MIX, new ItemStack(ModBlocks.TIME_STONE), 0.1f);
		GameRegistry.addSmelting(ModBlocks.SOLAR_WOOD, new ItemStack(ModItems.ENERGIZED_CHARCOAL), 0.15f);
	}
	/*
	public static void initOreDict() {
		OreDictionary.registerOre("logWood", ModBlocks.LUNAR_WOOD);
		OreDictionary.registerOre("plankWood", ModBlocks.LUNAR_PLANKS);
		OreDictionary.registerOre("logWood", ModBlocks.SOLAR_WOOD);
		OreDictionary.registerOre("plankWood", ModBlocks.SOLAR_PLANKS);
		OreDictionary.registerOre("stone", ModBlocks.TIME_STONE);
	}
	 */
	public static void initDimGen() {
		DimGenRecipes.addRecipe(new ItemStack(ModItems.TIME_FREEZER), new ItemStack(Blocks.CHORUS_FLOWER), new ItemStack(ModItems.TIME_STEEL_INGOT, 4), new ItemStack(ModItems.TIME_DUST, 4), new ItemStack(Items.DIAMOND, 4));
		DimGenRecipes.addRecipe(new ItemStack(Blocks.MELON_BLOCK), new ItemStack(Blocks.PUMPKIN), new ItemStack(Items.DYE, 4, 10)); //Test recipe
	}

	public static void initTimeFurnace() {
		TimeFurnaceRecipes.addRecipe(new ItemStack(Items.IRON_INGOT), new PowerRequirements(PowerTypeList.FIRE, 1600, PowerTypeList.STABLE, 700), new ItemStack(ModItems.STABLE_IRON_INGOT), 400);
	}

	public static void initTimeChest() {
		TimeChestRecipes.addConversion(new ItemStack(Items.DIAMOND), new ItemStack(ModItems.TIME_GEM), 31500);
	}
	/*
	TODO: OreDict the following recipes:
	GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.TIME_SAND, 8), "sss", "sds", "sss", 's', Blocks.SAND, 'd', ModItems.TIME_DUST);
	GameRegistry.addRecipe(new ShapelessOreRecipe(ModItems.INFUSED_WOOD, ModItems.TIME_DUST, "logWood"));
	 */
}
