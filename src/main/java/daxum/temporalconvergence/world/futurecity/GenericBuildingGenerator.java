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

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

//For testing purposes only
public class GenericBuildingGenerator extends FutureStructureGenerator {
	private final Random rand = new Random();
	private final int length;
	private final int width;
	private final double chance;
	private final char symbol;

	public GenericBuildingGenerator(int id, int buildingWidth, int buildingLength, double generationChance, char sym) {
		super(id);
		length = buildingLength;
		width = buildingWidth;
		chance = generationChance;
		symbol = sym;
	}

	@Override
	public ChunkPrimer generateStructure(int[][] cityMap, int[][] dataMap, int chunkX, int chunkZ, int groundLevel) {
		final int height = groundLevel + rand.nextInt(81) + 9;
		ChunkPrimer primer = getBasePrimer(groundLevel);

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = groundLevel; y <= height && y < 256; y++) {
					primer.setBlockState(x, y, z, Blocks.COBBLESTONE.getDefaultState());
				}
			}
		}

		return primer;
	}

	@Override
	public void placeInMap(int[][] cityMap, int[][] dataMap) {
		if (width == length) {
			placeInMap(cityMap, width, length, chance);
		}
		else {
			placeInMap(cityMap, width, length, chance / 2.0);
			placeInMap(cityMap, length, width, chance / 2.0);
		}
	}

	@Override
	public char getSymbol() {
		return symbol;
	}

	@Override
	public void setTiles(int[][] cityMap, int[][] dataMap, int chunkX, int chunkZ, World world, BlockPos startPos, int groundLevel) {}
}
