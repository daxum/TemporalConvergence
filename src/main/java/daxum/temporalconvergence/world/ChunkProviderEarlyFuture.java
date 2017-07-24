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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import daxum.temporalconvergence.world.futurecity.FutureCityGenerator;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.NoiseGeneratorOctaves;

public class ChunkProviderEarlyFuture implements IChunkGenerator {
	public static final IBlockState TOP_BLOCK = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, DirtType.COARSE_DIRT);
	public static final IBlockState BOTTOM_BLOCK = Blocks.STONE.getDefaultState();
	public static final IBlockState BASE_BLOCK = Blocks.BEDROCK.getDefaultState();
	private static final IBlockState OCEAN_BLOCK = Blocks.WATER.getDefaultState();
	private static final double depthNoiseScaleXZ = 200.0;
	private static final double xzScale = 684.412;
	private static final double yScale = 684.412;
	private static final double mainNoiseScaleXZ = 80.0;
	private static final double mainNoiseScaleY = 160.0;
	private static final double minNoiseScale = 512.0;
	private static final double maxNoiseScale = 512.0;
	private static final double baseSize = 8.0; //Effects where ground level is (bigger number == higher)
	private static final double stretchY = 9.0; //Bigger numbers seem to make everything flatter

	private final World world;
	private final Random heightRand = new Random(); //Used for generating heightMap, consistently has seed reset
	private final Random rand = new Random(); //Generic random for other purposes
	private final double[] heightMap = new double[825];
	private final MapGenBase ravineGenerator = new MapGenRavine();
	private final MapGenBase caveGenerator = new MapGenCaves();
	private FutureCityGenerator cityGenerator = null;

	private NoiseGeneratorOctaves mainPerlinNoise;
	private NoiseGeneratorOctaves minLimitPerlinNoise;
	private NoiseGeneratorOctaves maxLimitPerlinNoise;
	private NoiseGeneratorOctaves depthNoise;

	private double[] depthBuffer = new double[256];
	private double[] mainNoiseRegion;
	private double[] minLimitRegion;
	private double[] maxLimitRegion;
	private double[] depthRegion;

	public ChunkProviderEarlyFuture(World w) {
		world = w;

		mainPerlinNoise = new NoiseGeneratorOctaves(heightRand, 8);
		minLimitPerlinNoise = new NoiseGeneratorOctaves(heightRand, 16);
		maxLimitPerlinNoise = new NoiseGeneratorOctaves(heightRand, 16);
		depthNoise = new NoiseGeneratorOctaves(heightRand, 16);
	}

	@Override
	public Chunk generateChunk(int x, int z) {
		//Needed because chunkProviders are created before mapStorage
		if (cityGenerator == null) {
			cityGenerator = FutureCityGenerator.getGenerator(world);
		}

		ChunkPrimer primer;

		if (cityGenerator.isChunkCity(x, z)) {
			primer = cityGenerator.getPrimerForChunk(x, z);
		}
		else {
			heightRand.setSeed(x * 341873128712L + z * 132897987541L);
			primer = new ChunkPrimer();
			fillPrimer(primer, x, z);
			fillTop(primer);

			caveGenerator.generate(world, x, z, primer);
			ravineGenerator.generate(world, x, z, primer);
		}

		fillWithWater(primer);

		Chunk chunk = new Chunk(world, primer, x, z);
		chunk.generateSkylightMap();

		return chunk;
	}

	private void fillPrimer(ChunkPrimer primer, int x, int z) {
		generateHeightmap(x * 4, z * 4);

		for (int xSection = 0; xSection < 4; ++xSection) {
			int scaledX = xSection * 5;
			int nextScaledX = (xSection + 1) * 5;

			for (int zSection = 0; zSection < 4; ++zSection) {
				int ixz = (scaledX + zSection) * 33;
				int ixz1 = (scaledX + zSection + 1) * 33;
				int ix1z = (nextScaledX + zSection) * 33;
				int ix1z1 = (nextScaledX + zSection + 1) * 33;

				for (int ySection = 0; ySection < 32; ++ySection) {
					double heightXYZ = heightMap[ixz + ySection];
					double heightXYZ1 = heightMap[ixz1 + ySection];
					double heightX1YZ = heightMap[ix1z + ySection];
					double heightX1YZ1 = heightMap[ix1z1 + ySection];
					double yDiffxz = (heightMap[ixz + ySection + 1] - heightXYZ) * 0.125;
					double yDiffxz1 = (heightMap[ixz1 + ySection + 1] - heightXYZ1) * 0.125;
					double yDiffx1z = (heightMap[ix1z + ySection + 1] - heightX1YZ) * 0.125;
					double yDiffx1z1 = (heightMap[ix1z1 + ySection + 1] - heightX1YZ1) * 0.125;

					for (int ySectionOffset = 0; ySectionOffset < 8; ++ySectionOffset) {
						double xyz = heightXYZ;
						double xyz1 = heightXYZ1;
						double xDiffyz = (heightX1YZ - heightXYZ) * 0.25;
						double xDiffyz1 = (heightX1YZ1 - heightXYZ1) * 0.25;

						for (int xSectionOffset = 0; xSectionOffset < 4; ++xSectionOffset) {
							double zDiffxy = (xyz1 - xyz) * 0.25;
							double revZDiff = xyz - zDiffxy;

							for (int zSectionOffset = 0; zSectionOffset < 4; ++zSectionOffset) {
								if (ySection * 8 + ySectionOffset <= rand.nextInt(5)) {
									primer.setBlockState(xSection * 4 + xSectionOffset, ySection * 8 + ySectionOffset, zSection * 4 + zSectionOffset, BASE_BLOCK);
								}
								else if ((revZDiff += zDiffxy) > 0.0) {
									primer.setBlockState(xSection * 4 + xSectionOffset, ySection * 8 + ySectionOffset, zSection * 4 + zSectionOffset, BOTTOM_BLOCK);
								}
							}

							xyz += xDiffyz;
							xyz1 += xDiffyz1;
						}

						heightXYZ += yDiffxz;
						heightXYZ1 += yDiffxz1;
						heightX1YZ += yDiffx1z;
						heightX1YZ1 += yDiffx1z1;
					}
				}
			}
		}
	}

	private void generateHeightmap(int x, int z) {
		depthRegion = depthNoise.generateNoiseOctaves(depthRegion, x, z, 5, 5, depthNoiseScaleXZ, depthNoiseScaleXZ, 0.0); //TODO: last parameter unused?
		mainNoiseRegion = mainPerlinNoise.generateNoiseOctaves(mainNoiseRegion, x, 0, z, 5, 33, 5, xzScale / mainNoiseScaleXZ, yScale / mainNoiseScaleY, xzScale / mainNoiseScaleXZ);
		minLimitRegion = minLimitPerlinNoise.generateNoiseOctaves(minLimitRegion, x, 0, z, 5, 33, 5, xzScale, yScale, xzScale);
		maxLimitRegion = maxLimitPerlinNoise.generateNoiseOctaves(maxLimitRegion, x, 0, z, 5, 33, 5, xzScale, yScale, xzScale);
		int heightMapIndex = 0;
		int depthRegionCounter = 0;

		for (int xVal = 0; xVal < 5; xVal++) {
			for (int zVal = 0; zVal < 5; zVal++) {
				double depth = depthRegion[depthRegionCounter] / 8000.0;

				if (depth < 0.0) {
					depth = -depth * 0.3;
				}

				depth = depth * 3.0 - 2.0;

				if (depth < 0.0) {
					depth = depth / 2.0;

					if (depth < -1.0) {
						depth = -1.0;
					}

					depth = depth / 2.8;
				}
				else {
					if (depth > 1.0) {
						depth = 1.0;
					}

					depth = depth / 8.0;
				}

				depthRegionCounter++;
				double d0 = baseSize + (-0.125 + depth * 0.2) * baseSize / 8.0 * 4.0;

				for (int yVal = 0; yVal < 33; yVal++) {
					double d1 = (yVal - d0) * stretchY * 5;

					if (d1 < 0.0) {
						d1 *= 4.0;
					}

					double d2 = minLimitRegion[heightMapIndex] / minNoiseScale;
					double d3 = maxLimitRegion[heightMapIndex] / maxNoiseScale;
					double d4 = (mainNoiseRegion[heightMapIndex] / 10.0 + 1.0) / 2.0;
					double height = MathHelper.clampedLerp(d2, d3, d4) - d1;

					if (yVal > 29) {
						double d6 = (yVal - 29) / 3.0;
						height = height * (1.0 - d6) + -10.0 * d6;
					}

					heightMap[heightMapIndex] = height;
					++heightMapIndex;
				}
			}
		}
	}

	private void fillTop(ChunkPrimer primer) {
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 255; y >= 0; y--) {
					if (primer.getBlockState(x, y, z).getBlock() == BOTTOM_BLOCK.getBlock()) {
						for (int i = 0; i < 5; i++) {
							if (primer.getBlockState(x, y - i, z).getBlock() == BOTTOM_BLOCK.getBlock()) {
								primer.setBlockState(x, y - i, z, TOP_BLOCK);
							}
						}

						break;
					}
				}
			}
		}
	}

	private void fillWithWater(ChunkPrimer primer) {
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = world.getSeaLevel(); y > 0; y--) {
					if (primer.getBlockState(x, y, z).getBlock() == Blocks.AIR || primer.getBlockState(x, y, z).getBlock() == Blocks.LAVA || primer.getBlockState(x, y, z).getBlock() == Blocks.FLOWING_LAVA) {
						primer.setBlockState(x, y, z, OCEAN_BLOCK);
					}
				}
			}
		}
	}

	@Override
	public void recreateStructures(Chunk chunkIn, int x, int z) {}

	@Override
	public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		return new ArrayList<>();
	}

	@Override
	public void populate(int x, int z) {}


	@Override
	public boolean generateStructures(Chunk chunkIn, int x, int z) {
		return false;
	}

	@Override
	public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
		return null;
	}

	@Override
	public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
		return false;
	}
}
