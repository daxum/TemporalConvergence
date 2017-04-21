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
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockEarlyFutureSlab extends BlockSlab {
	public static final PropertyEnum<EnumVar> VARIANT = PropertyEnum.create("variant", EnumVar.class);

	public BlockEarlyFutureSlab() {
		super(Material.IRON, MapColor.BLACK);
		setUnlocalizedName("early_future_slab");

		if (isDouble())
			setRegistryName("early_future_double_slab");
		else
			setRegistryName("early_future_slab");

		setCreativeTab(ModItems.TEMPCONVTAB);
		setHardness(2.0f);
		setResistance(10.0f);
		setHarvestLevel("pickaxe", 0);
		setSoundType(SoundType.METAL);

		IBlockState state = blockState.getBaseState();

		state = state.withProperty(VARIANT, EnumVar.EARLY_FUTURE_BLOCK);

		if (!isDouble())
			state = state.withProperty(HALF, EnumBlockHalf.BOTTOM);

		setDefaultState(state);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_HALF_SLAB);
	}

	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(ModBlocks.EARLY_FUTURE_HALF_SLAB, 1, state.getValue(VARIANT).getMeta());
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(VARIANT).getMeta();
	}

	@Override
	public IProperty<?> getVariantProperty() {
		return VARIANT;
	}

	@Override
	public Comparable<?> getTypeForItem(ItemStack stack) {
		return EnumVar.getFromMeta(stack.getMetadata() & 7);
	}

	@Override
	public String getUnlocalizedName(int meta) {
		return super.getUnlocalizedName() + "." + EnumVar.getFromMeta(meta).getName();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = state.getValue(VARIANT).getMeta();

		if (!isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP)
			meta |= 8;

		return meta;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = getDefaultState().withProperty(VARIANT, EnumVar.getFromMeta(meta));

		if (!isDouble())
			state = state.withProperty(HALF, (meta & 8) == 8 ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM);

		return state;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return isDouble() ? new BlockStateContainer(this, new IProperty[] {VARIANT}) : new BlockStateContainer(this, new IProperty[] {HALF, VARIANT});
	}

	public static enum EnumVar implements IStringSerializable {
		EARLY_FUTURE_BLOCK(0, "early_future_block");

		private final int meta;
		private final String name;

		private EnumVar(int m, String n) {
			meta = m;
			name = n;
		}

		@Override
		public String getName() {
			return name;
		}

		public int getMeta() {
			return meta;
		}

		public static EnumVar getFromMeta(int meta) {
			switch(meta & 7) {
			default:
			case 0: return EARLY_FUTURE_BLOCK;
			}
		}
	}
}
