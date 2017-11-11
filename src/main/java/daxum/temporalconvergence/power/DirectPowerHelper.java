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

import daxum.temporalconvergence.util.WorldHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DirectPowerHelper {
	public static void signalProviderAdd(World world, BlockPos pos, int range) {
		BlockPos minPos = pos.add(-range, -range, -range);
		BlockPos maxPos = pos.add(range, range, range);

		List<IDirectPowerReceiver> receivers = WorldHelper.getAllInRange(world, minPos, maxPos, IDirectPowerReceiver.class);

		for (int i = 0; i < receivers.size(); i++) {
			receivers.get(i).providerAdded(pos);
		}
	}

	public static void signalProviderRemove(World world, BlockPos pos, int range) {
		BlockPos minPos = pos.add(-range, -range, -range);
		BlockPos maxPos = pos.add(range, range, range);

		List<IDirectPowerReceiver> receivers = WorldHelper.getAllInRange(world, minPos, maxPos, IDirectPowerReceiver.class);

		for (int i = 0; i < receivers.size(); i++) {
			receivers.get(i).providerRemoved(pos);
		}
	}

	public static int getPowerDistributed(List<IDirectPowerProvider> providers, PowerType type, int amount) {
		int providedAmount = 0;

		while (providedAmount < amount && providers.size() > 0) {
			int amountPerProvider = amount / providers.size();
			int extraAmount = amount - amountPerProvider * providers.size();

			for (int i = providers.size() - 1; i >= 0; i--) {
				int amountToRequest = amountPerProvider;

				if (extraAmount >= 0) {
					amountToRequest += extraAmount;
					extraAmount = 0;
				}

				int obtainedAmount = providers.get(i).getPower(type, amountToRequest);

				if (obtainedAmount <= 0) {
					providers.remove(i);
				}
				else {
					providedAmount += obtainedAmount;
				}
			}
		}

		return providedAmount;
	}
}
