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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import daxum.temporalconvergence.world.DimensionHandler;
import daxum.temporalconvergence.world.SaveDataHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

public class FutureCityGenerator implements INBTSerializable<NBTTagList> {
	public static final int MAX_CITY_SIZE = 32;
	private static final int MIN_CITY_SIZE = 16;

	public static final int EMPTY_ID = 0;
	public static final int BORDER_ID = -1;
	public static final int ROAD_ID = -2;

	private static final List<FutureStructureGenerator> GENERATORS = new ArrayList<>();
	private static final RoadGenerator ROAD_GENERATOR = new RoadGenerator();
	private static final BorderGenerator BORDER_GENERATOR = new BorderGenerator();

	private final Random rand = new Random();
	private final Map<Long, CityData> cityMap = new HashMap<>();
	private final SaveDataHandler saveHandler;

	public FutureCityGenerator(SaveDataHandler sdh) {
		saveHandler = sdh;
	}

	public boolean isChunkCity(int x, int z) {
		return getDataForSection(getSectionForChunk(x, z)).containsChunk(x, z);
	}

	public ChunkPrimer getPrimerForChunk(int x, int z) {
		return getDataForSection(getSectionForChunk(x, z)).getPrimerForChunk(x, z);
	}

	public BlockPos getCityLocationForSection(long section) {
		CityData data = getDataForSection(section);

		return new BlockPos(data.startChunkX * 16, data.groundLevel, data.startChunkZ * 16);
	}

	private CityData getDataForSection(long section) {
		CityData data = cityMap.get(section);

		if (data == null) {
			int x = (int) (section >> 32) * 256;
			int z = (int) (section & 0xFFFFFFFFL) * 256;
			int width = rand.nextInt(MAX_CITY_SIZE - MIN_CITY_SIZE + 1) + MIN_CITY_SIZE;
			int length = rand.nextInt(MAX_CITY_SIZE - MIN_CITY_SIZE + 1) + MIN_CITY_SIZE;

			data = new CityData(rand.nextInt(128 - width) + x, rand.nextInt(128 - length) + z, width, length, 66, 4);
			cityMap.put(section, data);
			saveHandler.markDirty();
		}

		return data;
	}

	public static long getSectionForChunk(long x, long z) {
		return  x / 256L << 32 | z / 256L & 0xFFFFFFFFL;
		//To unpack:
		//	int startChunkX = (int) (section >> 32) * 256;
		//	int startChunkZ = (int) (section & 0xFFFFFFFFL) * 256;
	}

	private static FutureStructureGenerator getGeneratorById(int id) {
		if (id == BORDER_ID) {
			return BORDER_GENERATOR;
		}
		else if (id == ROAD_ID) {
			return ROAD_GENERATOR;
		}
		else {
			return GENERATORS.get(id);
		}
	}

	private class CityData implements INBTSerializable<NBTTagCompound> {
		private int startChunkX;
		private int startChunkZ;
		private int groundLevel;
		private int cityWidth;
		private int cityLength;
		private int[][] cityIdMap;

		public CityData() {}

		public CityData(int startX, int startZ, int width, int length, int averageGroundLevel, int maxGroundVariation) {
			startChunkX = startX;
			startChunkZ = startZ;
			cityWidth = width;
			cityLength = length;
			groundLevel = rand.nextInt(maxGroundVariation * 2) - maxGroundVariation + averageGroundLevel;
			cityIdMap = new int[cityWidth][cityLength];

			generateCity();
		}

		public boolean containsChunk(int x, int z) {
			return x >= startChunkX && x < startChunkX + cityWidth && z >= startChunkZ && z < startChunkZ + cityLength;
		}

		public ChunkPrimer getPrimerForChunk(int chunkX, int chunkZ) {
			int mapX = Math.abs(chunkX - startChunkX);
			int mapZ = Math.abs(chunkZ - startChunkZ);

			return getGeneratorById(cityIdMap[mapX][mapZ]).generateStructure(cityIdMap, mapX, mapZ, groundLevel);
		}

		private void generateCity() {
			final int MAX_AREA = MAX_CITY_SIZE * MAX_CITY_SIZE;
			final int CITY_AREA = cityWidth * cityLength;

			//Fill in the border

			for (int x = 0; x < cityWidth; x++) {
				for (int z = 0; z < cityLength; z++) {
					if (x == 0 || x == cityWidth - 1 || z == 0 || z == cityLength - 1) {
						cityIdMap[x][z] = BORDER_ID;
					}
				}
			}

			//Generate roads

			ROAD_GENERATOR.placeInMap(cityIdMap);

			//If city too small, start over

			int chunksUsed = 0;

			for (int i = 1; i < cityWidth - 1; i++) {
				for (int j = 1; j < cityLength - 1; j++) {
					if (cityIdMap[i][j] != EMPTY_ID) {
						chunksUsed++;
					}
				}
			}

			if (chunksUsed / ((cityWidth - 2.0) * (cityLength - 2.0)) < 0.65) {
				for (int i = 0; i < cityWidth; i++) {
					for (int j = 0; j < cityLength; j++) {
						cityIdMap[i][j] = EMPTY_ID;
					}
				}

				generateCity();
				return;
			}

			//Generate other buildings

			for (FutureStructureGenerator gen : GENERATORS) {
				gen.placeInMap(cityIdMap);
			}

			saveHandler.markDirty();
		}

