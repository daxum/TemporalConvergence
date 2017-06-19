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
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class BlockSolarWood extends BlockLog {
	public BlockSolarWood() {
		setCreativeTab(ModItems.TEMPCONVTAB);
		setUnlocalizedName("solar_wood");
		setRegistryName("solar_wood");
		setHarvestLevel("axe", 0);

		setDefaultState(blockState.getBaseState().withProperty(LOG_AXIS, EnumAxis.Y));
	}

	@Override
	public MapColor getMapColor(IBlockState state) {
		return MapColor.RED;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {LOG_AXIS});
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(LOG_AXIS, EnumAxis.values()[meta & 3]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		switch(state.getValue(LOG_AXIS)) {
		case X: return 0;
		case Y: return 1;
		case Z: return 2;
		case NONE:
		default: return 3;

		}
	}
}
