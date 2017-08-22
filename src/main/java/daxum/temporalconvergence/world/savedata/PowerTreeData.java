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
import daxum.temporalconvergence.power.ProviderTree;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class PowerTreeData extends WorldSavedData {
	public static final String DATA_NAME = TemporalConvergence.MODID + "-powerTreeData";
	private ProviderTree tree = new ProviderTree(this);

	public PowerTreeData() {
		super(DATA_NAME);
	}

	public PowerTreeData(String name) {
		super(name);
	}

	public ProviderTree getTree() {
		return tree;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		tree.deserializeNBT(nbt.getTagList("tree", Constants.NBT.TAG_COMPOUND));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setTag("tree", tree.serializeNBT());
		return nbt;
	}

	public static PowerTreeData get(World world) {
		MapStorage storage = world.getPerWorldStorage();

		PowerTreeData saveData = (PowerTreeData)storage.getOrLoadData(PowerTreeData.class, DATA_NAME);

		if (saveData == null) {
			saveData = new PowerTreeData();
			storage.setData(DATA_NAME, saveData);
		}

		return saveData;
	}
}
