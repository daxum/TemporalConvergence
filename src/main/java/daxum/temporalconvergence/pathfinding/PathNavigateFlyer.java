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
package daxum.temporalconvergence.pathfinding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PathNavigateFlyer extends PathNavigate {
	public PathNavigateFlyer(EntityLiving entityliving, World world) {
		super(entityliving, world);
	}

	@Override
	protected PathFinder getPathFinder() {
		return new PathFinder(new FlyNodeProcessor());
	}

	@Override
	protected Vec3d getEntityPosition() {
		return new Vec3d(theEntity.posX, theEntity.posY + theEntity.height * 0.5, theEntity.posZ);
	}

	@Override
	protected boolean canNavigate() {
		return true;
	}

	@Override
	protected boolean isDirectPathBetweenPoints(Vec3d startPos, Vec3d endPos, int widthX, int height, int widthZ) {
		return true;
	}

	@Override
	public boolean canEntityStandOnPos(BlockPos pos) {
		return !world.getBlockState(pos).isFullBlock();
	}

	@Override
	protected void pathFollow() {
		Vec3d entityPos = getEntityPosition();
		float f = theEntity.width * theEntity.width;

		if (entityPos.squareDistanceTo(currentPath.getVectorFromIndex(theEntity, currentPath.getCurrentPathIndex())) < f)
		{
			currentPath.incrementPathIndex();
		}

		for (int j = Math.min(currentPath.getCurrentPathIndex() + 6, currentPath.getCurrentPathLength() - 1); j > currentPath.getCurrentPathIndex(); --j)
		{
			Vec3d pathVec = currentPath.getVectorFromIndex(theEntity, j);

			if (pathVec.squareDistanceTo(entityPos) <= 36.0 && isDirectPathBetweenPoints(entityPos, pathVec, 0, 0, 0))
			{
				currentPath.setCurrentPathIndex(j);
				break;
			}
		}

		checkForStuck(entityPos);
	}
}
