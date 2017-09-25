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

import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.util.WorldHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class TileTimePlant extends TileEntityBase {
	private static final int MIDNIGHT_CHARGE_BONUS = 2; //The amount of bonus charge a bulb gets when harvested at midnight
	private static final int MOON_CHARGE_BONUS = 4; //The amount of bonus charge a bulb gets when harvested during a full moon (other phases are fractions of this)

	private int charge = 0;

	public void increaseCharge(int amount) {
		charge += amount;
		markDirty();
	}

	public ItemStack getHarvestItem(long worldTime) {
		int bulbStrength = 1 + charge;

		if (WorldHelper.isNight(worldTime)) {
			if (WorldHelper.isMidnight(worldTime)) {
				bulbStrength += MIDNIGHT_CHARGE_BONUS;
			}

			bulbStrength += MOON_CHARGE_BONUS * world.getCurrentMoonPhaseFactor();
		}

		ItemStack output = new ItemStack(ModItems.TIME_BULB, 1);
		NBTTagCompound comp = new NBTTagCompound();
		comp.setInteger("amount", bulbStrength);
		output.setTagCompound(comp);

		charge = 0;
		markDirty();
		return output;
	}

	private static final String CHARGE_TAG = "charge";

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setInteger(CHARGE_TAG, charge);

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey(CHARGE_TAG, Constants.NBT.TAG_INT)) {
			charge = comp.getInteger(CHARGE_TAG);
		}

		super.readFromNBT(comp);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public String toString() {
		String out = this.getClass().toString();

		out += " at " + pos + " with charge " + charge;

		return out;
	}
}
