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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import daxum.temporalconvergence.TemporalConvergence;

public final class PowerTypeManager {
	private static Map<String, Map<String, Float>> interactionMap = new HashMap<>(); //Outer map = effected, inner map = effecting
	private static Set<String> powerTypes = new HashSet<>();
	private static boolean hasWarned = false;

	public static void addInteraction(String effecting, String effected, float multiplier) {
		if (multiplier < 0.0f) {
			TemporalConvergence.LOGGER.error("Invalid multiplier {} for type interaction ({} -> {})", multiplier, effecting, effected);
			return;
		}

		if (interactionMap.get(effected) == null) {
			interactionMap.put(effected, new HashMap<String, Float>());
		}

		interactionMap.get(effected).put(effecting, multiplier);
		addPowerType(effecting);
		addPowerType(effected);
	}

	public static float getMultiplier(String effecting, String effected) {
		if (interactionMap.get(effected) != null && interactionMap.get(effected).get(effecting) != null) {
			return interactionMap.get(effected).get(effecting);
		}

		return 1.0f;
	}

	public static String[] getEffectingTypes(String effected) {
		if (interactionMap.get(effected) != null) {
			return interactionMap.get(effected).keySet().toArray(new String[0]);
		}

		return null;
	}

	public static String[] getEffectedTypes(String effecting) {
		List<String> types = new ArrayList<>();

		for (String key : interactionMap.keySet()) {
			if (interactionMap.get(key) != null && interactionMap.get(key).containsKey(effecting)) {
				types.add(key);
			}
		}

		return types.toArray(new String[0]);
	}

	public static void addPowerType(String type) {
		if (powerTypes.add(type)) {
			if (powerTypes.size() > 100 && !hasWarned) {
				TemporalConvergence.LOGGER.warn("Powertype list contains over 100 entries, which may cause performance issues");
				hasWarned = true;
			}
		}
	}

	public static String[] getPowerTypes() {
		return powerTypes.toArray(new String[0]);
	}

	//Used by machines
	public static class PowerRequirements {
		private final String[] types;
		private final int[] amounts;

		//Objects passed in the form "String, Integer, String, Integer, ..." for all required types
		public PowerRequirements(Object... objects) {
			if (objects.length % 2 != 0) {
				throw new IllegalArgumentException("Invalid initializer for PowerRequirements: length must be even");
			}
			else if (objects.length == 0) {
				//No power requirements - a bit odd, but perfectly valid
				types = new String[] {};
				amounts = new int[] {};
				return;
			}

			types = new String[objects.length / 2];
			amounts = new int[objects.length / 2];

			for (int i = 0; i < objects.length - 1; i += 2) {
				if (objects[i] == null || objects[i + 1] == null) {
					throw new IllegalArgumentException("Cannot initialize PowerRequirements with a null argument");
				}

				if (objects[i] instanceof String && objects[i + 1] instanceof Integer) {
					types[i / 2] = (String) objects[i];
					amounts[i / 2] = (Integer) objects[i];
				}
				else {
					TemporalConvergence.LOGGER.warn("Skipping argument {} of PowerRequirements initializer: wasn't a String or Integer", i);
				}
			}
		}

		public String[] getTypesRequired() {
			return types;
		}

		public int getAmountForType(String type) {
			for (int i = 0; i < types.length; i++) {
				if (types[i].equals(type)) {
					return amounts[i];
				}
			}

			return 0;
		}
	}
}
