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

import net.minecraft.world.chunk.ChunkPrimer;

public class BossBuildingGenerator extends FutureStructureGenerator {
	private static final int BOSS_BUILDING_WIDTH = 4;
	private static final int BOSS_BUILDING_LENGTH = 4;

	public BossBuildingGenerator(int id) {
		super(id);
	}

	@Override
	public ChunkPrimer generateStructure(int[][] cityMap, int chunkX, int chunkZ, int groundLevel) {
		return getBasePrimer(groundLevel);
	}

	@Override
	public void placeInMap(int[][] cityMap) {
		final int cityWidth = cityMap.length;
		final int cityLength = cityMap[0].length;
		final int MAX_AREA = FutureCityGenerator.MAX_CITY_SIZE * FutureCityGenerator.MAX_CITY_SIZE;
		final int CITY_AREA = cityWidth * cityLength;

		final int startX = rand.nextInt(cityWidth - 1 - BOSS_BUILDING_WIDTH) + 1;
		final int startZ = rand.nextInt(cityLength - 1 - BOSS_BUILDING_LENGTH) + 1;

		for (int x = startX; x < startX + BOSS_BUILDING_WIDTH; x++) {
			for (int z = startZ; z < startZ + BOSS_BUILDING_LENGTH; z++) {
				cityMap[x][z] = getId();
			}
		}
	}

	@Override
	public char getSymbol() {
		return 'B';
	}

}