		@Override
		public String toString() {
			String out = "";

			out += "City of width " + cityWidth + " and length " + cityLength + ":\n";

			for (int i = 0; i < cityWidth; i++) {
				for (int j = 0; j < cityLength; j++) {
					out += getGeneratorById(cityIdMap[i][j]).getSymbol();
				}
				out += "\n";
			}

			return out;
		}

		private static final String START_X_TAG = "startX";
		private static final String START_Z_TAG = "startZ";
		private static final String WIDTH_TAG = "width";
		private static final String LENGTH_TAG = "length";
		private static final String GROUND_TAG = "ground";
		private static final String MAP_TAG = "city";

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound comp = new NBTTagCompound();

			comp.setInteger(START_X_TAG, startChunkX);
			comp.setInteger(START_Z_TAG, startChunkZ);
			comp.setInteger(WIDTH_TAG, cityWidth);
			comp.setInteger(LENGTH_TAG, cityLength);
			comp.setInteger(GROUND_TAG, groundLevel);

			int[] flattenedMap = new int[cityWidth * cityLength];

			for (int i = 0; i < cityWidth; i++) {
				for (int j = 0; j < cityLength; j++) {
					flattenedMap[i * cityLength + j] = cityIdMap[i][j];
				}
			}

			comp.setIntArray(MAP_TAG, flattenedMap);

			return comp;
		}

		@Override
		public void deserializeNBT(NBTTagCompound comp) {
			if (comp.hasKey(START_X_TAG, Constants.NBT.TAG_INT)) {
				startChunkX = comp.getInteger(START_X_TAG);
			}

			if (comp.hasKey(START_Z_TAG, Constants.NBT.TAG_INT)) {
				startChunkZ = comp.getInteger(START_Z_TAG);
			}

			if (comp.hasKey(WIDTH_TAG, Constants.NBT.TAG_INT)) {
				cityWidth = comp.getInteger(WIDTH_TAG);
			}

			if (comp.hasKey(LENGTH_TAG, Constants.NBT.TAG_INT)) {
				cityLength = comp.getInteger(LENGTH_TAG);
			}

			if (comp.hasKey(GROUND_TAG, Constants.NBT.TAG_INT)) {
				groundLevel = comp.getInteger(GROUND_TAG);
			}

			if (comp.hasKey(MAP_TAG, Constants.NBT.TAG_INT_ARRAY)) {
				int[] flatMap = comp.getIntArray(MAP_TAG);
				cityIdMap = new int[cityWidth][cityLength];

				for (int i = 0; i < cityWidth; i++) {
					for (int j = 0; j < cityLength; j++) {
						cityIdMap[i][j] = flatMap[i * cityLength + j];
					}
				}
			}
		}
	}

	static {
		int id = 1;

		GENERATORS.add(new EmptyStructureGenerator());
		GENERATORS.add(new BossBuildingGenerator(id++));
		GENERATORS.add(new HouseGenerator(id++));
	}

	@Override
	public NBTTagList serializeNBT() {
		NBTTagList mapList = new NBTTagList();

		for (Map.Entry<Long, CityData> e : cityMap.entrySet()) {
			NBTTagCompound comp = new NBTTagCompound();

			comp.setLong("key", e.getKey());
			comp.setTag("data", e.getValue().serializeNBT());

			mapList.appendTag(comp);
		}

		return mapList;
	}

	@Override
	public void deserializeNBT(NBTTagList saveList) {
		for (NBTBase base : saveList) {
			if (base instanceof NBTTagCompound) {
				NBTTagCompound comp = (NBTTagCompound) base;

				if (comp.hasKey("key", Constants.NBT.TAG_LONG) && comp.hasKey("data", Constants.NBT.TAG_COMPOUND)) {
					CityData data = new CityData();
					data.deserializeNBT(comp.getCompoundTag("data"));

					cityMap.put(comp.getLong("key"), data);
				}
			}
		}
	}

	public static FutureCityGenerator getGenerator(World world) {
		if (world.provider.getDimensionType() == DimensionHandler.EARLY_FUTURE) {
			SaveDataHandler sdh = SaveDataHandler.get(world);
			return sdh.getFutureCityGenerator();
		}

		return null;
	}
}
