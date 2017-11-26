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
package daxum.temporalconvergence.block;

import daxum.temporalconvergence.block.BlockBase.BlockPresets;
import daxum.temporalconvergence.tileentity.TileBrazier;
import daxum.temporalconvergence.tileentity.TileCrafter;
import daxum.temporalconvergence.tileentity.TileDimContr;
import daxum.temporalconvergence.tileentity.TileFutureChest;
import daxum.temporalconvergence.tileentity.TilePedestal;
import daxum.temporalconvergence.tileentity.TileTimeChest;
import daxum.temporalconvergence.tileentity.TileTimeFurnace;
import daxum.temporalconvergence.tileentity.TileTimeFurnaceController;
import daxum.temporalconvergence.tileentity.TileTimePlant;
import daxum.temporalconvergence.tileentity.TileTimeVault;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModBlocks {
	public static final Block ORIGIN_STONE;
	public static final Block TIME_STONE;
	public static final Block TIME_STEEL;
	public static final Block LUNAR_WOOD;
	public static final Block LUNAR_PLANKS;
	public static final Block TIMESTONE_MIX;
	public static final Block TIME_PLANT;
	public static final Block TIME_CHEST;
	public static final Block TIME_WATER;
	public static final Block PEDESTAL;
	public static final Block TIME_STONE_PILLAR;
	public static final Block TEST_TELEPORTER;
	public static final Block DIM_CONTR;
	public static final Block EARLY_FUTURE_BLOCK;
	public static final Block FANCY_EARLY_FUTURE_STAIRS;
	public static final Block EARLY_FUTURE_STAIRS;
	public static final Block EARLY_FUTURE_DOOR;
	public static final Block EARLY_FUTURE_FENCE;
	public static final Block EARLY_FUTURE_HALF_SLAB;
	public static final Block EARLY_FUTURE_DOUBLE_SLAB;
	public static final Block REWOUND_SOIL;
	public static final Block REWOUND_TIME;
	public static final Block EARLY_FUTURE_BUTTON;
	public static final Block BRAZIER;
	public static final Block SOLAR_WOOD;
	public static final Block SOLAR_PLANKS;
	public static final Block BOSS_SCREEN;
	public static final Block FAKE_BOSS_SCREEN;
	public static final Block TIME_FURNACE;
	public static final Block EARLY_FUTURE_ROAD_STRIPE;
	public static final Block EARLY_FUTURE_ROAD;
	public static final Block EARLY_FUTURE_ROAD_BORDER;
	public static final Block ROAD_BORDER_STAIRS;
	public static final Block FUTURE_CHEST;
	public static final Block SOLAR_LEAVES;
	public static final Block SOLAR_SAPLING;
	public static final Block BRAZIER_BOTTOM;
	public static final Block LUNAR_LEAVES;
	public static final Block LUNAR_SAPLING;
	public static final Block CRAFTER;
	public static final Block TIME_VAULT;

	static {
		ORIGIN_STONE = new BlockBase("origin_stone");
		TIME_STONE = new BlockBase("time_stone");
		TIME_STEEL = new BlockBase("time_steel", BlockPresets.IRON);
		LUNAR_PLANKS = new BlockBase("lunar_planks", BlockPresets.WOOD) {
			@Override
			public boolean isWood(IBlockAccess world, BlockPos pos) {
				return world.getBlockState(pos).getBlock() == this;
			}
		};
		EARLY_FUTURE_ROAD = new BlockBase("early_future_road", BlockPresets.STONE);
		EARLY_FUTURE_ROAD_BORDER = new BlockBase("early_future_road_border", BlockPresets.STONE);

		LUNAR_WOOD = new BlockLunarWood();
		TIMESTONE_MIX = new BlockTimestoneMix();
		TIME_PLANT = new BlockTimePlant();
		TIME_CHEST = new BlockTimeChest();
		TIME_WATER = new BlockFluidTimeWater();
		PEDESTAL = new BlockPedestal();
		TIME_STONE_PILLAR = new BlockTimeStonePillar();
		TEST_TELEPORTER = new BlockTestTeleporter();
		DIM_CONTR = new BlockDimContr();
		EARLY_FUTURE_BLOCK = new BlockEarlyFuture();
		FANCY_EARLY_FUTURE_STAIRS = new BlockFancyEarlyFutureStairs();
		EARLY_FUTURE_STAIRS = new BlockEarlyFutureStairs();
		EARLY_FUTURE_DOOR = new BlockEarlyFutureDoor();
		EARLY_FUTURE_FENCE = new BlockEarlyFutureFence();
		EARLY_FUTURE_HALF_SLAB = new BlockEarlyFutureSlab() {
			@Override
			public boolean isDouble() { return false; }
		};
		EARLY_FUTURE_DOUBLE_SLAB = new BlockEarlyFutureSlab() {
			@Override
			public boolean isDouble() { return true; }
		};
		REWOUND_SOIL = new BlockRewoundTimeSoil();
		REWOUND_TIME = new BlockRewoundTime();
		EARLY_FUTURE_BUTTON = new BlockButtonFuture();
		BRAZIER = new BlockBrazier();
		SOLAR_WOOD = new BlockSolarWood();
		SOLAR_PLANKS = new BlockBase("solar_planks", BlockPresets.WOOD) {
			@Override
			public boolean isWood(IBlockAccess world, BlockPos pos) {
				return world.getBlockState(pos).getBlock() == this;
			}
		};
		BOSS_SCREEN = new BlockAIBossScreen();
		FAKE_BOSS_SCREEN = new BlockFakeScreen();
		TIME_FURNACE = new BlockTimeFurnace();
		EARLY_FUTURE_ROAD_STRIPE = new BlockEarlyFutureRoadStripe();
		ROAD_BORDER_STAIRS = new BlockRoadBorderStairs();
		FUTURE_CHEST = new BlockFutureChest();
		SOLAR_LEAVES = new BlockSolarLeaves();
		SOLAR_SAPLING = new BlockSaplingBase("solar_sapling", BlockPresets.PLANT, SOLAR_WOOD.getDefaultState(), SOLAR_LEAVES.getDefaultState());
		BRAZIER_BOTTOM = new BlockBrazierBottom();
		LUNAR_LEAVES = new BlockLunarLeaves();
		LUNAR_SAPLING = new BlockSaplingBase("lunar_sapling", BlockPresets.PLANT, LUNAR_WOOD.getDefaultState(), LUNAR_LEAVES.getDefaultState());
		CRAFTER = new BlockCrafter();
		TIME_VAULT = new BlockTimeVault();
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry blockRegistry = event.getRegistry();

		blockRegistry.register(DIM_CONTR);
		blockRegistry.register(ORIGIN_STONE);
		blockRegistry.register(PEDESTAL);
		blockRegistry.register(TEST_TELEPORTER);
		blockRegistry.register(TIME_CHEST);
		blockRegistry.register(TIME_PLANT);
		blockRegistry.register(TIMESTONE_MIX);
		blockRegistry.register(TIME_STEEL);
		blockRegistry.register(TIME_STONE);
		blockRegistry.register(TIME_STONE_PILLAR);
		blockRegistry.register(TIME_WATER);
		blockRegistry.register(LUNAR_WOOD);
		blockRegistry.register(LUNAR_PLANKS);
		blockRegistry.register(EARLY_FUTURE_BLOCK);
		blockRegistry.register(FANCY_EARLY_FUTURE_STAIRS);
		blockRegistry.register(EARLY_FUTURE_STAIRS);
		blockRegistry.register(EARLY_FUTURE_DOOR);
		blockRegistry.register(EARLY_FUTURE_FENCE);
		blockRegistry.register(EARLY_FUTURE_HALF_SLAB);
		blockRegistry.register(EARLY_FUTURE_DOUBLE_SLAB);
		blockRegistry.register(REWOUND_SOIL);
		blockRegistry.register(REWOUND_TIME);
		blockRegistry.register(EARLY_FUTURE_BUTTON);
		blockRegistry.register(BRAZIER);
		blockRegistry.register(SOLAR_WOOD);
		blockRegistry.register(SOLAR_PLANKS);
		blockRegistry.register(BOSS_SCREEN);
		blockRegistry.register(FAKE_BOSS_SCREEN);
		blockRegistry.register(TIME_FURNACE);
		blockRegistry.register(EARLY_FUTURE_ROAD_STRIPE);
		blockRegistry.register(EARLY_FUTURE_ROAD);
		blockRegistry.register(EARLY_FUTURE_ROAD_BORDER);
		blockRegistry.register(ROAD_BORDER_STAIRS);
		blockRegistry.register(FUTURE_CHEST);
		blockRegistry.register(SOLAR_LEAVES);
		blockRegistry.register(SOLAR_SAPLING);
		blockRegistry.register(BRAZIER_BOTTOM);
		blockRegistry.register(LUNAR_LEAVES);
		blockRegistry.register(LUNAR_SAPLING);
		blockRegistry.register(CRAFTER);
		blockRegistry.register(TIME_VAULT);

		//Tile entities
		GameRegistry.registerTileEntity(TileDimContr.class, "dim_controller");
		GameRegistry.registerTileEntity(TilePedestal.class, "time_pedestal");
		GameRegistry.registerTileEntity(TileTimeChest.class, "time_chest");
		GameRegistry.registerTileEntity(TileTimePlant.class, "time_plant");
		GameRegistry.registerTileEntity(TileBrazier.class, "brazier");
		GameRegistry.registerTileEntity(TileTimeFurnace.class, "time_furnace");
		GameRegistry.registerTileEntity(TileTimeFurnaceController.class, "time_furnace_controller");
		GameRegistry.registerTileEntity(TileFutureChest.class, "future_chest");
		GameRegistry.registerTileEntity(TileCrafter.class, "time_crafter");
		GameRegistry.registerTileEntity(TileTimeVault.class, "time_vault");
	}
}
