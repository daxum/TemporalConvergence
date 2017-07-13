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
import daxum.temporalconvergence.block.BlockTimePlant.PlantState;
import daxum.temporalconvergence.block.ModBlocks;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.util.WorldHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class TileTimePlant extends TileEntityBase implements ITickable {
	private static final int MAX_SAFE_INSTABILITY = 5000; //If instability rises above this, bad things start to happen
	private static final int INSTABILITY_INCREASE = 3000; //The amount the instability increases when a plant is bonemealed
	private static final int MAX_WITHER_TIME = 24000; //The time the plant will stay withered when it looses its bulb (such as when it is sheared)
	private static final int TWILIGHT_CHARGE_INCREASE = 3; //The amount of charge the plant gains when bonemealed at dawn or dusk
	private static final int NORMAL_CHARGE_INCREASE = 2; //The amount of charge the plant gains when bonemealed during the day, but not at a special time
	private static final int NOON_CHARGE_INCREASE = 1; //The amount of charge the plant gains when bonemealed at noon
	private static final int INSTABILITY_SPREAD_DISTANCE = 2; //The distance instability spreads to other time plants when this one is bonemealed
	private static final int MIDNIGHT_CHARGE_BONUS = 2; //The amount of bonus charge a bulb gets when harvested at midnight
	private static final int MOON_CHARGE_BONUS = 4; //The amount of bonus charge a bulb gets when harvested during a full moon (other phases are fractions of this)

	private final Random rand = new Random();
	private int charge = 0;
	private int instability = 0;
	private int witherTimer = 0;

	@Override
	public void update() {
		updateInstability();

		if (witherTimer > 0) {
			witherTimer--;
			markDirty();
		}

		if (world.getWorldTime() == 0 && charge > 0) {
			charge = charge / 2;
			markDirty();
		}

		updateBlockState();
	}

	public void setWitherTimer() {
		witherTimer = MAX_WITHER_TIME;
	}

	public void onGrowthAccelerated(long time) {
		if (WorldHelper.isDay(time)) {
			if (WorldHelper.isDawn(time) || WorldHelper.isDusk(time)) {
				charge += TWILIGHT_CHARGE_INCREASE;
			}
			else if (WorldHelper.isNoon(time)) {
				charge += NOON_CHARGE_INCREASE;
			}
			else {
				charge += NORMAL_CHARGE_INCREASE;
			}

			instability += INSTABILITY_INCREASE;
			distributeInstability(INSTABILITY_INCREASE);
			markDirty();
		}
	}

	public ItemStack getShearedItem(long worldTime) {
		if (!isWithered()) {
			setWitherTimer();
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

					if (world.getBlockState(checkPos).getBlock() == ModBlocks.TIME_PLANT && world.getTileEntity(checkPos) instanceof TileTimePlant) {
						((TileTimePlant)world.getTileEntity(checkPos)).increaseInstability(amount / 2);
					}
				}
			}
		}
	}

	private void updateBlockState() {
		PlantState currentState = world.getBlockState(pos).getValue(BlockTimePlant.PLANT_STATE);

		if (isWithered()) {
			if (currentState != PlantState.WITHERED) {
				world.setBlockState(pos, ModBlocks.TIME_PLANT.getDefaultState().withProperty(BlockTimePlant.PLANT_STATE, PlantState.WITHERED));
			}
		}
		else if (WorldHelper.isNight(world.getWorldTime())) {
			if (currentState != PlantState.NIGHTTIME) {
				world.setBlockState(pos, ModBlocks.TIME_PLANT.getDefaultState().withProperty(BlockTimePlant.PLANT_STATE, PlantState.NIGHTTIME));
			}
		}
		else if (currentState != PlantState.DAYTIME) {
			world.setBlockState(pos, ModBlocks.TIME_PLANT.getDefaultState().withProperty(BlockTimePlant.PLANT_STATE, PlantState.DAYTIME));
		}
	}

	private void updateInstability() {
		if (instability > MAX_SAFE_INSTABILITY) {
			if (charge > 0 && instability - MAX_SAFE_INSTABILITY + rand.nextInt(1000) > 1000) {
				charge--;
			}

			if (instability - MAX_SAFE_INSTABILITY > 1000 && rand.nextInt(instability) > MAX_SAFE_INSTABILITY) {
				charge = 0;
				setWitherTimer();
			}
		}

		if (instability > 0) {
			instability--;
			markDirty();
		}
	}

	private boolean isWithered() {
		return witherTimer > 0;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setInteger("charge", charge);
		comp.setInteger("instability", instability);
		comp.setInteger("regrowthTimer", witherTimer);

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("charge", Constants.NBT.TAG_INT)) {
			charge = comp.getInteger("charge");
		}

		if (comp.hasKey("instability", Constants.NBT.TAG_INT)) {
			instability = comp.getInteger("instability");
		}

		if (comp.hasKey("regrowthTimer", Constants.NBT.TAG_INT)) {
			witherTimer = comp.getInteger("regrowthTimer");
		}

		super.readFromNBT(comp);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}
}
