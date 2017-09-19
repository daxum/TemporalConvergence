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

	//Used by machines
	public static class PowerRequirements {
		private final PowerType[] types;
		private final int[] amounts;

		//Objects passed in the form "String, Integer, String, Integer, ..." for all required types
		public PowerRequirements(Object... objects) {
			if (objects.length % 2 != 0) {
				throw new IllegalArgumentException("Invalid initializer for PowerRequirements: length must be even");
			}
			else if (objects.length == 0) {
				//No power requirements - a bit odd, but perfectly valid
				types = new PowerType[] {};
				amounts = new int[] {};
				return;
			}

			types = new PowerType[objects.length / 2];
			amounts = new int[objects.length / 2];

			for (int i = 0; i < objects.length - 1; i += 2) {
				if (objects[i] == null || objects[i + 1] == null) {
					throw new IllegalArgumentException("Cannot initialize PowerRequirements with a null argument");
				}

				if (objects[i] instanceof PowerType && objects[i + 1] instanceof Integer) {
					types[i / 2] = (PowerType) objects[i];
					amounts[i / 2] = (Integer) objects[i + 1];
				}
				else {
					throw new IllegalArgumentException("Argument to PowerRequirements constructor wasn't a PowerType or Integer");
				}
			}
		}

		public PowerType[] getTypesRequired() {
			return types;
		}

		public int[] getAmountsRequired() {
			return amounts;
		}

		public int getAmountForType(PowerType type) {
			for (int i = 0; i < types.length; i++) {
				if (types[i].equals(type)) {
					return amounts[i];
				}
			}

			return 0;
		}

		@Override
		public String toString() {
			String output = "";

			for (int i = 0; i < types.length; i++) {
				output += types[i] + ": " + amounts[i] + " ";
			}

			return output;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}

			if (obj == null || !(obj instanceof PowerRequirements)) {
				return false;
			}

			PowerRequirements other = (PowerRequirements) obj;

			if (other.amounts.length != amounts.length) {
				return false;
			}

			for (int i = 0; i < amounts.length; i++) {
				if (!(amounts[i] == other.amounts[i] && types[i].equals(other.types[i]))) {
					return false;
				}
			}

			return true;
		}
	}
}
