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

import java.util.Collections;
import java.util.List;

import daxum.temporalconvergence.world.DimensionHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;

public class CommandTimeTravel extends CommandBase {

	@Override
	public String getName() {
		return "timetravel";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.timetravel.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 1) {
			throw new WrongUsageException("commands.timetravel.usage");
		}
		else {
			EntityPlayer player = getCommandSenderAsPlayer(sender);

			if (args[0].equals("present")) {
				DimensionHandler.changePlayerDim(player, DimensionType.OVERWORLD);
			}
			else if (args[0].equals("early_future")) {
				DimensionHandler.changePlayerDim(player, DimensionHandler.EARLY_FUTURE);
			}
			else {
				throw new CommandException("commands.timetravel.nodim", args[0]);
			}
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, "present", "early_future");
		}
		else {
			return Collections.emptyList();
		}
	}
}
