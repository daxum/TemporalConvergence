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
import daxum.temporalconvergence.recipes.DimGenRecipes.EnumBoostingType;
import daxum.temporalconvergence.recipes.DimGenRecipes.EnumTier;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public final class RecipeHandler {
	public static void init() {
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.TIME_STEEL), "sss", "sss", "sss", 's', ModItems.TIME_STEEL_INGOT);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.TIME_SAND, 8), "sss", "sds", "sss", 's', Blocks.SAND, 'd', ModItems.TIME_DUST);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.TIME_WOOD_PICK), "www", " s ", " s ", 'w', ModBlocks.TIME_WOOD_PLANKS, 's', ModBlocks.TIME_STONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.TIME_WOOD_SHOVEL), "w", "s", "s", 'w', ModBlocks.TIME_WOOD_PLANKS, 's', ModBlocks.TIME_STONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.TIME_WOOD_AXE), "ww ", "ws ", " s ", 'w', ModBlocks.TIME_WOOD_PLANKS, 's', ModBlocks.TIME_STONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.TIME_CHEST), "wsw", "s s", "wsw", 'w', ModBlocks.TIME_WOOD_PLANKS, 's', ModBlocks.TIME_STONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.PEDESTAL), "sss", " s ", "sss", 's', ModBlocks.TIME_STONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.DIM_GEN), " c ", "dsd", "sss", 'c', Items.CLOCK, 'd', Items.DIAMOND, 's', ModBlocks.TIME_STONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.TIME_STONE_PILLAR, 4), "s" , "s", 's', ModBlocks.TIME_STONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.EARLY_FUTURE_STAIRS, 8), "f  ", "ff ", "fff", 'f', new ItemStack(ModBlocks.EARLY_FUTURE_BLOCK, 1, 0));
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.EARLY_FUTURE_HALF_SLAB, 6), "fff", 'f', new ItemStack(ModBlocks.EARLY_FUTURE_BLOCK, 1, 0));
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.EARLY_FUTURE_BLOCK), "f", "f", 'f', new ItemStack(ModBlocks.EARLY_FUTURE_HALF_SLAB, 1, 0));

		//If I can make timeplants shearable, this'll go away
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.TIME_PEARL), ModBlocks.TIME_PLANT);
		GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.TIME_WOOD_PLANKS, 4),  ModBlocks.TIME_WOOD);
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.TIME_STEEL_INGOT, 9), ModBlocks.TIME_STEEL);
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.DIM_LINKER), ModItems.TIME_PEARL, Items.GLOWSTONE_DUST);
		GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.EARLY_FUTURE_BUTTON, 4), ModBlocks.EARLY_FUTURE_BLOCK);

		GameRegistry.addSmelting(ModBlocks.TIME_SAND, new ItemStack(ModBlocks.TIME_STONE), 0.1f);

		initDimGen();
	}

	public static void initOreDict() {
		OreDictionary.registerOre("logWood", ModBlocks.TIME_WOOD);
		OreDictionary.registerOre("plankWood", ModBlocks.TIME_WOOD_PLANKS);
		OreDictionary.registerOre("stone", ModBlocks.TIME_STONE);
	}

	public static void initDimGen() {
		DimGenRecipes.addRecipe(new ItemStack(ModItems.TIME_FREEZER), new ItemStack(Blocks.CHORUS_FLOWER), new ItemStack(ModItems.TIME_STEEL_INGOT, 4), new ItemStack(ModItems.TIME_DUST, 4), new ItemStack(Items.DIAMOND, 4));
		DimGenRecipes.addRecipe(new ItemStack(Items.COAL), new ItemStack(Items.DIAMOND), new ItemStack(Items.CLOCK, 8)); //Test recipe
		DimGenRecipes.addRecipe(new ItemStack(Blocks.MELON_BLOCK), new ItemStack(Blocks.PUMPKIN), new ItemStack(Items.DYE, 4, 10)); //Test recipe
		DimGenRecipes.addRecipe(new ItemStack(Items.CLAY_BALL), new ItemStack(Items.NETHER_STAR), new ItemStack(Blocks.DRAGON_EGG, 2)); //Balanced test recipe

		//Dimension input
		DimGenRecipes.addDimensionInput(new ItemStack(ModItems.TIME_DUST), EnumTier.TIER1, EnumBoostingType.INITIAL_AMOUNT);
		DimGenRecipes.addDimensionInput(new ItemStack(ModBlocks.TIME_WOOD), EnumTier.TIER1, EnumBoostingType.IO_RATE);
		DimGenRecipes.addDimensionInput(new ItemStack(ModItems.TIME_PEARL), EnumTier.TIER1, EnumBoostingType.STORAGE_AMOUNT);
		DimGenRecipes.addDimensionInput(new ItemStack(ModItems.REWOUND_TIME_SEEDS), EnumTier.TIER2, EnumBoostingType.IO_RATE);
	}
}
