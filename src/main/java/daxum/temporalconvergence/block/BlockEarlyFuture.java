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

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEarlyFuture extends BlockBase {
	public static final PropertyEnum<EnumFutureBlockType> VARIANT = PropertyEnum.create("variant", EnumFutureBlockType.class);

	BlockEarlyFuture() {
		super("early_future_block", BlockPresets.WEAK_IRON);
		setStateDefaults(new Default(VARIANT, EnumFutureBlockType.PLAIN));
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, EnumFutureBlockType.PLAIN.ordinal()));
		list.add(new ItemStack(this, 1, EnumFutureBlockType.FLOOR.ordinal()));
	}

	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(this, 1, state.getValue(VARIANT).ordinal());
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(VARIANT).ordinal();
	}

	public static enum EnumFutureBlockType implements IStringSerializable {
		PLAIN("plain"),
		FLOOR("floor");

		private String name;

		private EnumFutureBlockType(String n) {
			name = n;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
