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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import daxum.temporalconvergence.TemporalConvergence;

public final class PowerTypeManager {
	private static Map<PowerType, Map<PowerType, Float>> interactionMap = new HashMap<>(); //Outer map = effected, inner map = effecting
	private static Map<String, PowerType> powerTypes = new HashMap<>();

	public static void addInteraction(PowerType effecting, PowerType effected, float multiplier) {
		if (multiplier < 0.0f) {
			TemporalConvergence.LOGGER.error("Invalid multiplier {} for type interaction ({} -> {})", multiplier, effecting, effected);
			return;
		}

		if (interactionMap.get(effected) == null) {
			interactionMap.put(effected, new HashMap<PowerType, Float>());
		}

		interactionMap.get(effected).put(effecting, multiplier);
	}

	public static float getMultiplier(PowerType effecting, PowerType effected) {
		if (interactionMap.get(effected) != null && interactionMap.get(effected).get(effecting) != null) {
			return interactionMap.get(effected).get(effecting);
		}

		return 1.0f;
	}

	public static PowerType[] getEffectingTypes(PowerType effected) {
		if (interactionMap.get(effected) != null) {
			return interactionMap.get(effected).keySet().toArray(new PowerType[0]);
		}

		return null;
	}

	public static PowerType[] getEffectedTypes(PowerType effecting) {
		List<PowerType> types = new ArrayList<>();

		for (PowerType key : interactionMap.keySet()) {
			if (interactionMap.get(key) != null && interactionMap.get(key).containsKey(effecting)) {
				types.add(key);
			}
		}

		return types.toArray(new PowerType[0]);
	}

	public static boolean addPowerType(PowerType type) {
		if (type == null || powerTypes.containsKey(type.getName())) {
			return false;
		}

		powerTypes.put(type.getName(), type);

		return true;
	}

	public static PowerType getType(String name) {
		return powerTypes.get(name);
	}
}
