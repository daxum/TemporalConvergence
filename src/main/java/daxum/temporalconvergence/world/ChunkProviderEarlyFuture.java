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

import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

//Pretty much everything here was taken from ChunkProviderOverworld, because I don't know how most of this works :/
public class ChunkProviderEarlyFuture implements IChunkGenerator {
	private final Random rand = new Random();
	private final World world;
	private final double[] heightMap = new double[825];
	private final float[] biomeWeights = new float[25];
	private NoiseGeneratorOctaves depthNoise;
	private NoiseGeneratorOctaves mainPerlinNoise;
	private NoiseGeneratorOctaves minLimitPerlinNoise;
	private NoiseGeneratorOctaves maxLimitPerlinNoise;
	private NoiseGeneratorPerlin surfaceNoise;
	private MapGenBase caveGenerator = new MapGenCaves();
	private MapGenBase ravineGenerator = new MapGenRavine();
	private Biome[] biomesForGeneration;
	private double[] depthBuffer = new double[256];
	private double[] depthRegion;
	private double[] mainNoiseRegion;
	private double[] minLimitRegion;
	private double[] maxLimitRegion;

	public ChunkProviderEarlyFuture(World w) {
		world = w;

		depthNoise = new NoiseGeneratorOctaves(rand, 16);
		mainPerlinNoise = new NoiseGeneratorOctaves(rand, 8);
		minLimitPerlinNoise = new NoiseGeneratorOctaves(rand, 16);
		maxLimitPerlinNoise = new NoiseGeneratorOctaves(rand, 16);
		surfaceNoise = new NoiseGeneratorPerlin(rand, 4);

		for (int i = -2; i <= 2; ++i)
			for (int j = -2; j <= 2; ++j)
				biomeWeights[i + 2 + (j + 2) * 5] = 10.0f / MathHelper.sqrt(i * i + j * j + 0.2f);
	}

	@Override
	public Chunk provideChunk(int x, int z) {
		rand.setSeed(x * 348873128752L + z * 43279798741L);
		ChunkPrimer chunkprimer = new ChunkPrimer();
		setBlocksInChunk(x, z, chunkprimer);
		biomesForGeneration = world.getBiomeProvider().getBiomes(biomesForGeneration, x * 16, z * 16, 16, 16);
		replaceBiomeBlocks(x, z, chunkprimer, biomesForGeneration);

		caveGenerator.generate(world, x, z, chunkprimer);
		ravineGenerator.generate(world, x, z, chunkprimer);

		Chunk chunk = new Chunk(world, chunkprimer, x, z);
		byte[] chunkBiomeArray = chunk.getBiomeArray();

		for (int i = 0; i < chunkBiomeArray.length; ++i)
			chunkBiomeArray[i] = (byte)Biome.getIdForBiome(biomesForGeneration[i]);

		chunk.generateSkylightMap();

		return chunk;
	}

	@Override
	public void populate(int x, int z) {
		BlockFalling.fallInstantly = true; //This seems to be in all the other providers, so might as well put it here

		world.getBiome(new BlockPos(x * 16 + 16, 0, z * 16 + 16)).decorate(world, rand, new BlockPos(x * 16, 0, z * 16));

		BlockFalling.fallInstantly = false;
	}

	@Override
	public boolean generateStructures(Chunk chunkIn, int x, int z) {
		return false;
	}

