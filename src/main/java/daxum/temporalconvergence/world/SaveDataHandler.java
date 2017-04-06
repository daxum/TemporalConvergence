package daxum.temporalconvergence.world;

import java.util.HashMap;
import java.util.Map;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.power.PowerDimension;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class SaveDataHandler extends WorldSavedData {
	public static final String DATA_NAME = TemporalConvergence.MODID + "-SaveData";

	private  Map<Integer, PowerDimension> powerDims = new HashMap<>();
	private int nextDimId = 0;

	public SaveDataHandler() {
		super(DATA_NAME);
	}

	public SaveDataHandler(String name) {
		super(name);
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

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("nextdim"))
			nextDimId = comp.getInteger("nextdim");

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

		return comp;
	}

	public static SaveDataHandler get(World world) {
		SaveDataHandler instance = (SaveDataHandler) world.getMapStorage().getOrLoadData(SaveDataHandler.class, DATA_NAME);

		if (instance == null) {
			TemporalConvergence.LOGGER.warn("Couldn't obtain instance of world save data, making a new one...");
			instance = new SaveDataHandler();
			world.getMapStorage().setData(DATA_NAME, instance);
		}

		return instance;
	}
}
