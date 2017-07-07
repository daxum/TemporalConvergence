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
package daxum.temporalconvergence.item;

import java.util.List;

import daxum.temporalconvergence.block.BlockFluidTimeWater;
import daxum.temporalconvergence.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemInfusedWood extends ItemBase {
	public ItemInfusedWood() {
		super("infused_wood");
	}

	@Override
	public int getEntityLifespan(ItemStack stack, World world) {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem item) {
		World world = item.world;

		if (!world.isRemote && BlockFluidTimeWater.isInValidLocation(item.world, item.getPosition()) && world.getTotalWorldTime() % 12 == 0) {
			ItemStack stack = item.getItem();

			if (isDay(world.getWorldTime())) {
				incrementSolarPercent(stack);
			}
			else {
				incrementLunarPercent(stack);
			}

			if (getTotalPercent(stack) >= 100) {
				Block result = getSolarPercent(stack) > 50 ? ModBlocks.SOLAR_WOOD : ModBlocks.LUNAR_WOOD;
				int strength = (result == ModBlocks.SOLAR_WOOD ? getSolarPercent(stack) : getLunarPercent(stack)) - 49;

				ItemStack resultStack = new ItemStack(result, stack.getCount());
				resultStack.setTagCompound(new NBTTagCompound());
				resultStack.getTagCompound().setInteger("strength", strength);

				item.setItem(resultStack);
			}
		}

		return false;
	}

	private void incrementSolarPercent(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("solar", Constants.NBT.TAG_INT)) {
			stack.getTagCompound().setInteger("solar", stack.getTagCompound().getInteger("solar") + 1);
		}
		else if (stack.hasTagCompound()) {
			stack.getTagCompound().setInteger("solar", 1);
		}
		else {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("solar", 1);
		}
	}

	private void incrementLunarPercent(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("lunar", Constants.NBT.TAG_INT)) {
			stack.getTagCompound().setInteger("lunar", stack.getTagCompound().getInteger("lunar") + 1);
		}
		else if (stack.hasTagCompound()) {
			stack.getTagCompound().setInteger("lunar", 1);
		}
		else {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("lunar", 1);
		}
	}

	private int getSolarPercent(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("solar", Constants.NBT.TAG_INT)) {
			return stack.getTagCompound().getInteger("solar");
		}

		return 0;
	}

	private int getLunarPercent(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("lunar", Constants.NBT.TAG_INT)) {
			return stack.getTagCompound().getInteger("lunar");
		}

		return 0;
	}

	private int getTotalPercent(ItemStack stack) {
		return getSolarPercent(stack) + getLunarPercent(stack);
	}

	private boolean isDay(long time) {
		return time >= 0 && time < 13000;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add("Infusion Progress: " + getTotalPercent(stack) + "%");
		tooltip.add(" - Solar: " + getSolarPercent(stack) + "%");
		tooltip.add(" - Lunar: " + getLunarPercent(stack) + "%");
	}
}
