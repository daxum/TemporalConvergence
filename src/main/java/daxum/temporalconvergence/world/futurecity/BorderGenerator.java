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

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class BorderGenerator extends FutureStructureGenerator {

	public BorderGenerator() {
		super(FutureCityGenerator.BORDER_ID);
	}

	@Override
	public ChunkPrimer generateStructure(int[][] cityMap, int dataMap[][], int chunkX, int chunkZ, int groundLevel) {
		return getBasePrimer(groundLevel);
	}

	@Override
	public void placeInMap(int[][] cityMap, int[][] dataMap) {}

	@Override
	public char getSymbol() {
		return 'X';
	}

	@Override
	public void setTiles(int[][] cityMap, int[][] dataMap, int chunkX, int chunkZ, World world, BlockPos startPos, int groundLevel) {}
}
