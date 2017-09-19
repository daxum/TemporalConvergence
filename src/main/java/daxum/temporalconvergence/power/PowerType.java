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

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraft.util.math.MathHelper;

public final class PowerType {
	private final String name;
	private final int color;

	public PowerType(String typeName, int red, int green, int blue) {
		if (typeName == null) {
			throw new IllegalArgumentException("Name cannot be null!");
		}

		name = typeName;

		if (!TemporalConvergence.proxy.isDedicatedServer()) {
			color = MathHelper.rgb(red, green, blue);
		}
		else {
			color = 0;
		}

		if (!PowerTypeManager.addPowerType(this)) {
			TemporalConvergence.LOGGER.warn("Created duplicate power type with name \"{}\"", name);
		}
	}

	public String getName() {
		return name;
	}

	public int getColor() {
		return color;
	}

	public int getRed() {
		return color >> 16 & 255;
	}

	public int getGreen() {
		return color >> 8 & 255;
	}

	public int getBlue() {
		return color & 255;
	}

	public String getUnlocalizedName() {
		return "temporalconvergence.powertype." + name;
	}

	@Override
	public boolean equals(Object other) {
		return other != null && other instanceof PowerType && ((PowerType)other).getName().equals(name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}
}
