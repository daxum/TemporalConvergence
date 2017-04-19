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

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockEarlyFutureDoor extends BlockBase {
	public static final PropertyBool OPEN = PropertyBool.create("open");
	public static final PropertyBool NORTH_SOUTH = PropertyBool.create("north_south");
	public static final PropertyEnum<EnumPart> PART = PropertyEnum.create("part", EnumPart.class);
	public static final AxisAlignedBB NS_AABB = new AxisAlignedBB(0.375, 0.0, 0.0, 0.625, 1.0, 1.0);
	public static final AxisAlignedBB WE_AABB = new AxisAlignedBB(0.0, 0.0, 0.375, 1.0, 1.0, 0.625);

	public BlockEarlyFutureDoor() {
		super("early_future_door");
		setDefaultState(blockState.getBaseState().withProperty(OPEN, false).withProperty(PART, EnumPart.BOTTOM_RIGHT).withProperty(NORTH_SOUTH, true));
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(OPEN) ? NULL_AABB : state.getValue(NORTH_SOUTH) ? NS_AABB : WE_AABB;
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		BlockPos posList[] = new BlockPos[3];
		Boolean ns = state.getValue(NORTH_SOUTH);

		switch(state.getValue(PART)) {
		case BOTTOM_LEFT: posList[0] = ns ? pos.north() : pos.east(); posList[1] = pos.up(); posList[2] = posList[0].up(); break;
		case BOTTOM_RIGHT: posList[0] = ns ? pos.south() : pos.west(); posList[1] = pos.up(); posList[2] = posList[0].up(); break;
		case TOP_LEFT: posList[0] = ns ? pos.north() : pos.east(); posList[1] = pos.down(); posList[2] = posList[0].down(); break;
		case TOP_RIGHT: posList[0] = ns ? pos.south() : pos.west(); posList[1] = pos.down(); posList[2] = posList[0].down(); break;
		}

		for (BlockPos pos2 : posList) {
			if (world.getBlockState(pos2).getBlock() == this)
				world.setBlockToAir(pos2);
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = getDefaultState().withProperty(OPEN, (meta & 1) == 1);
		state = state.withProperty(NORTH_SOUTH, (meta >> 1 & 1) == 1);
		state = state.withProperty(PART, EnumPart.getFromMeta(meta >> 2 & 3));

		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(OPEN) ? 1 : 0) | (state.getValue(NORTH_SOUTH) ? 2 : 0) | state.getValue(PART).getMeta();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {OPEN, PART, NORTH_SOUTH});
	}

	//To simplify things, "right" is either north or east, and "left" is south or west, depending on orientation
	public static enum EnumPart implements IStringSerializable {
		TOP_LEFT("top_left", 0),
		TOP_RIGHT("top_right", 4),
		BOTTOM_LEFT("bottom_left", 8),
		BOTTOM_RIGHT("bottom_right", 12);

		private String name;
		private int meta;

		private EnumPart(String n, int m) {
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

		public static EnumPart getFromMeta(int meta) {
			switch(meta) {
			default:
			case 0: return TOP_LEFT;
			case 1: return TOP_RIGHT;
			case 2: return BOTTOM_LEFT;
			case 3: return BOTTOM_RIGHT;
			}
		}
	}

	public static IBlockState getState(int part, boolean open, boolean ns) {
		return ModBlocks.EARLY_FUTURE_DOOR.getDefaultState().withProperty(PART, EnumPart.getFromMeta(part)).withProperty(OPEN, open).withProperty(NORTH_SOUTH, ns);
	}
}
