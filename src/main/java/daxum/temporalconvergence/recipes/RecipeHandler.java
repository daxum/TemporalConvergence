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
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.timeSteel), "sss", "sss", "sss", 's', ModItems.timeSteelIngot);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.timeSand, 8), "sss", "sds", "sss", 's', Blocks.SAND, 'd', ModItems.timeDust);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.timeWoodPick), "www", " s ", " s ", 'w', ModBlocks.timeWoodPlanks, 's', ModBlocks.timeStone);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.timeWoodShovel), "w", "s", "s", 'w', ModBlocks.timeWoodPlanks, 's', ModBlocks.timeStone);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.timeWoodAxe), "ww ", "ws ", " s ", 'w', ModBlocks.timeWoodPlanks, 's', ModBlocks.timeStone);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.timeChest), "wsw", "s s", "wsw", 'w', ModBlocks.timeWoodPlanks, 's', ModBlocks.timeStone);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.timePedestal), "sss", " s ", "sss", 's', ModBlocks.timeStone);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.dimGen), " c ", "dsd", "sss", 'c', Items.CLOCK, 'd', Items.DIAMOND, 's', ModBlocks.timeStone);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.timeStonePillar, 4), "s" , "s", 's', ModBlocks.timeStone);

		//If I can make timeplants shearable, this'll go away
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.timePearl), ModBlocks.timePlant);
		GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.timeWoodPlanks, 4),  ModBlocks.timeWood);
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.timeSteelIngot, 9), ModBlocks.timeSteel);
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.dimLinker), ModItems.timePearl, Items.GLOWSTONE_DUST);

		GameRegistry.addSmelting(ModBlocks.timeSand, new ItemStack(ModBlocks.timeStone), 0.1f);

		initDimGen();
	}

	public static void initOreDict() {
		OreDictionary.registerOre("logWood", ModBlocks.timeWood);
		OreDictionary.registerOre("plankWood", ModBlocks.timeWoodPlanks);
		OreDictionary.registerOre("stone", ModBlocks.timeStone);
	}

	public static void initDimGen() {
		DimGenRecipes.addRecipe(new ItemStack(ModItems.timeFreezer), new ItemStack(Blocks.CHORUS_FLOWER), new ItemStack(ModItems.timeSteelIngot, 4), new ItemStack(ModItems.timeDust, 8));
		DimGenRecipes.addRecipe(new ItemStack(Items.COAL), new ItemStack(Items.DIAMOND), new ItemStack(Items.CLOCK, 8)); //Test recipe
		DimGenRecipes.addRecipe(new ItemStack(Blocks.MELON_BLOCK), new ItemStack(Blocks.PUMPKIN), new ItemStack(Items.DYE, 4, 10)); //Test recipe
		DimGenRecipes.addRecipe(new ItemStack(Items.CLAY_BALL), new ItemStack(Items.NETHER_STAR), new ItemStack(Blocks.DRAGON_EGG, 2)); //Balanced test recipe

		initDimInput();
	}

	public static void initDimInput() {
		DimGenRecipes.addDimensionInput(new ItemStack(ModItems.timeDust), EnumTier.TIER1, EnumBoostingType.INITIAL_AMOUNT);
		DimGenRecipes.addDimensionInput(new ItemStack(ModBlocks.timeWood), EnumTier.TIER1, EnumBoostingType.IO_RATE);
		DimGenRecipes.addDimensionInput(new ItemStack(ModItems.timePearl), EnumTier.TIER1, EnumBoostingType.STORAGE_AMOUNT);
	}
}
