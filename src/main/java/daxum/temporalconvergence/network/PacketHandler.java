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
package daxum.temporalconvergence.network;

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketHandler {
	public static final SimpleNetworkWrapper HANDLER = NetworkRegistry.INSTANCE.newSimpleChannel(TemporalConvergence.MODID);

	public static void init() {
		int id = 0;

		HANDLER.registerMessage(PacketFrozenEntity.Handler.class, PacketFrozenEntity.class, id++, Side.CLIENT);
		HANDLER.registerMessage(PacketDodge.Handler.class, PacketDodge.class, id++, Side.SERVER);
		HANDLER.registerMessage(PacketDodgeSuccess.Handler.class, PacketDodgeSuccess.class, id++, Side.CLIENT);
	}
}
