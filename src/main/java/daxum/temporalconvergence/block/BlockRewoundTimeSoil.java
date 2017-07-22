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
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRewoundTimeSoil extends BlockBase {
	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool EAST = PropertyBool.create("east");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool WEST = PropertyBool.create("west");
	public static final AxisAlignedBB MAIN_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.9375, 1.0);
	public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0, 0.9375, 0.0, 1.0, 1.0, 0.0625);
	public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.9375, 0.9375, 0.0, 1.0, 1.0, 1.0);
	public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0, 0.9375, 0.9375, 1.0, 1.0, 1.0);
	public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.0, 0.9375, 0.0, 0.0625, 1.0, 1.0);

	public BlockRewoundTimeSoil() {
		super(Material.GROUND, "rewound_soil", 0.6f, 3.0f, Tool.SHOVEL, MiningLevel.WOOD, SoundType.SLIME);
		setStateDefaults(NORTH, true, EAST, true, SOUTH, true, WEST, true);
		setTickRandomly(true);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB aabb, List<AxisAlignedBB> aabbList, Entity entity, boolean actualState) {
		addCollisionBoxToList(pos, aabb, aabbList, MAIN_AABB);

		if (state.getValue(NORTH)) addCollisionBoxToList(pos, aabb, aabbList, NORTH_AABB);
		if (state.getValue(EAST)) addCollisionBoxToList(pos, aabb, aabbList, EAST_AABB);
		if (state.getValue(SOUTH)) addCollisionBoxToList(pos, aabb, aabbList, SOUTH_AABB);
		if (state.getValue(WEST)) addCollisionBoxToList(pos, aabb, aabbList, WEST_AABB);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return FULL_BLOCK_AABB;
	}

	@Override
	protected boolean isCube() {
		return false;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (world.isAirBlock(pos.up()) && rand.nextInt(15) == 0)
			world.setBlockState(pos.up(), ModBlocks.REWOUND_TIME.getDefaultState());
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed, BlockPos changedPos) {
		boolean north = world.getBlockState(pos.north()).getBlock() != this;
		boolean east = world.getBlockState(pos.east()).getBlock() != this;
		boolean south = world.getBlockState(pos.south()).getBlock() != this;
		boolean west = world.getBlockState(pos.west()).getBlock() != this;

		if (state.getValue(NORTH) != north || state.getValue(EAST) != east || state.getValue(SOUTH) != south || state.getValue(WEST) != west)
			world.setBlockState(pos, getDefaultState().withProperty(NORTH, north).withProperty(EAST, east).withProperty(SOUTH, south).withProperty(WEST, west));
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(NORTH, world.getBlockState(pos.north()).getBlock() != this)
				.withProperty(EAST, world.getBlockState(pos).getBlock() != this)
				.withProperty(SOUTH, world.getBlockState(pos).getBlock() != this)
				.withProperty(WEST, world.getBlockState(pos).getBlock() != this);
	}
}
