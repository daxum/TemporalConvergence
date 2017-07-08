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

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.entity.EntityFrozen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketFrozenEntity implements IMessage {
	private static final String FROZEN_TAG = "frozenEntity";
	private static final String UUID_TAG = "frozenUUID";
	private static final String UUID_MOST_TAG = UUID_TAG + "Most";
	private static final String UUID_LEAST_TAG = UUID_TAG + "Least";
	private NBTTagCompound packetComp = null;

	public PacketFrozenEntity setTag(UUID frozenUUID, NBTTagCompound entityComp) {
		packetComp = new NBTTagCompound();
		packetComp.setUniqueId(UUID_TAG, frozenUUID);
		packetComp.setTag(FROZEN_TAG, entityComp);

		return this;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);

		try {
			packetComp = buffer.readCompoundTag();
		} catch (IOException e) {
			TemporalConvergence.LOGGER.error("Failed to read frozen entity packet");
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		buffer.writeCompoundTag(packetComp);
	}

	public static class Handler implements IMessageHandler<PacketFrozenEntity, IMessage> {

		@Override
		public IMessage onMessage(PacketFrozenEntity message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
			}
			else {
				TemporalConvergence.LOGGER.error("Client packet (PacketFrozenEntity) recieved on server?!");
			}

			return null;
		}

		public void handle(PacketFrozenEntity message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				NBTTagCompound comp = message.packetComp;

				if (isNBTValid(comp)) {
					NBTTagCompound frozenComp = comp.getCompoundTag(FROZEN_TAG);
					UUID uuid = comp.getUniqueId(UUID_TAG);

					List<EntityFrozen> entityList = Minecraft.getMinecraft().world.getEntities(EntityFrozen.class, entityFrozen -> entityFrozen.getPersistentID().equals(uuid));
					EntityFrozen entity = null;

					//This should never have more than one iteration
					for (EntityFrozen ef : entityList) {
						if (ef.getPersistentID().equals(uuid)) {
							entity = ef;
							break;
						}
					}

					if (entity != null) {
						entity.setFrozenEntity(frozenComp);
					}
				}
				else {
					TemporalConvergence.LOGGER.error("Recieved invalid nbt for frozen entity: {}", message.packetComp);
				}
			}
			else {
				TemporalConvergence.LOGGER.error("PacketFrozenEntity.Handler.handle() called on server");
			}
		}

		private boolean isNBTValid(NBTTagCompound comp) {
			return comp.hasKey(FROZEN_TAG, Constants.NBT.TAG_COMPOUND) && comp.hasKey(UUID_MOST_TAG, Constants.NBT.TAG_LONG) && comp.hasKey(UUID_LEAST_TAG, Constants.NBT.TAG_LONG);
		}
	}
}
