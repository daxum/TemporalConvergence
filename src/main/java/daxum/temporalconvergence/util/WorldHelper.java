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
package daxum.temporalconvergence.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class WorldHelper {
	public static <T extends TileEntity> T getTileEntity(IBlockAccess world, BlockPos pos, Class<T> targetClass) {
		TileEntity te = world.getTileEntity(pos);

		if (te != null && targetClass.isAssignableFrom(te.getClass())) {
			return targetClass.cast(te);
		}

		return null;
	}

	public static List<EntityPlayer> getPlayersWithinAABB(World world, AxisAlignedBB aabb) {
		List<EntityPlayer> players = new ArrayList<>();

		for (EntityPlayer player : world.playerEntities) {
			if (player.getEntityBoundingBox().intersects(aabb)) {
				players.add(player);
			}
		}

		return players;
	}

	//Finds all tile entities of the given class in the cube defined by minPos and maxPos (inclusive)
	public static <T> List<T> getAllInRange(IBlockAccess world, BlockPos minPos, BlockPos maxPos, Class<T> targetClass) {
		if (minPos.getX() > maxPos.getX() || minPos.getY() > maxPos.getY() || minPos.getZ() > maxPos.getZ()) {
			throw new IllegalArgumentException("minPos " + minPos + " is greater than maxPos " + maxPos);
		}

		List<T> tiles = new ArrayList<>();

		for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
			for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
				for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
					TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

					if (te != null && targetClass.isAssignableFrom(te.getClass())) {
						tiles.add((T)te);
					}
				}
			}
		}

		return tiles;
	}

	//These 6 functions are meant to be called with the result of world.getWorldTime()
	public static boolean isNight(long time) {
		time = time % 24000;
		return time >= 13000 && time < 24000;
	}

	public static boolean isDay(long time) {
		time = time % 24000;
		return !isNight(time);
	}

	public static boolean isDawn(long time) {
		time = time % 24000;
		return time >= 0 && time < 2000;
	}

	public static boolean isNoon(long time) {
		time = time % 24000;
		return time >= 5000 && time < 7000;
	}

	public static boolean isDusk(long time) {
		time = time % 24000;
		return time >= 11000 && time < 13000;
	}

	public static boolean isMidnight(long time) {
		time = time % 24000;
		return time >= 17500 && time < 18500;
	}
}
