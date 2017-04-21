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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTimeStonePillar extends BlockBase {
	private static final PropertyEnum<EnumEnds> ENDS = PropertyEnum.create("location", EnumEnds.class);
	private static final AxisAlignedBB MIDDLE_AABB = new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 1.0, 0.6875);
	private static final AxisAlignedBB BOTTOM_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0);
	private static final AxisAlignedBB TOP_AABB = new AxisAlignedBB( 0.1875, 0.75, 0.1875, 0.8125, 1.0, 0.8125);

	BlockTimeStonePillar() {
		super("time_stone_pillar");
		setDefaultState(blockState.getBaseState().withProperty(ENDS, EnumEnds.BOTH));
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		boolean topBlock = world.getBlockState(pos.up()).getBlock() == this;
		boolean bottomBlock = world.getBlockState(pos.down()).getBlock() == this;

		if (topBlock && bottomBlock)
			return getDefaultState().withProperty(ENDS, EnumEnds.NEITHER);
		if (topBlock)
			return getDefaultState().withProperty(ENDS, EnumEnds.BOTTOM);
		if (bottomBlock)
			return getDefaultState().withProperty(ENDS, EnumEnds.TOP);
		return getDefaultState();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed, BlockPos changedPos) {
		boolean topBlock = world.getBlockState(pos.up()).getBlock() == this;
		boolean bottomBlock = world.getBlockState(pos.down()).getBlock() == this;

		IBlockState newState = getDefaultState();

		if (topBlock && bottomBlock)
			newState = getDefaultState().withProperty(ENDS, EnumEnds.NEITHER);
		else if (topBlock)
			newState = getDefaultState().withProperty(ENDS, EnumEnds.BOTTOM);
		else if (bottomBlock)
			newState = getDefaultState().withProperty(ENDS, EnumEnds.TOP);

		if (newState.getValue(ENDS) != state.getValue(ENDS))
			world.setBlockState(pos, newState);
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

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return MIDDLE_AABB;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB aabb, List<AxisAlignedBB> aabbList, Entity entity, boolean actualState) {
		addCollisionBoxToList(pos, aabb, aabbList, MIDDLE_AABB);

		EnumEnds ends = state.getValue(ENDS);
		if (ends == EnumEnds.BOTH || ends == EnumEnds.BOTTOM) {
			addCollisionBoxToList(pos, aabb, aabbList, BOTTOM_AABB);
		}
		if (ends == EnumEnds.BOTH || ends == EnumEnds.TOP) {
			addCollisionBoxToList(pos, aabb, aabbList, TOP_AABB);
		}
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		List<AxisAlignedBB> aabbList = new ArrayList<>();

		aabbList.add(MIDDLE_AABB);

		EnumEnds ends = state.getValue(ENDS);
		if (ends == EnumEnds.BOTH || ends == EnumEnds.BOTTOM)
			aabbList.add(BOTTOM_AABB);
		if (ends == EnumEnds.BOTH || ends == EnumEnds.TOP)
			aabbList.add(TOP_AABB);

		for (AxisAlignedBB aabb : aabbList) {
			RayTraceResult rtr = rayTrace(pos, start, end, aabb);

			if (rtr != null)
				return new RayTraceResult(rtr.hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), rtr.sideHit, pos);
		}

		return null;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		switch(meta) {
		default:
		case 0: return getDefaultState();
		case 1: return getDefaultState().withProperty(ENDS, EnumEnds.NEITHER);
		case 2: return getDefaultState().withProperty(ENDS, EnumEnds.TOP);
		case 3: return getDefaultState().withProperty(ENDS, EnumEnds.BOTTOM);
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		switch(state.getValue(ENDS)) {
		default:
		case BOTH: return 0;
		case NEITHER: return 1;
		case TOP: return 2;
		case BOTTOM: return 3;
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {ENDS});
	}

	public static enum EnumEnds implements IStringSerializable {
		NEITHER,
		TOP,
		BOTTOM,
		BOTH;

		@Override
		public String getName() {
			switch(this) {
			case BOTH: return "single";
			case BOTTOM: return "bottom";
			case NEITHER: return "middle";
			case TOP: return "top";
			default: return "Abort! Abort! EVERYONE PANIC!!1!!11!";
			}
		}
	}
}
