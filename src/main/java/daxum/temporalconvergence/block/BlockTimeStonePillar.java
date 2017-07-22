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
package daxum.temporalconvergence.block;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTimeStonePillar extends BlockBase {
	public static final PropertyBool TOP = PropertyBool.create("top");
	public static final PropertyBool BOTTOM = PropertyBool.create("bottom");
	public static final AxisAlignedBB MIDDLE_AABB = new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 1.0, 0.6875);
	public static final AxisAlignedBB BOTTOM_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.125, 1.0);
	public static final AxisAlignedBB TOP_AABB = new AxisAlignedBB( 0.1875, 0.75, 0.1875, 0.8125, 1.0, 0.8125);
	public static final AxisAlignedBB TOP_MIDDLE_AABB = new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 0.75, 0.6875);
	public static final AxisAlignedBB BOTTOM_MIDDLE_AABB = new AxisAlignedBB(0.3125, 0.125, 0.3125, 0.6875, 1.0, 0.6875);
	public static final AxisAlignedBB BOTH_MIDDLE_AABB = new AxisAlignedBB(0.3125, 0.125, 0.3125, 0.6875, 0.75, 0.6875);

	BlockTimeStonePillar() {
		super("time_stone_pillar");
		setStateDefaults(TOP, true, BOTTOM, true);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(TOP, world.getBlockState(pos.up()).getBlock() != this).withProperty(BOTTOM, world.getBlockState(pos.down()).getBlock() != this);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed, BlockPos changedPos) {
		boolean topBlock = world.getBlockState(pos.up()).getBlock() == this;
		boolean bottomBlock = world.getBlockState(pos.down()).getBlock() == this;

		if (state.getValue(TOP) != topBlock || state.getValue(BOTTOM) != bottomBlock)
			world.setBlockState(pos, getDefaultState().withProperty(TOP, !topBlock).withProperty(BOTTOM, !bottomBlock));
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return MIDDLE_AABB;
	}

	@Override
	public boolean hasMultipleBoundingBoxes() {
		return true;
	}

	@Override
	public AxisAlignedBB[] getNewBoundingBoxList(World world, BlockPos pos, IBlockState state) {
		AxisAlignedBB[] aabbList = new AxisAlignedBB[state.getValue(BOTTOM) ? state.getValue(TOP) ? 3 : 2 : state.getValue(TOP) ? 2 : 1];
		int i = 0;

		aabbList[i++] = state.getValue(BOTTOM) ? state.getValue(TOP) ? BOTH_MIDDLE_AABB : BOTTOM_MIDDLE_AABB : state.getValue(TOP) ? TOP_MIDDLE_AABB : MIDDLE_AABB;

		if (state.getValue(TOP)) {
			aabbList[i++] = TOP_AABB;
		}

		if (state.getValue(BOTTOM)) {
			aabbList[i++] = BOTTOM_AABB;
		}

		return aabbList;
	}

	@Override
	protected boolean isCube() {
		return false;
	}
}
