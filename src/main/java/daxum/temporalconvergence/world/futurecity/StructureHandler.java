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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.IOUtils;

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Rotation;
import net.minecraftforge.common.util.Constants;

public class StructureHandler {
	private static final Map<String, StateData[]> structureNameMap = new HashMap<>();

	public static StateData[] getStructure(String name) {
		StateData[] data = structureNameMap.get(name);

		if (data == null) {
			data = readStructureFromFile("/assets/temporalconvergence/city_structures/" + name + ".nbt");
			structureNameMap.put(name, data);
		}

		return data;
	}

	public static StateData[] rotateStructure(StateData[] structure, Rotation rot) {
		StateData[] newStruct;

		switch(rot) {
		case CLOCKWISE_180:
			newStruct = new StateData[structure.length];

			for (int i = 0; i < structure.length; i++) {
				StateData current = structure[i];
				long newPos = compressPos(current.sizeX() - current.getX() - 1, current.getY(), current.sizeZ() - current.getZ() - 1);

				newStruct[i] = new StateData(newPos, current.size, current.state.withRotation(rot));
			}

			return newStruct;
		case CLOCKWISE_90:
			newStruct = new StateData[structure.length];

			for (int i = 0; i < structure.length; i++) {
				StateData current = structure[i];
				long newPos = compressPos(current.sizeZ() - current.getZ() - 1, current.getY(), current.getX());
				int newSize = (current.sizeX() - 1 & 15) << 4 | current.sizeZ() - 1;

				newStruct[i] = new StateData(newPos, newSize, current.state.withRotation(rot));
			}

			return newStruct;
		case COUNTERCLOCKWISE_90:
			newStruct = new StateData[structure.length];

			for (int i = 0; i < structure.length; i++) {
				StateData current = structure[i];
				long newPos = compressPos(current.getZ(), current.getY(), current.sizeX() - current.getX() - 1);
				int newSize = (current.sizeX() - 1 & 15) << 4 | current.sizeZ() - 1;

				newStruct[i] = new StateData(newPos, newSize, current.state.withRotation(rot));
			}

			return newStruct;
		case NONE:
		default: return structure;

		}
	}

	public static long compressPos(int x, int y, int z) {
		return (long)(x & 15) << 32 | (long)(z & 15) << 36 | y;
	}

	public static class StateData {
		private final long pos;
		public final int size;
		public final IBlockState state;

		public StateData(long position, int compressedSize, IBlockState blockState) {
			pos = position;
			state = blockState;
			size = compressedSize;
		}

		public int getX() {
			return (int) (pos >> 32 & 15);
		}

		public int getY() {
			return (int) (pos & 0xFFFFFFFF);
		}

		public int getZ() {
			return (int) (pos >> 36 & 15);
		}

		public int sizeX() {
			return (size & 15) + 1;
		}

		public int sizeZ() {
			return (size >> 4 & 15) + 1;
		}
	}

	private static StateData[] readStructureFromFile(String fileName) {
		InputStream inputStream = TemporalConvergence.class.getResourceAsStream(fileName);
		NBTTagCompound saveData = null;

		try {
			saveData = CompressedStreamTools.readCompressed(inputStream);
		} catch (IOException e) {
			TemporalConvergence.LOGGER.error("Error reading structure save data from {}", fileName);
		} catch (NullPointerException e) {
			TemporalConvergence.LOGGER.error("File {} does not exist", fileName);
			return null;
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}

		if (saveData == null) {
			TemporalConvergence.LOGGER.error("Error reading structure data from {}", fileName);
			return null;
		}

		Map<Integer, IBlockState> stateMap = new HashMap<>();

		if (saveData.hasKey("size", Constants.NBT.TAG_BYTE)) {
			final int size = saveData.getByte("size");

			if (saveData.hasKey("map", Constants.NBT.TAG_LIST)) {
				NBTTagList idList = saveData.getTagList("map", Constants.NBT.TAG_COMPOUND);

				for (int i = 0; i < idList.tagCount(); i++) {
					stateMap.put(i, NBTUtil.readBlockState(idList.getCompoundTagAt(i)));
				}

				if (saveData.hasKey("data", Constants.NBT.TAG_LIST)) {
					NBTTagList statePos = saveData.getTagList("data", Constants.NBT.TAG_COMPOUND);

					List<StateData> stateList = new ArrayList<>();

					for (int i = 0; i < statePos.tagCount(); i++) {
						NBTTagCompound comp = statePos.getCompoundTagAt(i);

						if (comp.hasKey("pos", Constants.NBT.TAG_LONG) && comp.hasKey("state", Constants.NBT.TAG_INT)) {
							stateList.add(new StateData(comp.getLong("pos"), size, stateMap.get(comp.getInteger("state"))));
						}
					}

					return stateList.toArray(new StateData[0]);
				}
			}
		}

		TemporalConvergence.LOGGER.error("Failed to read structure save data for {}", fileName);
		return null;
	}
}
