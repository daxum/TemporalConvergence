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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEarlyFutureRoadStripe extends BlockBase {
	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool EAST = PropertyBool.create("east");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool WEST = PropertyBool.create("west");

	public BlockEarlyFutureRoadStripe() {
		super("early_future_road_stripe", BlockPresets.WEAK_IRON);
		setStateDefaults(NORTH, true, EAST, true, SOUTH, true, WEST, true);
		setLightLevel(0.625f);
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
		boolean north = world.getBlockState(pos.north()).getBlock() != this;
		boolean east = world.getBlockState(pos.east()).getBlock() != this;
		boolean south = world.getBlockState(pos.south()).getBlock() != this;
		boolean west = world.getBlockState(pos.west()).getBlock() != this;

		return getDefaultState().withProperty(NORTH, north).withProperty(EAST, east).withProperty(SOUTH, south).withProperty(WEST, west);
	}
}
