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
	}

	public static void registerBlocks(IForgeRegistry blockRegistry) {
		blockRegistry.register(ModBlocks.DIM_CONTR);
		blockRegistry.register(ModBlocks.DIM_GEN);
		blockRegistry.register(ModBlocks.ORIGIN_STONE);
		blockRegistry.register(ModBlocks.PEDESTAL);
		blockRegistry.register(ModBlocks.TEST_TELEPORTER);
		blockRegistry.register(ModBlocks.TIME_CHEST);
		blockRegistry.register(ModBlocks.TIME_PLANT);
		blockRegistry.register(ModBlocks.TIME_SAND);
		blockRegistry.register(ModBlocks.TIME_STEEL);
		blockRegistry.register(ModBlocks.TIME_STONE);
		blockRegistry.register(ModBlocks.TIME_STONE_PILLAR);
		blockRegistry.register(ModBlocks.TIME_WATER);
		blockRegistry.register(ModBlocks.TIME_WOOD);
		blockRegistry.register(ModBlocks.TIME_WOOD_PLANKS);
	}
}
