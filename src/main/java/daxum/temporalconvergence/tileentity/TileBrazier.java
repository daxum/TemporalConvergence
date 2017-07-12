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
package daxum.temporalconvergence.tileentity;

import java.util.Random;

import daxum.temporalconvergence.block.BlockBrazier;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class TileBrazier extends TileEntityBase implements ITickable {
	private static final int MAX_BURN_TIME = 2400;

	private final Random rand = new Random();
	private int burnTime = 0;

	@Override
	public void update() {
		IBlockState state = world.getBlockState(pos);

		if (!world.isRemote && state.getValue(BlockBrazier.BURNING)) {
			if (BlockBrazier.hasDust(state)) {
				if (burnTime > 0) {
					burnTime--;
					markDirty();
				}
				else {
					world.setBlockState(pos, BlockBrazier.getLowerDustState(state));

					if (BlockBrazier.isEmpty(world.getBlockState(pos))) {
						BlockBrazier.putOutBrazier(world, pos, world.getBlockState(pos));
					}
					else {
						burnTime = MAX_BURN_TIME;
						markDirty();
					}
				}
			}

			if (rand.nextInt(720) == 0) {
				world.playSound(null, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0f + rand.nextFloat(), rand.nextFloat() * 0.7f + 0.3f);
			}
		}
	}

	public void startBurning() {
		burnTime = MAX_BURN_TIME;
		markDirty();
	}

	public void resetBurnTime() {
		burnTime = 0;
		markDirty();
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("burnTime", Constants.NBT.TAG_INT)) {
			burnTime = comp.getInteger("burnTime");
		}

		super.readFromNBT(comp);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setInteger("burnTime", burnTime);

		return super.writeToNBT(comp);
	}
}
