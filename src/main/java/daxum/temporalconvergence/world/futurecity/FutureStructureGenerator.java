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
package daxum.temporalconvergence.world.futurecity;

import java.util.Random;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.world.ChunkProviderEarlyFuture;
import daxum.temporalconvergence.world.futurecity.StructureHandler.StateData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public abstract class FutureStructureGenerator {
	protected final Random rand = new Random();
	private final int id;

	public FutureStructureGenerator(int idnum) {
		id = idnum;
	}

	public abstract ChunkPrimer generateStructure(int[][] cityMap, int[][] dataMap, int chunkX, int chunkZ, int groundLevel);

	public abstract void placeInMap(int[][] cityMap, int[][] dataMap);

	public abstract void setTiles(int[][] cityMap, int[][] dataMap, int chunkX, int chunkZ, World world, BlockPos startPos, int groundLevel);

	//For debug purposes
	public abstract char getSymbol();

	protected void placeInMap(int[][] cityMap, int width, int length, double placementChance) {
		for (int x = 0; x < cityMap.length; x++) {
			for (int z = 0; z < cityMap[x].length; z++) {
				if (cityMap[x][z] == FutureCityGenerator.EMPTY_ID && rand.nextDouble() < placementChance) {

					boolean invalid = false;

					for (int nx = x; nx < x + width; nx++) {
						for (int nz = z; nz < z + length; nz++) {
							if (nx >= cityMap.length || nz >= cityMap[nx].length || cityMap[nx][nz] != FutureCityGenerator.EMPTY_ID) {
								invalid = true;
								break;
							}
						}

						if (invalid) {
							break;
						}
					}

					if (!invalid) {
						for (int nx = x; nx < x + width; nx++) {
							for (int nz = z; nz < z + length; nz++) {
								cityMap[nx][nz] = id;
							}
						}
					}
				}
			}
		}
	}

	protected ChunkPrimer getBasePrimer(int groundLevel) {
		ChunkPrimer primer = new ChunkPrimer();

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = groundLevel; y >= 0; y--) {
					if (y <= rand.nextInt(5)) {
						primer.setBlockState(x, y, z, ChunkProviderEarlyFuture.BASE_BLOCK);
					}
					else if (groundLevel - y < 5) {
						primer.setBlockState(x, y, z, ChunkProviderEarlyFuture.TOP_BLOCK);
					}
					else {
						primer.setBlockState(x, y, z, ChunkProviderEarlyFuture.BOTTOM_BLOCK);
					}
				}
			}
		}

		return primer;
	}

	protected void setTiles(World world, StateData[] dataArray, BlockPos start, Rotation rot, int yOffset) {
		for (StateData data : dataArray) {
			if (data.tileData != null) {
				data.tileData.setInteger("x", data.getX() + start.getX());
				data.tileData.setInteger("y", data.getY() + start.getY() + yOffset);
				data.tileData.setInteger("z", data.getZ() + start.getZ());

				TileEntity te = data.state.getBlock().createTileEntity(world, data.state);
				BlockPos pos = new BlockPos(data.getX() + start.getX(), data.getY() + start.getY() + yOffset, data.getZ() + start.getZ());

				if (te != null) {
					te.rotate(rot);
					world.setTileEntity(pos, te);
					te.readFromNBT(data.tileData);
				}
				else {
					TemporalConvergence.LOGGER.warn("Block at {} (state {}) has no tile entity!?", pos, data.state);
				}
			}
		}
	}

	public int getId() {
		return id;
	}
}
