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
import net.minecraft.block.BlockCrops;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRewoundTime extends BlockBase {
	public static final PropertyInteger AGE = BlockCrops.AGE;
	public static final AxisAlignedBB[] AABB_LIST = new AxisAlignedBB[] {
			new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.125, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.1875, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.25, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.3125, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.625, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.8125, 1.0),
			new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
	};

	public BlockRewoundTime() {
		super("rewound_time", BlockPresets.PLANT);
		setTickRandomly(true);
		setCreativeTab(null);
		disableStats();
		setStateDefaults(AGE, 7);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AABB_LIST[state.getValue(AGE)];
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (rand.nextInt(10) == 0) {
			if (state.getValue(AGE) > 0)
				world.setBlockState(pos, state.withProperty(AGE, state.getValue(AGE) - 1), 2);
			else
				world.setBlockToAir(pos);
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed, BlockPos changedPos) {
		if (world.getBlockState(pos.down()).getBlock() != ModBlocks.REWOUND_SOIL) {
			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(getItemDropped(state, RANDOM, 0)));
			world.setBlockToAir(pos);
		}
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> stacks = new ArrayList();

		if (state.getValue(AGE) == 0)
			stacks.add(new ItemStack(ModItems.REWOUND_TIME_SEEDS));
		else if (state.getValue(AGE) == 7)
			stacks.add(new ItemStack(ModItems.TIME_BULB, Math.max(RANDOM.nextInt(fortune + 2), MathHelper.ceil(fortune / 2.0))));

		return stacks;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(AGE) == 0 ? ModItems.REWOUND_TIME_SEEDS : state.getValue(AGE) == 7 ? ModItems.TIME_BULB : Items.AIR;
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(ModItems.REWOUND_TIME_SEEDS);
	}

	@Override
	protected boolean isCube() {
		return false;
	}

	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
}
