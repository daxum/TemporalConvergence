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

import daxum.temporalconvergence.block.ModBlocks;
import net.minecraft.block.Block;
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

public class ItemBrazier extends ItemBase {

	public ItemBrazier() {
		super("brazier");
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		Block block = world.getBlockState(pos).getBlock();

		if (!block.isReplaceable(world, pos)) {
			pos = pos.offset(facing);
		}

		ItemStack stack = player.getHeldItem(hand);

		if (!stack.isEmpty() && isValidLocation(world, player, pos, facing, stack)) {
			world.setBlockState(pos, ModBlocks.BRAZIER_BOTTOM.getDefaultState(), 11);
			world.setBlockState(pos.up(), ModBlocks.BRAZIER.getDefaultState(), 11);

			IBlockState state = world.getBlockState(pos);
			SoundType soundtype = state.getBlock().getSoundType(state, world, pos, player);
			world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0f) / 2.0f, soundtype.getPitch() * 0.8f);
			stack.shrink(1);

			return EnumActionResult.SUCCESS;
		}
		else {
			return EnumActionResult.FAIL;
		}
	}

	private boolean isValidLocation(World world, EntityPlayer player, BlockPos pos, EnumFacing facing, ItemStack stack) {
		return player.canPlayerEdit(pos, facing, stack) && world.mayPlace(ModBlocks.BRAZIER_BOTTOM, pos, false, facing, null)
				&& player.canPlayerEdit(pos.up(), EnumFacing.DOWN, stack) && world.mayPlace(ModBlocks.BRAZIER, pos.up(), false, EnumFacing.DOWN, null);
	}
}
