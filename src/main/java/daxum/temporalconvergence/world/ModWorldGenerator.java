package daxum.temporalconvergence.world;

import java.util.Random;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.block.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.IWorldGenerator;

public class ModWorldGenerator implements IWorldGenerator {
	@Override
	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (world.provider.getDimension() == 0)
			generateTimePlants(world, rand, chunkX, chunkZ, 3, 55, 80);
	}

	public void generateTimePlants(World world, Random rand, int chunkX, int chunkZ, int chancesToSpawn, int minHeight, int maxHeight) {
		if (minHeight < 0 || maxHeight > 256) {
			TemporalConvergence.LOGGER.error("generateTimePlants() called with illegal height arguments: Min-" + minHeight + ", Max-" + maxHeight + ". Generation will be skipped.");
			return;
		}

		int heightDiff = Math.abs(maxHeight - minHeight) + 1;

		for (int i = 0; i < chancesToSpawn; i++) {
			int x = chunkX * 16 + rand.nextInt(16);
			int y = minHeight + rand.nextInt(heightDiff);
			int z = chunkZ * 16 + rand.nextInt(16);

			BlockPos pos = new BlockPos(x, y, z);
			BlockPos downPos = pos.down();
			IBlockState state = world.getBlockState(downPos);

			if (world.isAirBlock(pos) && state.getBlock().canSustainPlant(state, world, downPos, EnumFacing.UP, (IPlantable) ModBlocks.TIME_PLANT)) {
				world.setBlockState(pos, ModBlocks.TIME_PLANT.getDefaultState(), 2);
			}
		}
	}
}
