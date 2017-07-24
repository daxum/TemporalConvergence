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

import daxum.temporalconvergence.world.futurecity.StructureHandler.StateData;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.world.chunk.ChunkPrimer;

public class RoadGenerator extends FutureStructureGenerator {
	private static final int EMPTY_ID = FutureCityGenerator.EMPTY_ID;

	public RoadGenerator() {
		super(FutureCityGenerator.ROAD_ID);
	}

	@Override
	public ChunkPrimer generateStructure(int[][] cityMap, int chunkX, int chunkZ, int groundLevel) {
		boolean roadNorth = cityMap[chunkX][chunkZ - 1] == getId();
		boolean roadEast = cityMap[chunkX + 1][chunkZ] == getId();
		boolean roadSouth = cityMap[chunkX][chunkZ + 1] == getId();
		boolean roadWest = cityMap[chunkX - 1][chunkZ] == getId();

		StateData[] data = getDataForConnections(roadNorth, roadEast, roadSouth, roadWest);

		if (data == null || data.length == 0) {
			return getBasePrimer(groundLevel);
		}

		data = getRotationForConnections(data, roadNorth, roadEast, roadSouth, roadWest);

		ChunkPrimer primer = getBasePrimer(groundLevel);

		for (StateData sd : data) {
			primer.setBlockState(sd.getX(), sd.getY() + groundLevel - 3, sd.getZ(), sd.state);
		}

		return primer;
	}

	private StateData[] getDataForConnections(boolean north, boolean east, boolean south, boolean west) {
		int amount = 0;

		if (north) {
			amount++;
		}

		if (east) {
			amount++;
		}

		if (south) {
			amount++;
		}

		if (west) {
			amount++;
		}

		switch (amount) {
		case 0: return null;
		case 1: return StructureHandler.getStructure("road_end");
		case 2: return north && south || east && west ? StructureHandler.getStructure("road_straight") : StructureHandler.getStructure("road_curve");
		case 3: return StructureHandler.getStructure("road_three_intersect");
		case 4: return StructureHandler.getStructure("road_four_intersect");
		default: return null;
		}
	}

	private StateData[] getRotationForConnections(StateData[] data, boolean north, boolean east, boolean south, boolean west) {
		int amount = 0;

		if (north) {
			amount++;
		}

		if (east) {
			amount++;
		}

		if (south) {
			amount++;
		}

		if (west) {
			amount++;
		}

		switch (amount) {
		case 0: return data;
		case 1: return StructureHandler.rotateStructure(data, getOneRotation(north, east, south, west));
		case 2:
			if (north && south || east && west) {
				return north && south ? data : StructureHandler.rotateStructure(data, Rotation.CLOCKWISE_90);
			}
			else {
				return StructureHandler.rotateStructure(data, getCornerRotation(north, east, south, west));
			}
		case 3: return StructureHandler.rotateStructure(data, getThreeRotation(north, east, south, west));
		case 4: return data;
		default: return data;
		}
	}

	private Rotation getOneRotation(boolean north, boolean east, boolean south, boolean west) {
		if (north) {
			return Rotation.NONE;
		}

		if (east) {
			return Rotation.CLOCKWISE_90;
		}

		if (south) {
			return Rotation.CLOCKWISE_180;
		}

		if (west) {
			return Rotation.COUNTERCLOCKWISE_90;
		}

		return Rotation.NONE;
	}

	private Rotation getCornerRotation(boolean north, boolean east, boolean south, boolean west) {
		if (north && west) {
			return Rotation.NONE;
		}

		if (north && east) {
			return Rotation.CLOCKWISE_90;
		}

		if (east && south) {
			return Rotation.CLOCKWISE_180;
		}

		if (south && west) {
			return Rotation.COUNTERCLOCKWISE_90;
		}

		return Rotation.NONE;
	}

	private Rotation getThreeRotation(boolean north, boolean east, boolean south, boolean west) {
		if (!north) {
			return Rotation.CLOCKWISE_180;
		}

		if (!east) {
			return Rotation.COUNTERCLOCKWISE_90;
		}

		if (!south) {
			return Rotation.NONE;
		}

		if (!west) {
			return Rotation.CLOCKWISE_90;
		}

		return Rotation.NONE;
	}

	@Override
	public void placeInMap(int[][] cityMap) {
		final int roadStartX = rand.nextInt(cityMap.length - 2) + 1;
		final int roadStartZ = rand.nextInt(cityMap[0].length - 2) + 1;

		cityMap[roadStartX][roadStartZ] = getId();
		placeRoads(cityMap, roadStartX, roadStartZ, EnumFacing.NORTH, 0);
		placeRoads(cityMap, roadStartX, roadStartZ, EnumFacing.EAST, 0);
		placeRoads(cityMap, roadStartX, roadStartZ, EnumFacing.SOUTH, 0);
		placeRoads(cityMap, roadStartX, roadStartZ, EnumFacing.WEST, 0);
	}

