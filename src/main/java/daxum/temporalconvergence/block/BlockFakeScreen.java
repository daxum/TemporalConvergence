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

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockFakeScreen extends BlockBase {
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyBool TOP = PropertyBool.create("top");
	public static final AxisAlignedBB BOTTOM_NORTH_AABB = BlockAIBossScreen.NORTH_AABB;
	public static final AxisAlignedBB BOTTOM_EAST_AABB = BlockAIBossScreen.EAST_AABB;
	public static final AxisAlignedBB BOTTOM_SOUTH_AABB = BlockAIBossScreen.SOUTH_AABB;
	public static final AxisAlignedBB BOTTOM_WEST_AABB = BlockAIBossScreen.WEST_AABB;
	public static final AxisAlignedBB TOP_NORTH_AABB = new AxisAlignedBB(0.0, 0.0, 0.875, 1.0, 0.75, 1.0);
	public static final AxisAlignedBB TOP_EAST_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 0.125, 0.75, 1.0);
	public static final AxisAlignedBB TOP_SOUTH_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.75, 0.125);
	public static final AxisAlignedBB TOP_WEST_AABB = new AxisAlignedBB(0.875, 0.0, 0.0, 1.0, 0.75, 1.0);

	public BlockFakeScreen() {
		super("fake_boss_screen", BlockPresets.UNBREAKABLE);
		setSoundType(SoundType.METAL);
		setStateDefaults(FACING, EnumFacing.NORTH, TOP, false);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state.getValue(TOP)) {
			switch(state.getValue(FACING)) {
			case NORTH: return TOP_NORTH_AABB;
			case EAST: return TOP_EAST_AABB;
			case SOUTH: return TOP_SOUTH_AABB;
			case WEST: return TOP_WEST_AABB;
			case UP:
			case DOWN:
			default: TemporalConvergence.LOGGER.error("Invalid FakeScreen (class {}) {} detected at {}", state.getBlock().getClass(), state, pos); return NULL_AABB;
			}
		}
		else {
			switch(state.getValue(FACING)) {
			case NORTH: return BOTTOM_NORTH_AABB;
			case EAST: return BOTTOM_EAST_AABB;
			case SOUTH: return BOTTOM_SOUTH_AABB;
			case WEST: return BOTTOM_WEST_AABB;
			case UP:
			case DOWN:
			default: TemporalConvergence.LOGGER.error("Invalid FakeScreen (class {}) {} detected at {}", state.getBlock().getClass(), state, pos); return NULL_AABB;
			}
		}
	}

	@Override
	protected boolean isCube() {
		return false;
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		return false;
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror) {
		return state.withRotation(mirror.toRotation(state.getValue(FACING)));
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
}
