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

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.IForgeRegistry;

public final class ModBlocks {
	public static final Block ORIGIN_STONE;
	public static final Block TIME_STONE;
	public static final Block TIME_STEEL;
	public static final Block TIME_WOOD;
	public static final Block TIME_WOOD_PLANKS;
	public static final Block TIME_SAND;
	public static final Block TIME_PLANT;
	public static final Block TIME_CHEST;
	public static final Block TIME_WATER;
	public static final Block PEDESTAL;
	public static final Block DIM_GEN;
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

	static {
		ORIGIN_STONE = new BlockBase("origin_stone");
		TIME_STONE = new BlockBase("time_stone");
		TIME_STEEL = new BlockBase(Material.IRON, "time_steel", 5.0f, 30.0f, "pickaxe", 1, SoundType.METAL);
		TIME_WOOD_PLANKS = new BlockBase(Material.WOOD, "time_wood_planks", 2.0f, 15.0f, "axe", 0, SoundType.WOOD) {
			@Override
			public boolean isWood(IBlockAccess world, BlockPos pos) {
				return world.getBlockState(pos).getBlock() == this;
			}
		};

		TIME_WOOD = new BlockTimeWood();
		TIME_SAND = new BlockTimeSand();
		TIME_PLANT = new BlockTimePlant();
		TIME_CHEST = new BlockTimeChest();
		TIME_WATER = new BlockFluidTimeWater();
		PEDESTAL = new BlockPedestal();
		DIM_GEN = new BlockDimGen();
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
	}

	public static void registerBlocks(IForgeRegistry blockRegistry) {
		blockRegistry.register(DIM_CONTR);
		blockRegistry.register(DIM_GEN);
		blockRegistry.register(ORIGIN_STONE);
		blockRegistry.register(PEDESTAL);
		blockRegistry.register(TEST_TELEPORTER);
		blockRegistry.register(TIME_CHEST);
		blockRegistry.register(TIME_PLANT);
		blockRegistry.register(TIME_SAND);
		blockRegistry.register(TIME_STEEL);
		blockRegistry.register(TIME_STONE);
		blockRegistry.register(TIME_STONE_PILLAR);
		blockRegistry.register(TIME_WATER);
		blockRegistry.register(TIME_WOOD);
		blockRegistry.register(TIME_WOOD_PLANKS);
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
	}
}
