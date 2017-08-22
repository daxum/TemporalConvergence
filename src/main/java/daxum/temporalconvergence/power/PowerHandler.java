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
package daxum.temporalconvergence.power;

import java.util.List;

import daxum.temporalconvergence.power.ProviderTree.ProviderData;
import daxum.temporalconvergence.world.savedata.PowerTreeData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public final class PowerHandler {

	//Request power of type powerType from all providers within range of the given position
	public static int requestPower(World world, BlockPos pos, String powerType, int amount) {
		if (!world.isRemote) {
			ProviderTree tree = PowerTreeData.get(world).getTree();

			return getPower(world, tree.getIntersectingProviders(powerType, ProviderTree.SINGLE_BLOCK_AABB.offset(pos)), powerType, amount);
		}

		return 0;
	}

	//Request power of type powerType from all providers within range of the given bounding box
	public static int requestPower(World world, AxisAlignedBB receiverBox, String powerType, int amount) {
		if (!world.isRemote) {
			ProviderTree tree = PowerTreeData.get(world).getTree();

			return getPower(world, tree.getIntersectingProviders(powerType, receiverBox), powerType, amount);
		}

		return 0;
	}

	//Adds a provider that produces power of type powerType within range to the list of providers for the given world
	public static void addProvider(World world, BlockPos pos, String powerType, AxisAlignedBB range, boolean startActive) {
		if (!world.isRemote) {
			ProviderTree tree = PowerTreeData.get(world).getTree();

			tree.addProvider(pos, powerType, range, startActive);
			PowerTypeManager.addPowerType(powerType);
		}
	}

	//Removes all providers at the given position from the world's provider list
	public static void removeProvider(World world, BlockPos pos) {
		if (!world.isRemote) {
			ProviderTree tree = PowerTreeData.get(world).getTree();

			for (String type : PowerTypeManager.getPowerTypes()) {
				tree.removeProvider(pos, type);
			}
		}
	}

	//Removes all providers at the given position that produce power of type powerType from the world's provider list
	public static void removeProvider(World world, BlockPos pos, String powerType) {
		if (!world.isRemote) {
			ProviderTree tree = PowerTreeData.get(world).getTree();

			tree.removeProvider(pos, powerType);
		}
	}

	//Informs the PowerHandler that the provider at the given position is actively producing power of type powerType (the generator is on)
	public static void activateProvider(World world, BlockPos pos, String powerType) {
		if (!world.isRemote) {
			ProviderTree tree = PowerTreeData.get(world).getTree();

			tree.setActive(pos, powerType, true);
		}
	}

	//Informs the PowerHandler that the provider at the given position is not currently producing power of type powerType (the generator is off)
	public static void deactivateProvider(World world, BlockPos pos, String powerType) {
		if (!world.isRemote) {
			ProviderTree tree = PowerTreeData.get(world).getTree();

			tree.setActive(pos, powerType, false);
		}
	}

	private static int getPower(World world, List<ProviderData> dataList, String powerType, int amount) {
		//Get all providers from dataList, removing invalid ones from the tree
		int providerCount = 0;

		IPowerProvider[] providers = new IPowerProvider[dataList.size()];

		for (int i = 0; i < providers.length; i++) {
			TileEntity tile = world.getTileEntity(dataList.get(i).pos);

			if (tile != null && tile instanceof IPowerProvider) {
				providers[i] = (IPowerProvider) tile;
				providerCount++;
			}
			else {
				removeProvider(world, dataList.get(i).pos, powerType);
			}
		}

		//Get and return power from all providers

		int providedAmount = 0;

		while (providedAmount < amount && providerCount > 0) {
			//Distribute requested amount evenly between providers
			int amountPerProvider = amount / providerCount;
			int extraAmount = amount - amountPerProvider * providerCount;

			for (int i = 0; i < providers.length; i++) {
				if (providers[i] != null) {
					int amountToRequest = amountPerProvider;

					if (extraAmount >= 0) {
						amountToRequest += extraAmount;
						extraAmount = 0;
					}

					int obtainedAmount = 0;

					//If other providers in the area effect this one, adjust the requested power accordingly
					if (providers[i].adjustAmount()) {
						int adjustedAmount = MathHelper.ceil(amountToRequest / dataList.get(i).multiplier);

						obtainedAmount = Math.min(MathHelper.floor(providers[i].getPower(powerType, adjustedAmount) * dataList.get(i).multiplier), amountToRequest);
					}
					else {
						obtainedAmount = providers[i].getPower(powerType, amountToRequest);
					}

					//If a provider isn't providing power, then remove it from the list
					if (obtainedAmount <= 0) {
						providers[i] = null;
						providerCount--;
					}
					else {
						providedAmount += obtainedAmount;
					}
				}
			}
		}

		return providedAmount;
	}
}
