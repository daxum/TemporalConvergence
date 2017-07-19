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
import java.util.Random;

import daxum.temporalconvergence.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockEarlyFutureDoor extends BlockBase {
	public static final PropertyBool OPEN = PropertyBool.create("open");
	public static final PropertyBool NORTH_SOUTH = PropertyBool.create("ns");
	public static final PropertyEnum<Part> PART = PropertyEnum.create("part", Part.class);
	public static final AxisAlignedBB OPEN_LEFT_NS_AABB = new AxisAlignedBB(0.375, 0.0, 0.875, 0.625, 1.0, 1.0);
	public static final AxisAlignedBB OPEN_RIGHT_NS_AABB = new AxisAlignedBB(0.375, 0.0, 0.0, 0.625, 1.0, 0.125);
	public static final AxisAlignedBB OPEN_LEFT_WE_AABB = new AxisAlignedBB(0.0, 0.0, 0.375, 0.125, 1.0, 0.625);
	public static final AxisAlignedBB OPEN_RIGHT_WE_AABB = new AxisAlignedBB(0.875, 0.0, 0.375, 1.0, 1.0, 0.625);
	public static final AxisAlignedBB NS_AABB = new AxisAlignedBB(0.375, 0.0, 0.0, 0.625, 1.0, 1.0);
	public static final AxisAlignedBB WE_AABB = new AxisAlignedBB(0.0, 0.0, 0.375, 1.0, 1.0, 0.625);
	public static final AxisAlignedBB NS_TOP_AABB = new AxisAlignedBB(0.375, 0.875, 0.0, 0.625, 1.0, 1.0);
	public static final AxisAlignedBB WE_TOP_AABB = new AxisAlignedBB(0.0, 0.875, 0.375, 1.0, 1.0, 0.625);

	public BlockEarlyFutureDoor() {
		super("early_future_door");
		setStateDefaults(new Default(OPEN, false), new Default(PART, Part.BOTTOM_RIGHT), new Default(NORTH_SOUTH, true));
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(OPEN) ? getOpenAABB(state) : state.getValue(NORTH_SOUTH) ? NS_AABB : WE_AABB;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB aabb, List<AxisAlignedBB> aabbList, Entity entity, boolean actualState) {
		if (!state.getValue(OPEN))
			addCollisionBoxToList(pos, aabb, aabbList, state.getCollisionBoundingBox(world, pos));
		else {
			addCollisionBoxToList(pos, aabb, aabbList, getOpenAABB(state));

			if (state.getValue(PART) == Part.TOP_LEFT || state.getValue(PART) == Part.TOP_RIGHT)
				addCollisionBoxToList(pos, aabb, aabbList, state.getValue(NORTH_SOUTH) ? NS_TOP_AABB : WE_TOP_AABB);
		}
	}

	public AxisAlignedBB getOpenAABB(IBlockState state) {
		if (state.getValue(NORTH_SOUTH)) {
			if (state.getValue(PART) == Part.BOTTOM_LEFT || state.getValue(PART) == Part.TOP_LEFT)
				return OPEN_LEFT_NS_AABB;
			return OPEN_RIGHT_NS_AABB;
		}
		else {
			if (state.getValue(PART) == Part.BOTTOM_LEFT || state.getValue(PART) == Part.TOP_LEFT)
				return OPEN_LEFT_WE_AABB;
			return OPEN_RIGHT_WE_AABB;
		}
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		if (!state.getValue(OPEN))
			return rayTrace(pos, start, end, state.getBoundingBox(world, pos));
		else {
			List<AxisAlignedBB> aabbList = new ArrayList<>();

			aabbList.add(getOpenAABB(state));

			if (state.getValue(PART) == Part.TOP_LEFT || state.getValue(PART) == Part.TOP_RIGHT)
				aabbList.add(state.getValue(NORTH_SOUTH) ? NS_TOP_AABB : WE_TOP_AABB);

			for (AxisAlignedBB aabb : aabbList) {
				RayTraceResult rtr = rayTrace(pos, start, end, aabb);

				if (rtr != null)
					return new RayTraceResult(rtr.hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), rtr.sideHit, pos);
			}

			return null;
		}
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		for (BlockPos pos2 : getParts(state, pos, false)) {
			if (world.getBlockState(pos2).getBlock() == this)
				world.setBlockToAir(pos2);
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos changedPos) {
		setOpening(world, state, pos, isDoorPowered(world, state, pos));
	}

	public void setOpening(World world, IBlockState state, BlockPos pos, boolean open) {
		for (BlockPos pos2 : getParts(state, pos, true)) {
			state = world.getBlockState(pos2);

			if (state.getBlock() == this && state.getValue(OPEN) != open) {
				world.setBlockState(pos2, state.withProperty(OPEN, open), 10);
			}
		}
	}

	private boolean isDoorPowered(World world, IBlockState state, BlockPos pos) {
		BlockPos[] doorPos = getParts(state, pos, true);

		for (BlockPos currentPos : doorPos) {
			if (world.isBlockPowered(currentPos)) {
				return true;
			}
		}

		return false;
	}

	public BlockPos[] getParts(IBlockState state, BlockPos pos, boolean includeGiven) {
		BlockPos posList[];

		if (includeGiven) {
			posList = new BlockPos[4];
			posList[3] = pos;
		}
		else {
			posList = new BlockPos[3];
		}

		Boolean ns = state.getValue(NORTH_SOUTH);

		switch(state.getValue(PART)) {
		case BOTTOM_LEFT: posList[0] = ns ? pos.north() : pos.east(); posList[1] = pos.up(); posList[2] = posList[0].up(); break;
		case BOTTOM_RIGHT: posList[0] = ns ? pos.south() : pos.west(); posList[1] = pos.up(); posList[2] = posList[0].up(); break;
		case TOP_LEFT: posList[0] = ns ? pos.north() : pos.east(); posList[1] = pos.down(); posList[2] = posList[0].down(); break;
		case TOP_RIGHT: posList[0] = ns ? pos.south() : pos.west(); posList[1] = pos.down(); posList[2] = posList[0].down(); break;
		}

		return posList;
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		switch(rot) {
		case CLOCKWISE_180: return togglePart(state);
		case CLOCKWISE_90:
			if (state.getValue(NORTH_SOUTH)) {
				return state.withProperty(NORTH_SOUTH, false);
			}
			else {
				return togglePart(state).withProperty(NORTH_SOUTH, true);
			}
		case COUNTERCLOCKWISE_90:
			if (state.getValue(NORTH_SOUTH)) {
				return togglePart(state).withProperty(NORTH_SOUTH, false);
			}
			else {
				return state.withProperty(NORTH_SOUTH, true);
			}
		case NONE:
		default: return state;
		}
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror) {
		if (mirror == Mirror.LEFT_RIGHT)
			return togglePart(state);
		return state;
	}

	public IBlockState togglePart(IBlockState state) {
		switch(state.getValue(PART)) {
		case BOTTOM_LEFT: return state.withProperty(PART, Part.BOTTOM_RIGHT);
		case BOTTOM_RIGHT: return state.withProperty(PART, Part.BOTTOM_LEFT);
		case TOP_LEFT: return state.withProperty(PART, Part.TOP_RIGHT);
		case TOP_RIGHT: return state.withProperty(PART, Part.TOP_LEFT);
		default: return state;
		}
	}

	@Override
	protected boolean isCube() {
		return false;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ModItems.EARLY_FUTURE_DOOR;
	}

	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(ModItems.EARLY_FUTURE_DOOR);
	}

	//To simplify things, "right" is either north or east, and "left" is south or west, depending on orientation
	public static enum Part implements IStringSerializable {
		TOP_LEFT("top_left"),
		TOP_RIGHT("top_right"),
		BOTTOM_LEFT("bottom_left"),
		BOTTOM_RIGHT("bottom_right");

		private String name;

		private Part(String n) {
			name = n;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
