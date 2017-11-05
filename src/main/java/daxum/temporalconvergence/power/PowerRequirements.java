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

public class PowerRequirements {
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