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

import java.util.UUID;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.item.ItemPhaseClothChest;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketDodgeSuccess implements IMessage {
	private UUID mainPlayerUUID;

	public PacketDodgeSuccess setUUID(UUID uuid) {
		mainPlayerUUID = uuid;

		return this;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		long uuidMost = buf.readLong();
		long uuidLeast = buf.readLong();

		mainPlayerUUID = new UUID(uuidMost, uuidLeast);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(mainPlayerUUID.getMostSignificantBits());
		buf.writeLong(mainPlayerUUID.getLeastSignificantBits());
	}

	public static class Handler implements IMessageHandler<PacketDodgeSuccess, IMessage> {

		@Override
		public IMessage onMessage(PacketDodgeSuccess message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				Minecraft.getMinecraft().addScheduledTask(() -> handle(message));
			}
			else {
				TemporalConvergence.LOGGER.error("Client packet (PacketDodgeSuccess) recieved on server?!");
			}

			return null;
		}

		public void handle(PacketDodgeSuccess message) {
			EntityPlayer player = TemporalConvergence.proxy.getClientPlayer();

			if (player.getPersistentID().equals(message.mainPlayerUUID)) {
				ItemPhaseClothChest.onSuccessfulDodge(player);
			}

			//TODO: Particles, etc.
		}
	}
}
