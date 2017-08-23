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

import java.util.Random;

import daxum.temporalconvergence.item.ModItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBrazierBottom extends BlockBase {
	private static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.125, 0.75);
	private static final AxisAlignedBB MIDDLE_AABB = new AxisAlignedBB(0.4375, 0.125, 0.4375, 0.5625, 1.0, 0.5625);

	public BlockBrazierBottom() {
		super("brazier_bottom", BlockPresets.STONE);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.getBlockState(pos.up()).getBlock() instanceof BlockBrazier) {
			return world.getBlockState(pos.up()).getBlock().onBlockActivated(world, pos.up(), world.getBlockState(pos.up()), player, hand, facing, hitX, hitY, hitZ);
		}

		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (world.getBlockState(pos.up()).getBlock() instanceof BlockBrazier) {
			world.setBlockToAir(pos.up());
		}
	}

	@Override
	protected boolean isCube() {
		return false;
	}

	@Override
	public boolean hasMultipleBoundingBoxes() {
		return true;
	}

	@Override
	protected AxisAlignedBB[] getNewBoundingBoxList(World world, BlockPos pos, IBlockState state) {
		return new AxisAlignedBB[] {BASE_AABB, MIDDLE_AABB};
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ModItems.BRAZIER;
	}

	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(ModItems.BRAZIER);
	}
}
