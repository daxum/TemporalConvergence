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

import daxum.temporalconvergence.item.ModItems;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockTimeWood extends BlockRotatedPillar {
	public BlockTimeWood() {
		super(Material.WOOD);
		setCreativeTab(ModItems.TEMPCONVTAB);
		setUnlocalizedName("time_wood");
		setRegistryName("time_wood");
		setHardness(2.0f);
		setHarvestLevel("axe", 0);
		setSoundType(SoundType.WOOD);
	}

	@Override
	public boolean isWood(IBlockAccess world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() == this;
	}
}
