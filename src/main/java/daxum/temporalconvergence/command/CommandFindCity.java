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
package daxum.temporalconvergence.command;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.world.DimensionHandler;
import daxum.temporalconvergence.world.futurecity.FutureCityGenerator;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandFindCity extends CommandBase {

	@Override
	public String getName() {
		return "findfuturecity";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.findfuturecity.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length > 2 || args.length == 1) {
			throw new WrongUsageException("commands.findfuturecity.usage");
		}
		else {
			if (sender.getEntityWorld().provider.getDimensionType() == DimensionHandler.EARLY_FUTURE) {
				FutureCityGenerator gen = FutureCityGenerator.getGenerator(sender.getEntityWorld());

				if (args.length == 2) {
					long sectX = parseInt(args[0]);
					long sectZ = parseInt(args[1]);
					TemporalConvergence.LOGGER.info("Section: {}, {}, packed: {}", sectX, sectZ, sectX << 32 | sectZ);
					BlockPos pos = gen.getCityLocationForSection(sectX << 32 | sectZ & 0xFFFFFFFFL);
					notifyCommandListener(sender, this, "commands.findfuturecity.success", pos.getX(), pos.getY(), pos.getZ());
				}
				else {
					int chunkX = sender.getPosition().getX() / 16;
					int chunkZ = sender.getPosition().getZ() / 16;
					TemporalConvergence.LOGGER.info("Sender position: {}, {}, Chunk position: {}, {}", sender.getPosition().getX(), sender.getPosition().getZ(), chunkX, chunkZ);
					BlockPos pos = gen.getCityLocationForSection(gen.getSectionForChunk(chunkX, chunkZ));
					notifyCommandListener(sender, this, "commands.findfuturecity.success", pos.getX(), pos.getY(), pos.getZ());
				}
			}
			else {
				throw new CommandException("commands.findfuturecity.wrongdim");
			}
		}
	}

}
