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
import daxum.temporalconvergence.tileentity.TileTimePlant;
import daxum.temporalconvergence.util.WorldHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockTimePlant extends ItemBlock {

	public ItemBlockTimePlant() {
		super(ModBlocks.TIME_PLANT);
		setRegistryName(ModBlocks.TIME_PLANT.getRegistryName());
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		boolean superVal = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);

		if (superVal) {
			TileTimePlant timePlant = WorldHelper.getTileEntity(world, pos, TileTimePlant.class);

			if (timePlant != null) {
				timePlant.setWitherTimer();
			}
		}

		return superVal;
	}

}
