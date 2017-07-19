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

import daxum.temporalconvergence.block.BlockTimePlant;
import daxum.temporalconvergence.block.ModBlocks;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.util.WorldHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class TileTimePlant extends TileEntityBase {
	private static final int MAX_SAFE_INSTABILITY = 5; //If instability rises above this, bad things start to happen
	private static final int MAX_INSTABILITY = 10; //If instability rises above this, the plant becomes withered and loses all charge
	private static final int INSTABILITY_INCREASE = 2; //The amount the instability increases when a plant is bonemealed
	private static final int MIN_WITHER_TIME = 12000; //The minimum amount of time a plant will stay withered once it loses its bulb
	private static final int MAX_WITHER_TIME = 24000; //The maximum amount of time a plant will stay withered
	private static final int MAX_PASSIVE_CHARGE = 3; //The maximum amount of charge a plant will gain on its own
	private static final int TWILIGHT_CHARGE_INCREASE = 3; //The amount of charge the plant gains when bonemealed at dawn or dusk
	private static final int NORMAL_CHARGE_INCREASE = 2; //The amount of charge the plant gains when bonemealed during the day, but not at a special time
	private static final int NOON_CHARGE_INCREASE = 1; //The amount of charge the plant gains when bonemealed at noon
	private static final int INSTABILITY_SPREAD_DISTANCE = 2; //The distance instability spreads to other time plants when this one is bonemealed
	private static final int MIDNIGHT_CHARGE_BONUS = 2; //The amount of bonus charge a bulb gets when harvested at midnight
	private static final int MOON_CHARGE_BONUS = 4; //The amount of bonus charge a bulb gets when harvested during a full moon (other phases are fractions of this)

	private final Random rand = new Random();
	private int charge = 0;
	private int instability = 0;
	private long witherRecoveryTime = 0;
	private boolean withered = false;

	public void onRandomTick() {
		if (!withered && charge < MAX_PASSIVE_CHARGE) {
			charge++;
			markDirty();
		}

		updateWitherState();
		updateInstability();
	}

	public void setWithered() {
		if (world.getBlockState(pos).getBlock() instanceof BlockTimePlant) {
			world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockTimePlant.WITHERED, true));
		}

		withered = true;
		witherRecoveryTime = world.getTotalWorldTime() + MIN_WITHER_TIME + rand.nextInt(MAX_WITHER_TIME - MIN_WITHER_TIME);
		charge = 0;
		markDirty();
	}

	public void onGrowthAccelerated(long time) {
		if (!withered) {
			if (WorldHelper.isDawn(time) || WorldHelper.isDusk(time)) {
				charge += TWILIGHT_CHARGE_INCREASE;
			}
			else if (WorldHelper.isNoon(time)) {
				charge += NOON_CHARGE_INCREASE;
			}
			else {
				charge += NORMAL_CHARGE_INCREASE;
			}

			instability += INSTABILITY_INCREASE + charge / 5;
			distributeInstability(INSTABILITY_INCREASE);
			updateWitherState();
			markDirty();
		}
	}

	public ItemStack getShearedItem(long worldTime) {
		if (!withered) {
			setWithered();
			ItemStack output = ItemStack.EMPTY;

			int bulbStrength = 1 + MathHelper.ceil(charge / 2.0);

			if (WorldHelper.isNight(worldTime)) {
				bulbStrength *= 2;

				if (WorldHelper.isMidnight(worldTime)) {
					bulbStrength += MIDNIGHT_CHARGE_BONUS;
				}

				bulbStrength += MOON_CHARGE_BONUS * world.getCurrentMoonPhaseFactor();
			}

			output = new ItemStack(ModItems.TIME_BULB, 1);
			NBTTagCompound comp = new NBTTagCompound();
			comp.setInteger("amount", bulbStrength);
			output.setTagCompound(comp);

			charge = 0;
			markDirty();
			return output;
		}
		else {
			return ItemStack.EMPTY;
		}
	}

	public void increaseInstability(int amount) {
		instability += amount;
		markDirty();
	}

	private void distributeInstability(int amount) {
		for (int x = pos.getX() - INSTABILITY_SPREAD_DISTANCE; x <= pos.getX() + INSTABILITY_SPREAD_DISTANCE; x++) {
			for (int y = pos.getY() - INSTABILITY_SPREAD_DISTANCE * 2; y <= pos.getY() + INSTABILITY_SPREAD_DISTANCE * 2; y++) {
				for (int z = pos.getZ() - INSTABILITY_SPREAD_DISTANCE; z <= pos.getZ() + INSTABILITY_SPREAD_DISTANCE; z++) {
					BlockPos checkPos = new BlockPos(x, y, z);

					if (!checkPos.equals(pos) && world.getBlockState(checkPos).getBlock() == ModBlocks.TIME_PLANT && world.getTileEntity(checkPos) instanceof TileTimePlant) {
						((TileTimePlant)world.getTileEntity(checkPos)).increaseInstability(amount / 2);
					}
				}
			}
		}
	}

	private void updateInstability() {
		if (instability > 0) {
			if (instability > MAX_SAFE_INSTABILITY && rand.nextBoolean() && charge > 0) {
				charge--;
			}

			instability--;
			markDirty();
		}
	}

	private void updateWitherState() {
		if (withered) {
			if (world.getTotalWorldTime() > witherRecoveryTime) {
				withered = false;
				witherRecoveryTime = 0;

				IBlockState state = world.getBlockState(pos);

				if (state.getBlock() instanceof BlockTimePlant) {
					world.setBlockState(pos, state.withProperty(BlockTimePlant.WITHERED, false));
				}

				markDirty();
			}
		}
		else {
			if (instability > MAX_SAFE_INSTABILITY && (instability > MAX_INSTABILITY || rand.nextInt(MAX_INSTABILITY - MAX_SAFE_INSTABILITY) < instability - MAX_SAFE_INSTABILITY)) {
				setWithered();
			}
		}
	}

	private static final String CHARGE_TAG = "charge";
	private static final String INSTABILITY_TAG = "instability";
	private static final String WITHER_TAG = "witherTime";

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setInteger(CHARGE_TAG, charge);
		comp.setInteger(INSTABILITY_TAG, instability);

		if (withered) {
			comp.setLong(WITHER_TAG, witherRecoveryTime);
		}

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey(CHARGE_TAG, Constants.NBT.TAG_INT)) {
			charge = comp.getInteger(CHARGE_TAG);
		}

		if (comp.hasKey(INSTABILITY_TAG, Constants.NBT.TAG_INT)) {
			instability = comp.getInteger(INSTABILITY_TAG);
		}

		if (comp.hasKey(WITHER_TAG, Constants.NBT.TAG_LONG)) {
			withered = true;
			witherRecoveryTime = comp.getLong(WITHER_TAG);
		}
		else {
			withered = false;
			witherRecoveryTime = 0;
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

		out += " at " + pos + ". Variables: [";
		out += "charge = " + charge + ", ";
		out += "instability = " + instability + ", ";
		out += "withered = " + withered + " (End time: " + witherRecoveryTime + ")]";

		return out;
	}
}