	@Override
	public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		return new ArrayList<>();
	}

	@Override
	public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position, boolean shouldDestroyWorld) {
		return null; //Actually, the last parameter appears to be findUnexplored. Not that it matters.
	}

	@Override
	public void recreateStructures(Chunk chunkIn, int x, int z) {}

	public void setBlocksInChunk(int x, int z, ChunkPrimer primer) {
		biomesForGeneration = world.getBiomeProvider().getBiomesForGeneration(biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
		generateHeightmap(x * 4, 0, z * 4);

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int i1 = (i * 5 + j) * 33;
				int j1 = (i * 5 + j + 1) * 33;
				int k1 = ((i + 1) * 5 + j) * 33;
				int l1 = ((i + 1) * 5 + j + 1) * 33;

				for (int k = 0; k < 32; k++) {
					double d1 = heightMap[i1 + k];
					double d2 = heightMap[j1 + k];
					double d3 = heightMap[k1 + k];
					double d4 = heightMap[l1 + k];
					double d5 = (heightMap[i1 + k + 1] - d1) * 0.125;
					double d6 = (heightMap[j1 + k + 1] - d2) * 0.125;
					double d7 = (heightMap[k1 + k + 1] - d3) * 0.125;
					double d8 = (heightMap[l1 + k + 1] - d4) * 0.125;

					for (int l = 0; l < 8; l++) {
						double d10 = d1;
						double d11 = d2;
						double d12 = (d3 - d1) * 0.25;
						double d13 = (d4 - d2) * 0.25;

						for (int m = 0; m < 4; m++) {
							double d16 = (d11 - d10) * 0.25;
							double lvt_45_1_ = d10 - d16;

							for (int n = 0; n < 4; n++) {
								if ((lvt_45_1_ += d16) > 0.0) {
									primer.setBlockState(i * 4 + m, k * 8 + l, j * 4 + n, Blocks.STONE.getDefaultState());
								}
								else if (k * 8 + l < world.getSeaLevel()) {
									primer.setBlockState(i * 4 + m, k * 8 + l, j * 4 + n, Blocks.WATER.getDefaultState());
								}
							}

							d10 += d12;
							d11 += d13;
						}

						d1 += d5;
						d2 += d6;
						d3 += d7;
						d4 += d8;
					}
				}
			}
		}
	}

	public void replaceBiomeBlocks(int x, int z, ChunkPrimer primer, Biome[] biomes) {
		depthBuffer = surfaceNoise.getRegion(depthBuffer, x * 16, z * 16, 16, 16, 0.0625, 0.0625, 1.0);

		for (int i = 0; i < 16; i++)
			for (int j = 0; j < 16; j++)
				biomes[j + i * 16].genTerrainBlocks(world, rand, primer, x * 16 + i, z * 16 + j, depthBuffer[j + i * 16]);
	}

	private void generateHeightmap(int xoffset, int yoffset, int zoffset) {
		depthRegion = depthNoise.generateNoiseOctaves(depthRegion, xoffset, zoffset, 5, 5, 200.0, 200.0, 0.5);

		double coordinateScale = 684.412;
		double heightScale = 684.412;

		mainNoiseRegion = mainPerlinNoise.generateNoiseOctaves(mainNoiseRegion, xoffset, yoffset, zoffset, 5, 33, 5, coordinateScale / 80.0, heightScale / 160, coordinateScale / 80);
		minLimitRegion = minLimitPerlinNoise.generateNoiseOctaves(minLimitRegion, xoffset, yoffset, zoffset, 5, 33, 5, coordinateScale, heightScale, coordinateScale);
		maxLimitRegion = maxLimitPerlinNoise.generateNoiseOctaves(maxLimitRegion, xoffset, yoffset, zoffset, 5, 33, 5, coordinateScale, heightScale, coordinateScale);

		int i = 0;
		int j = 0;

		for (int k = 0; k < 5; k++) {
			for (int l = 0; l < 5; l++) {
				float f2 = 0.0f;
				float f3 = 0.0f;
				float f4 = 0.0f;

				Biome biome = biomesForGeneration[k + 2 + (l + 2) * 10];

				for (int j1 = -2; j1 <= 2; ++j1) {
					for (int k1 = -2; k1 <= 2; ++k1) {
						Biome biome1 = biomesForGeneration[k + j1 + 2 + (l + k1 + 2) * 10];

						float f7 = biomeWeights[j1 + 2 + (k1 + 2) * 5] / (biome1.getBaseHeight() + 2.0f);

						if (biome1.getBaseHeight() > biome.getBaseHeight()) {
							f7 /= 2.0f;
						}

						f2 += biome1.getHeightVariation() * f7;
						f3 += biome1.getBaseHeight() * f7;
						f4 += f7;
					}
				}

				f2 = f2 / f4;
				f3 = f3 / f4;
				f2 = f2 * 0.9f + 0.1f;
				f3 = (f3 * 4.0f - 1.0f) / 8.0f;
				double d7 = depthRegion[j] / 8000.0;

				if (d7 < 0.0){
					d7 = -d7 * 0.3;
				}

				d7 = d7 * 3.0 - 2.0;

				if (d7 < 0.0) {
					d7 = d7 / 2.0;

					if (d7 < -1.0) {
						d7 = -1.0;
					}

					d7 = d7 / 1.4;
					d7 = d7 / 2.0;
				}
				else {
					if (d7 > 1.0) {
						d7 = 1.0;
					}

					d7 = d7 / 8.0;
				}

				j++;

				for (int l1 = 0; l1 < 33; l1++) {
					double d1 = (l1 - (8.5 + (f3 + d7 * 0.2) * 4.25)) * 6.0 / f2;

					if (d1 < 0.0)
						d1 *= 4.0;

					double height = MathHelper.clampedLerp(minLimitRegion[i] / 512.0, maxLimitRegion[i] / 512.0, (mainNoiseRegion[i] / 10.0 + 1.0) / 2.0) - d1;

					if (l1 > 29)
						height = height * (1.0 - (l1 - 29.0) / 3.0) - 10.0 * ((l1 - 29.0) / 3.0);

					heightMap[i] = height;
					i++;
				}
			}
		}
	}
}
