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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.BlockPos;

public class CommandExportStructure extends CommandBase {

	@Override
	public String getName() {
		return "tempconvexport";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.tempconvexport.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		//Prevent potential flooding of dedicated servers with masses of files
		if (!server.isSinglePlayer()) {
			throw new CommandException("commands.tempconvexport.noserver");
		}
		else if (args.length != 7) {
			if (args.length == 7 + extras.length) {
				throw new CommandException("commands.tempconvexport.verywrong");
			}
			else {
				throw new WrongUsageException("commands.tempconvexport.usage");
			}
		}
		else {
			BlockPos tempStart = parseBlockPos(sender, args, 0, false);
			BlockPos tempEnd = parseBlockPos(sender, args, 3, false);

			int smallX = tempStart.getX() < tempEnd.getX() ? tempStart.getX() : tempEnd.getX();
			int smallY = tempStart.getY() < tempEnd.getY() ? tempStart.getY() : tempEnd.getY();
			int smallZ = tempStart.getZ() < tempEnd.getZ() ? tempStart.getZ() : tempEnd.getZ();

			BlockPos startPos = new BlockPos(smallX, smallY, smallZ);

			int largeX = tempStart.getX() > tempEnd.getX() ? tempStart.getX() : tempEnd.getX();
			int largeY = tempStart.getY() > tempEnd.getY() ? tempStart.getY() : tempEnd.getY();
			int largeZ = tempStart.getZ() > tempEnd.getZ() ? tempStart.getZ() : tempEnd.getZ();

			BlockPos endPos = new BlockPos(largeX, largeY, largeZ);

			if (sender.getEntityWorld().isOutsideBuildHeight(startPos) || sender.getEntityWorld().isOutsideBuildHeight(endPos)) {
				throw new CommandException("commands.tempconvexport.outsideworld");
			}

			if (endPos.getX() - startPos.getX() > 16 || endPos.getZ() - startPos.getZ() > 16) {
				throw new CommandException("commands.tempconvexport.tobig");
			}

			if (args[6].length() > 251) {
				throw new CommandException("commands.tempconvexport.nametolong");
			}

			if (isInvalidFileName(args[6])) {
				throw new CommandException("commands.tempconvexport.invalidname");
			}

			IBlockState[][][] states = new IBlockState[Math.abs(startPos.getX() - endPos.getX()) + 1][Math.abs(startPos.getY() - endPos.getY()) + 1][Math.abs(startPos.getZ() - endPos.getZ()) + 1];

			for (int x = startPos.getX(); x <= endPos.getX(); x++) {
				for (int y = startPos.getY(); y <= endPos.getY(); y++) {
					for (int z = startPos.getZ(); z <= endPos.getZ(); z++) {
						states[x - startPos.getX()][y - startPos.getY()][z - startPos.getZ()] = sender.getEntityWorld().getChunkFromChunkCoords(x >> 4, z >> 4).getBlockState(x, y, z);
					}
				}
			}

			//Currently not possible to reach on dedicated server, but just in case
			saveStructure((server.isSinglePlayer() ? "saves/" : "") + server.getFolderName() + "/temporalconvergence_structures/", args[6] + ".nbt", states);

			notifyCommandListener(sender, this, "commands.tempconvexport.success", args[6] + ".nbt");
		}
	}

	private void saveStructure(String parentFolder, String saveFile, IBlockState[][][] states) throws CommandException {
		Map<IBlockState, Integer> stateMap = new HashMap<>();
		List<IBlockState> stateList = new ArrayList<>();

		int id = 0;
		for (int i = 0; i < states.length; i++) {
			for (int j = 0; j < states[i].length; j++) {
				for (int k = 0; k < states[i][j].length; k++) {
					if (stateMap.get(states[i][j][k]) == null) {
						stateMap.put(states[i][j][k], id++);
						stateList.add(states[i][j][k]);
					}
				}
			}
		}

		NBTTagList stateIds = new NBTTagList();

		for (IBlockState state : stateList) {
			stateIds.appendTag(NBTUtil.writeBlockState(new NBTTagCompound(), state));
		}

		NBTTagList statePos = new NBTTagList();

		for (int i = 0; i < states.length; i++) {
			for (int j = 0; j < states[i].length; j++) {
				for (int k = 0; k < states[i][j].length; k++) {
					NBTTagCompound comp = new NBTTagCompound();

					comp.setLong("pos", (long)(i & 15) << 32 | (long)(k & 15) << 36 | j);
					comp.setInteger("state", stateMap.get(states[i][j][k]));

					statePos.appendTag(comp);
				}
			}
		}
		TemporalConvergence.LOGGER.info("Saved {} blocks", statePos.tagCount());
		NBTTagCompound toSave = new NBTTagCompound();
		toSave.setByte("size", (byte) (states.length - 1 & 15 | (states[0][0].length - 1 & 15) << 4));
		toSave.setTag("map", stateIds);
		toSave.setTag("data", statePos);

		File dir = new File(parentFolder);

		if (!dir.exists() && !dir.mkdirs() || !dir.isDirectory()) {
			throw new CommandException("Commands.tempconvexport.filefail");
		}

		File file = new File(parentFolder, saveFile);
		OutputStream stream = null;

		try {
			stream = new FileOutputStream(file);
			CompressedStreamTools.writeCompressed(toSave, stream);
		} catch (IOException e) {
			throw new CommandException("Commands.tempconvexport.filefail");
		}
		finally {
			IOUtils.closeQuietly(stream);
		}
	}

	private boolean isInvalidFileName(String name) {
		for (char c : ChatAllowedCharacters.ILLEGAL_FILE_CHARACTERS) {
			for (char c2 : name.toCharArray()) {
				if (c == c2) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		if (args.length > 0 && args.length <= 3) {
			return getTabCompletionCoordinate(args, 0, targetPos);
		}
		else if (args.length > 3 && args.length <= 6) {
			return getTabCompletionCoordinate(args, 3, targetPos);
		}
		else if (args.length == 7) {
			return Collections.singletonList("file");
		}
		else if (args.length > 8 && args.length - 9 < extras.length) {
			return Collections.singletonList(extras[args.length - 9]);
		}
		else {
			return Collections.emptyList();
		}
	}

	private String[] extras = {"why", "are", "you", "still", "pressing", "tab", "this", "is", "obviously", "too", "many", "arguments"};
}
