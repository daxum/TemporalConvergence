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
import net.minecraft.util.Rotation;
import net.minecraft.world.chunk.ChunkPrimer;

public class HouseGenerator extends FutureStructureGenerator {

	public HouseGenerator(int id) {
		super(id);
	}

	@Override
	public ChunkPrimer generateStructure(int[][] cityMap, int chunkX, int chunkZ, int groundLevel) {
		ChunkPrimer primer = getBasePrimer(groundLevel);

		Rotation rot;
		final int ROAD_ID = FutureCityGenerator.ROAD_ID;

		if (cityMap[chunkX][chunkZ - 1] == ROAD_ID) {
			rot = Rotation.CLOCKWISE_180;
		}
		else if (cityMap[chunkX + 1][chunkZ] == ROAD_ID) {
			rot = Rotation.COUNTERCLOCKWISE_90;
		}
		else if (cityMap[chunkX][chunkZ + 1] == ROAD_ID) {
			rot = Rotation.NONE;
		}
		else if (cityMap[chunkX - 1][chunkZ] == ROAD_ID) {
			rot = Rotation.CLOCKWISE_90;
		}
		else {
			return primer;
		}

		StateData[] floorData = StructureHandler.getStructureWithRotation("house1_floor", rot);

		for (StateData sd : floorData) {
			primer.setBlockState(sd.getX(), sd.getY() + groundLevel, sd.getZ(), sd.state);
		}

		StateData[] middleData = StructureHandler.getStructureWithRotation("house1_middle", rot);

		final int height = rand.nextInt(10) + 10;
		for (int i = 0; i < height; i++) {
			for (StateData sd : middleData) {
				primer.setBlockState(sd.getX(), sd.getY() + i * 5 + groundLevel + 6, sd.getZ(), sd.state);
			}
		}

		return primer;
	}

	@Override
	public void placeInMap(int[][] cityMap) {
		for (int x = 0; x < cityMap.length; x++) {
			for (int z = 0; z < cityMap[0].length; z++) {
				if (cityMap[x][z] == FutureCityGenerator.EMPTY_ID) {
					cityMap[x][z] = getId();
				}
			}
		}
	}

	@Override
	public char getSymbol() {
		return 'H';
	}

}
