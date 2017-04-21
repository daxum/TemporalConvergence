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

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTimeStonePillar extends BlockBase {
	public static final PropertyBool TOP = PropertyBool.create("top");
	public static final PropertyBool BOTTOM = PropertyBool.create("bottom");
	public static final AxisAlignedBB MIDDLE_AABB = new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 1.0, 0.6875);
	public static final AxisAlignedBB BOTTOM_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0);
	public static final AxisAlignedBB TOP_AABB = new AxisAlignedBB( 0.1875, 0.75, 0.1875, 0.8125, 1.0, 0.8125);

	BlockTimeStonePillar() {
		super("time_stone_pillar");
		setDefaultState(blockState.getBaseState().withProperty(TOP, true).withProperty(BOTTOM, true));
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
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB aabb, List<AxisAlignedBB> aabbList, Entity entity, boolean actualState) {
		addCollisionBoxToList(pos, aabb, aabbList, MIDDLE_AABB);

		if (state.getValue(BOTTOM))
			addCollisionBoxToList(pos, aabb, aabbList, BOTTOM_AABB);
		if (state.getValue(TOP))
			addCollisionBoxToList(pos, aabb, aabbList, TOP_AABB);
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		RayTraceResult rtr = rayTrace(pos, start, end, MIDDLE_AABB);

		if (rtr == null) {
			if (state.getValue(BOTTOM))
				rtr = rayTrace(pos, start, end, BOTTOM_AABB);
			if (rtr == null && state.getValue(TOP))
				rtr = rayTrace(pos, start, end, TOP_AABB);
		}

		return rtr;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(TOP, (meta & 1) == 1).withProperty(BOTTOM, (meta & 2) == 2);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(TOP) ? 1 : 0) | (state.getValue(BOTTOM) ? 2 : 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {TOP, BOTTOM});
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}
}
