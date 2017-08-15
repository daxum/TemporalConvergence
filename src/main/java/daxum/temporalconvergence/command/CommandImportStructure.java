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

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.world.futurecity.StructureHandler;
import daxum.temporalconvergence.world.futurecity.StructureHandler.StateData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class CommandImportStructure extends CommandBase {

	@Override
	public String getName() {
		return "tempconvimport";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.tempconvimport.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length > 5 || args.length < 4) {
			throw new WrongUsageException("commands.tempconvimport.usage");
		}
		else {
			BlockPos startPos = parseBlockPos(sender, args, 0, false);
			Rotation rot = null;

			if (args.length == 4) {
				rot = Rotation.NONE;
			}
			else {
				if (args[4].equals("0")) {
					rot = Rotation.NONE;
				}
				else if (args[4].equals("90")) {
					rot = Rotation.CLOCKWISE_90;
				}
				else if (args[4].equals("180")) {
					rot = Rotation.CLOCKWISE_180;
				}
				else if (args[4].equals("270")) {
					rot = Rotation.COUNTERCLOCKWISE_90;
				}
				else {
					throw new WrongUsageException("commands.tempconvimport.badrot");
				}
			}

			StateData[] data = StructureHandler.getStructureWithRotation(args[3], rot);

			if (data == null) {
				throw new CommandException("commands.tempconvimport.nofile", args[3] + ".nbt");
			}
			else {
				TemporalConvergence.LOGGER.info("loaded {} blocks", data.length);

				for (StateData sd : data) {
					sender.getEntityWorld().setBlockState(new BlockPos(startPos.getX() + sd.getX(), startPos.getY() + sd.getY(), startPos.getZ() + sd.getZ()), sd.state);
				}

				notifyCommandListener(sender, this, "commands.tempconvimport.success", args[3] + ".nbt", startPos.getX(), startPos.getY(), startPos.getZ());
			}
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		if (args.length > 0 && args.length <= 3) {
			return getTabCompletionCoordinate(args, 0, targetPos);
		}
		else if (args.length == 4) {
			return Collections.singletonList("file");
		}
		else if (args.length == 5) {
			return getListOfStringsMatchingLastWord(args, "0", "90", "180", "270");
		}
		else {
			return Collections.emptyList();
		}
	}
}
