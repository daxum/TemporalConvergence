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
package daxum.temporalconvergence.world.savedata;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.world.futurecity.FutureCityGenerator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class CitySaveData extends WorldSavedData {
	public static final String DATA_NAME = TemporalConvergence.MODID + "-cityData";
	private FutureCityGenerator futureGen = null;

	public CitySaveData() {
		super(DATA_NAME);
	}

	public CitySaveData(String name) {
		super(name);
	}

	public FutureCityGenerator getFutureCityGenerator() {
		if (futureGen == null) {
			futureGen = new FutureCityGenerator(this);
		}

		return futureGen;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("futureGen")) {
			FutureCityGenerator cityGen = new FutureCityGenerator(this);
			cityGen.deserializeNBT(nbt.getTagList("futureGen", Constants.NBT.TAG_COMPOUND));

			futureGen = cityGen;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if (futureGen != null) {
			nbt.setTag("futureGen", futureGen.serializeNBT());
		}

		return nbt;
	}

	public static CitySaveData get(World world) {
		MapStorage storage = world.getPerWorldStorage();

		CitySaveData saveData = (CitySaveData)storage.getOrLoadData(CitySaveData.class, DATA_NAME);

		if (saveData == null) {
			saveData = new CitySaveData();
			storage.setData(DATA_NAME, saveData);
		}

		return saveData;
	}
}
