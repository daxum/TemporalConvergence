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

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFancyEarlyFutureStairs extends BlockBase {
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyEnum PART = PropertyEnum.create("part", EnumOrientation.class);

	public BlockFancyEarlyFutureStairs() {
		super(Material.IRON, "early_future_stairs_fancy", 2.0f, 10.0f, "pickaxe", 0, SoundType.METAL);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(PART, EnumOrientation.BOTTOM));
		setLightLevel(0.95f);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return true; //TODO: Fix properly later
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean isFullyOpaque(IBlockState state){
		return state.getValue(PART) == EnumOrientation.TOP;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		IBlockState state = getDefaultState().withProperty(FACING, placer.getHorizontalFacing());

		if (facing == EnumFacing.DOWN) state = state.withProperty(PART, EnumOrientation.BOTTOM);
		else if (facing == EnumFacing.UP) state = state.withProperty(PART, EnumOrientation.TOP);
		else state = state.withProperty(PART, EnumOrientation.SIDE);

		return state;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = getDefaultState();

		state = state.withProperty(FACING, EnumFacing.getFront(5 - (meta & 3)));
		return state.withProperty(PART, EnumOrientation.get(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 5 - state.getValue(FACING).getIndex() | ((EnumOrientation)state.getValue(PART)).getMetaVal();
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
		return new BlockStateContainer(this, new IProperty[] {FACING, PART});
	}

	public static enum EnumOrientation implements IStringSerializable {
		TOP,
		BOTTOM,
		SIDE;

		@Override
		public String getName() {
			switch(this) {
			default:
			case BOTTOM: return "bottom";
			case SIDE: return "side";
			case TOP: return "top";
			}
		}

		public int getMetaVal() {
			switch(this) {
			default:
			case BOTTOM: return 0;
			case SIDE: return 4;
			case TOP: return 8;
			}
		}

		public static EnumOrientation get(int meta) {
			switch(meta >> 2 & 3) {
			default:
			case 0: return BOTTOM;
			case 1: return SIDE;
			case 2: return TOP;
			}
		}
	}
}
