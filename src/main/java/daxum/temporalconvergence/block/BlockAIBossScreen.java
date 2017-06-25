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
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockAIBossScreen extends BlockBase {
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyEnum<ScreenState> STATE = PropertyEnum.create("state", ScreenState.class);
	public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0, 0.25, 0.875, 1.0, 1.0, 1.0);
	public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0, 0.25, 0.0, 0.125, 1.0, 1.0);
	public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0, 0.25, 0.0, 1.0, 1.0, 0.125);
	public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.875, 0.25, 0.0, 1.0, 1.0, 1.0);

	public BlockAIBossScreen() {
		super(Material.BARRIER, "boss_screen", -1.0f, Float.MAX_VALUE, "", 0, SoundType.METAL);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(STATE, ScreenState.OFF));
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		EnumFacing facing = state.getValue(FACING);

		switch(facing) {
		case NORTH: setBlocks(world, new BlockPos[] {pos.east(), pos.up(), pos.east().up()}, pos.getY(), facing); break;
		case EAST: setBlocks(world, new BlockPos[] {pos.south(), pos.up(), pos.south().up()}, pos.getY(), facing); break;
		case SOUTH: setBlocks(world, new BlockPos[] {pos.west(), pos.up(), pos.west().up()}, pos.getY(), facing); break;
		case WEST: setBlocks(world, new BlockPos[] {pos.north(), pos.up(), pos.north().up()}, pos.getY(), facing); break;
		case UP:
		case DOWN:
		default: TemporalConvergence.LOGGER.error("Invalid AIBossScreen (class {}) {} placed at {}", state.getBlock().getClass(), state, pos);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		switch(state.getValue(FACING)) {
		case NORTH: removeBlocks(world, new BlockPos[] {pos.east(), pos.up(), pos.east().up()}); break;
		case EAST: removeBlocks(world, new BlockPos[] {pos.south(), pos.up(), pos.south().up()}); break;
		case SOUTH: removeBlocks(world, new BlockPos[] {pos.west(), pos.up(), pos.west().up()}); break;
		case WEST: removeBlocks(world, new BlockPos[] {pos.north(), pos.up(), pos.north().up()}); break;
		case UP:
		case DOWN:
		default: TemporalConvergence.LOGGER.error("Invalid AIBossScreen (class {}) {} detected at {}", state.getBlock().getClass(), state, pos);
		}
	}

	private void setBlocks(World world, BlockPos[] posList, int initialY, EnumFacing facing) {
		for (BlockPos pos : posList) {
			world.setBlockState(pos, ModBlocks.FAKE_BOSS_SCREEN.getDefaultState().withProperty(BlockFakeScreen.FACING, facing).withProperty(BlockFakeScreen.TOP, initialY != pos.getY()));
		}
	}

	private void removeBlocks(World world, BlockPos[] posList) {
		for (BlockPos pos : posList) {
			world.setBlockToAir(pos);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		switch(state.getValue(FACING)) {
		case NORTH: return NORTH_AABB;
		case EAST: return EAST_AABB;
		case SOUTH: return SOUTH_AABB;
		case WEST: return WEST_AABB;
		case UP:
		case DOWN:
		default: TemporalConvergence.LOGGER.error("Invalid AIBossScreen (class {}) {} detected at {}", state.getBlock().getClass(), state, pos); return NULL_AABB;

		}
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
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		return false;
	}

	public enum ScreenState implements IStringSerializable {
		OFF(0, "off"),
		STATIC(1, "static"),
		OCCUPIED(2, "occupied"),
		FIRING(3, "firing");

		private String name;
		private int meta;

		private ScreenState(int m, String n) {
			name = n;
			meta = m;
		}

		@Override
		public String getName() {
			return name;
		}

		public int getMeta() {
			return meta;
		}

		public static ScreenState getFromMeta(int meta) {
			switch(meta) {
			case 0: return OFF;
			case 1: return STATIC;
			case 2: return OCCUPIED;
			case 3: return FIRING;
			default: return OFF;
			}
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3)).withProperty(STATE, ScreenState.getFromMeta(meta >> 2 & 3));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex() | state.getValue(STATE).getMeta() << 2;
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
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {FACING, STATE});
	}
}
