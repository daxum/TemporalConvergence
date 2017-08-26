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
package daxum.temporalconvergence.world;

import java.util.Random;

import daxum.temporalconvergence.block.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModWorldGenerator implements IWorldGenerator {
	private static final WorldGenTrees SOLAR_TREE_GEN = new WorldGenTrees(false, 5, ModBlocks.SOLAR_WOOD.getDefaultState(), ModBlocks.SOLAR_LEAVES.getDefaultState(), false);

	@Override
	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (world.provider.getDimensionType() == DimensionType.OVERWORLD) {
			generateTimePlants(world, rand, chunkX, chunkZ, 3, 55, 80);
		}
	}

	@SubscribeEvent
	public static void decorate(DecorateBiomeEvent.Post event) {
		World world = event.getWorld();

		if (world.provider.getDimensionType() == DimensionType.OVERWORLD) {
			generateTrees(world, new Random(), event.getPos().getX() >> 4, event.getPos().getZ() >> 4, 5);
		}
	}

	private void generateTimePlants(World world, Random rand, int chunkX, int chunkZ, int chancesToSpawn, int minHeight, int maxHeight) {
		int heightDiff = Math.abs(maxHeight - minHeight) + 1;

		for (int i = 0; i < chancesToSpawn; i++) {
			int x = chunkX * 16 + rand.nextInt(14) + 1;
			int y = minHeight + rand.nextInt(heightDiff);
			int z = chunkZ * 16 + rand.nextInt(14) + 1;

			BlockPos pos = new BlockPos(x, y, z);
			BlockPos downPos = pos.down();
			IBlockState state = world.getBlockState(downPos);

			if (world.isAirBlock(pos) && state.getBlock().canSustainPlant(state, world, downPos, EnumFacing.UP, (IPlantable) ModBlocks.TIME_PLANT)) {
				world.setBlockState(pos, ModBlocks.TIME_PLANT.getDefaultState(), 2);
			}
		}
	}

	private static void generateTrees(World world, Random rand, int chunkX, int chunkZ, int chance) {
		if (rand.nextInt(chance) == 0) {
			int x = chunkX * 16 + rand.nextInt(10) + 3;
			int z = chunkZ * 16 + rand.nextInt(10) + 3;
			int y = world.getHeight(x, z);

			BlockPos pos = new BlockPos(x, y, z);

			if (isTrunkClear(world, pos)) {
				BlockPos downPos = pos.down();
				IBlockState state = world.getBlockState(downPos);

				if (state.getBlock().canSustainPlant(state, world, downPos, EnumFacing.UP, (IPlantable) ModBlocks.SOLAR_SAPLING)) {
					SOLAR_TREE_GEN.generate(world, rand, pos);
				}
			}
		}
	}

	//check the eight blocks around the base of the trunk, mainly to prevent spawning directly next to other trees
	private static boolean isTrunkClear(World world, BlockPos pos) {
		return isBlockReplaceable(world, pos.north().west()) && isBlockReplaceable(world, pos.north()) && isBlockReplaceable(world, pos.north().east())
				&& isBlockReplaceable(world, pos.west()) && isBlockReplaceable(world, pos.east())
				&& isBlockReplaceable(world, pos.south().west()) && isBlockReplaceable(world, pos.south()) && isBlockReplaceable(world, pos.south().east());
	}

	private static boolean isBlockReplaceable(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
	}
}
