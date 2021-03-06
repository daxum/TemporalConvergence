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

public final class PowerTypeList {
	//Null power type, used when something isn't providing power
	public static final PowerType POWER_0 = new PowerType("null", 0, 0, 0);
	public static final PowerType TIME = new PowerType("time", 70, 193, 219);
	public static final PowerType STABLE = new PowerType("stable", 142, 186, 169);
	public static final PowerType FIRE = new PowerType("fire", 191, 120, 19);
}
