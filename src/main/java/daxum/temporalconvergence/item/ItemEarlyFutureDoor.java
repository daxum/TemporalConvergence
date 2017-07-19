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
package daxum.temporalconvergence.item;

import daxum.temporalconvergence.block.BlockEarlyFutureDoor;
import daxum.temporalconvergence.block.BlockEarlyFutureDoor.EnumPart;
import daxum.temporalconvergence.block.ModBlocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemEarlyFutureDoor extends ItemBase {
	public ItemEarlyFutureDoor() {
		super("early_future_door");
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		pos = pos.up(); //Get the position above the blocked that was right clicked, as the original block doesn't matter
		if (player != null) {
			//standardize right and left - see BlockEarlyFutureDoor.EnumPart
			pos = player.getHorizontalFacing() == EnumFacing.EAST ? pos.north() : player.getHorizontalFacing() == EnumFacing.SOUTH ? pos.east() : pos;
		}

		if (!validateDoor(world, player, facing, pos))
			return EnumActionResult.FAIL;

		ItemStack stack = player.getHeldItem(hand);

		if (player.canPlayerEdit(pos, facing, stack) && ModBlocks.EARLY_FUTURE_DOOR.canPlaceBlockAt(world, pos)) {
			placeDoor(world, pos, getLeftPos(player, pos), player.getHorizontalFacing() == EnumFacing.EAST || player.getHorizontalFacing() == EnumFacing.WEST);

			SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
			world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0f) / 2.0f, soundtype.getPitch() * 0.8f);

			stack.shrink(1);
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.FAIL;
	}

	public boolean validateDoor(World world, EntityPlayer player, EnumFacing facing, BlockPos pos) {
		if (facing != EnumFacing.UP || player == null)
			return false;

		return isAreaClear(world, pos, getLeftPos(player, pos));
	}

	public void placeDoor(World world, BlockPos pos, BlockPos leftPos, boolean ns) {
		IBlockState state = ModBlocks.EARLY_FUTURE_DOOR.getDefaultState().withProperty(BlockEarlyFutureDoor.NORTH_SOUTH, ns);

		world.setBlockState(pos, state.withProperty(BlockEarlyFutureDoor.PART, EnumPart.BOTTOM_RIGHT));
		world.setBlockState(pos.up(), state.withProperty(BlockEarlyFutureDoor.PART, EnumPart.TOP_RIGHT));
		world.setBlockState(leftPos, state.withProperty(BlockEarlyFutureDoor.PART, EnumPart.BOTTOM_LEFT));
		world.setBlockState(leftPos.up(), state.withProperty(BlockEarlyFutureDoor.PART, EnumPart.TOP_LEFT));

		world.notifyNeighborsOfStateChange(pos, ModBlocks.EARLY_FUTURE_DOOR, false);
		world.notifyNeighborsOfStateChange(leftPos, ModBlocks.EARLY_FUTURE_DOOR, false);
		world.notifyNeighborsOfStateChange(pos.up(), ModBlocks.EARLY_FUTURE_DOOR, false);
		world.notifyNeighborsOfStateChange(leftPos.up(), ModBlocks.EARLY_FUTURE_DOOR, false);
	}

	public boolean isAreaClear(World world, BlockPos pos, BlockPos leftPos) {
		return isReplaceable(world, pos) && isReplaceable(world, pos.up()) && isReplaceable(world, leftPos) && isReplaceable(world, leftPos.up());
	}

	public boolean isReplaceable(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
	}

	public BlockPos getLeftPos(EntityPlayer player, BlockPos pos) {
		if (player == null) return pos;

		switch(player.getHorizontalFacing()) {
		case NORTH:
		case SOUTH: return pos.west();
		case EAST:
		case WEST: return pos.south();
		default: return pos;
		}
	}
}
