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

import java.util.HashMap;
import java.util.Map;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.power.PowerDimension;
import daxum.temporalconvergence.world.futurecity.FutureCityGenerator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class SaveDataHandler extends WorldSavedData {
	public static final String DATA_NAME = TemporalConvergence.MODID + "-SaveData";

	private  Map<Integer, PowerDimension> powerDims = new HashMap<>();
	private int nextDimId = 0;
	private FutureCityGenerator futureGen = null;

	public SaveDataHandler() {
		super(DATA_NAME);
	}

	public SaveDataHandler(String name) {
		super(name);
	}

	public FutureCityGenerator getFutureCityGenerator() {
		if (futureGen == null) {
			futureGen = new FutureCityGenerator(this);
		}

		return futureGen;
	}

	public PowerDimension getNewPowerDim() {
		if (nextDimId < 0) {
			//This should never ever happen unless someone continuously creates dimensions using over 2000 dimGens for three years straight. Or somehow spends ~1300 years creating power dims with one dimGen.
			TemporalConvergence.LOGGER.error("Next power dimension id is less than zero (" + nextDimId + "). The creation will still work, but it will be unable to be loaded from disk once saved, and therefore erased.");
		}

		powerDims.put(nextDimId, new PowerDimension(this, nextDimId));
		markDirty();

		return powerDims.get(nextDimId++);
	}

	public PowerDimension getExistingPowerDim(int id) {
		return powerDims.get(id);
	}

	public void removePowerDim(int id) {
		powerDims.remove(id);
		markDirty();
	}

	public int getMaxDimId() {
		return nextDimId;
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("nextdim"))
			nextDimId = comp.getInteger("nextdim");

		if (comp.hasKey("futureGen")) {
			FutureCityGenerator cityGen = new FutureCityGenerator(this);
			cityGen.deserializeNBT(comp.getTagList("futureGen", Constants.NBT.TAG_COMPOUND));

			futureGen = cityGen;
		}

		for (int i = 0; i < nextDimId; i++)
			if (comp.hasKey("dim#" + i)) {
				powerDims.put(i, new PowerDimension(this, i));
				powerDims.get(i).deserializeNBT(comp.getCompoundTag("dim#" + i));
			}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setInteger("nextdim", nextDimId);

		for (Map.Entry<Integer, PowerDimension> pde : powerDims.entrySet())
			if (pde.getValue() != null)
				comp.setTag("dim#" + pde.getValue().id, pde.getValue().serializeNBT());

		if (futureGen != null) {
			comp.setTag("futureGen", futureGen.serializeNBT());
		}

		return comp;
	}

	public static SaveDataHandler get(World world) {
		MapStorage store = world.getMapStorage();

		if (store == null) {
			TemporalConvergence.LOGGER.info("MapStorage null on {}", world.isRemote);
		}
		SaveDataHandler instance = (SaveDataHandler) world.getMapStorage().getOrLoadData(SaveDataHandler.class, DATA_NAME);

		if (instance == null) {
			TemporalConvergence.LOGGER.warn("Couldn't obtain instance of world save data, making a new one...");
			instance = new SaveDataHandler();
			world.getMapStorage().setData(DATA_NAME, instance);
		}

		return instance;
	}
}
