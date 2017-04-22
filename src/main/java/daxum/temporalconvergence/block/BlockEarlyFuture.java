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

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockEarlyFuture extends BlockBase {
	public static final PropertyEnum<EnumFutureBlockType> VARIANT = PropertyEnum.create("variant", EnumFutureBlockType.class);

	BlockEarlyFuture() {
		super(Material.IRON, "early_future_block", 2.0f, 10.0f, "pickaxe", 0, SoundType.METAL);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, EnumFutureBlockType.PLAIN));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, EnumFutureBlockType.PLAIN.getMeta()));
		list.add(new ItemStack(this, 1, EnumFutureBlockType.FLOOR.getMeta()));
	}

	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(this, 1, state.getValue(VARIANT).getMeta());
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(VARIANT, EnumFutureBlockType.getFromMeta(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(VARIANT).getMeta();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {VARIANT});
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(VARIANT).getMeta();
	}

	public static enum EnumFutureBlockType implements IStringSerializable {
		PLAIN(0, "plain"),
		FLOOR(1, "floor");

		private int meta;
		private String name;

		private EnumFutureBlockType(int m, String n) {
			meta = m;
			name = n;
		}

		public int getMeta() {
			return meta;
		}

		public static EnumFutureBlockType getFromMeta(int meta) {
			switch(meta) {
			default:
			case 0: return PLAIN;
			case 1: return FLOOR;
			}
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