	@Override
	public char getSymbol() {
		return '+';
	}

	private void placeRoads(int[][] cityMap, int startX, int startZ, EnumFacing facing, int depth) {
		final double ROAD_PLACEMENT_CHANCE = 0.7;
		final int cityWidth = cityMap.length;
		final int cityLength = cityMap[0].length;
		final int MIN_DEPTH = cityLength * cityWidth / 100;
		boolean hasPlaced = false;

		if (facing == EnumFacing.NORTH) {
			if (startZ > 1 && cityMap[startX][startZ - 1] == EMPTY_ID) {
				cityMap[startX][startZ - 1] = getId();

				if (startZ > 2 && cityMap[startX][startZ - 2] == EMPTY_ID) {
					cityMap[startX][startZ - 2] = getId();

					if (rand.nextDouble() < ROAD_PLACEMENT_CHANCE) {
						placeRoads(cityMap, startX, startZ - 2, EnumFacing.EAST, depth + 1);
						hasPlaced = true;
					}
					if (rand.nextDouble() < ROAD_PLACEMENT_CHANCE) {
						placeRoads(cityMap, startX, startZ - 2, EnumFacing.WEST, depth + 1);
						hasPlaced = true;
					}
					if (rand.nextDouble() < ROAD_PLACEMENT_CHANCE || !hasPlaced && depth < MIN_DEPTH) {
						placeRoads(cityMap, startX, startZ - 2, EnumFacing.NORTH, depth + 1);
					}
				}
			}
		}
		else if (facing == EnumFacing.EAST) {
			if (startX < cityWidth - 2 && cityMap[startX + 1][startZ] == EMPTY_ID) {
				cityMap[startX + 1][startZ] = getId();

				if (startX < cityWidth - 3 && cityMap[startX + 2][startZ] == EMPTY_ID) {
					cityMap[startX + 2][startZ] = getId();

					if (rand.nextDouble() < ROAD_PLACEMENT_CHANCE) {
						placeRoads(cityMap, startX + 2, startZ, EnumFacing.SOUTH, depth + 1);
						hasPlaced = true;
					}
					if (rand.nextDouble() < ROAD_PLACEMENT_CHANCE) {
						placeRoads(cityMap, startX + 2, startZ, EnumFacing.NORTH, depth + 1);
						hasPlaced = true;
					}
					if (rand.nextDouble() < ROAD_PLACEMENT_CHANCE || !hasPlaced && depth < MIN_DEPTH) {
						placeRoads(cityMap, startX + 2, startZ, EnumFacing.EAST, depth + 1);
					}
				}
			}
		}
		else if (facing == EnumFacing.SOUTH) {
			if (startZ < cityLength - 2 && cityMap[startX][startZ + 1] == EMPTY_ID) {
				cityMap[startX][startZ + 1] = getId();

				if (startZ < cityLength - 3 && cityMap[startX][startZ + 2] == EMPTY_ID) {
					cityMap[startX][startZ + 2] = getId();

					if (rand.nextDouble() < ROAD_PLACEMENT_CHANCE) {
						placeRoads(cityMap, startX, startZ + 2, EnumFacing.EAST, depth + 1);
						hasPlaced = true;
					}
					if (rand.nextDouble() < ROAD_PLACEMENT_CHANCE) {
						placeRoads(cityMap, startX, startZ + 2, EnumFacing.WEST, depth + 1);
						hasPlaced = true;
					}
					if (rand.nextDouble() < ROAD_PLACEMENT_CHANCE || !hasPlaced && depth < MIN_DEPTH) {
						placeRoads(cityMap, startX, startZ + 2, EnumFacing.SOUTH, depth + 1);
					}
				}
			}
		}
		else if (facing == EnumFacing.WEST) {
			if (startX > 1 && cityMap[startX - 1][startZ] == EMPTY_ID) {
				cityMap[startX - 1][startZ] = getId();

				if (startX > 2 && cityMap[startX - 2][startZ] == EMPTY_ID) {
					cityMap[startX - 2][startZ] = getId();

					if (rand.nextDouble() < ROAD_PLACEMENT_CHANCE) {
						placeRoads(cityMap, startX - 2, startZ, EnumFacing.SOUTH, depth + 1);
						hasPlaced = true;
					}
					if (rand.nextDouble() < ROAD_PLACEMENT_CHANCE) {
						placeRoads(cityMap, startX - 2, startZ, EnumFacing.NORTH, depth + 1);
						hasPlaced = true;
					}
					if (rand.nextDouble() < ROAD_PLACEMENT_CHANCE || !hasPlaced && depth < MIN_DEPTH) {
						placeRoads(cityMap, startX - 2, startZ, EnumFacing.WEST, depth + 1);
					}
				}
			}
		}
	}
}
