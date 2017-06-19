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

import daxum.temporalconvergence.fluid.ModFluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

//TODO: different way to convert, particles
public class BlockFluidTimeWater extends BlockFluidClassic {
	public BlockFluidTimeWater() {
		super(ModFluids.TIME_WATER, Material.WATER);
		setUnlocalizedName("time_water");
		setRegistryName("time_water");
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block oldNeighbor, BlockPos neighborPos) {
		super.neighborChanged(state, world, pos, oldNeighbor, neighborPos);

		if (!neighborPos.equals(pos.up())) {
			checkAndReplace(world, neighborPos);
		}
	}

	@Override
	public int place(World world, BlockPos pos, FluidStack stack, boolean doPlace) {
		int superVal = super.place(world, pos, stack, doPlace);

		if (superVal != 0 && doPlace) {
			checkAllButUp(world, pos);
		}

		return superVal;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		checkAllButUp(world, pos);
	}

	@Override
	public void flowIntoBlock(World world, BlockPos pos, int meta) {
		if (meta >= 0 && displaceIfPossible(world, pos)) {
			world.setBlockState(pos, getBlockState().getBaseState().withProperty(LEVEL, meta), 3);
			checkAllButUp(world, pos);
		}
	}

	public void checkAllButUp(World world, BlockPos pos) {
		checkAndReplace(world, pos.down());
		checkAndReplace(world, pos.north());
		checkAndReplace(world, pos.east());
		checkAndReplace(world, pos.south());
		checkAndReplace(world, pos.west());
	}

	private void checkAndReplace(World world, BlockPos pos) {
		Block toConvert = world.getBlockState(pos).getBlock();

		if (toConvert == ModBlocks.LUNAR_WOOD || toConvert == ModBlocks.SOLAR_WOOD) {
			return;
		}

		if (OreDictionary.containsMatch(false, OreDictionary.getOres("logWood"), new ItemStack(toConvert, 1, OreDictionary.WILDCARD_VALUE))) {
			IBlockState state = world.getBlockState(pos);
			Block newBlock = isDay(world.getWorldTime()) ? ModBlocks.SOLAR_WOOD : ModBlocks.LUNAR_WOOD;

			if (state.getProperties().containsKey(BlockLog.LOG_AXIS)) {
				world.setBlockState(pos, newBlock.getDefaultState().withProperty(BlockLog.LOG_AXIS, state.getValue(BlockLog.LOG_AXIS)));
			}
			else {
				world.setBlockState(pos, newBlock.getDefaultState());
			}
		}
	}

	private boolean isDay(long time) {
		return time >= 0 && time < 13000;
	}
}
