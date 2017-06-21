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

import daxum.temporalconvergence.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
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

		if (!world.isRemote && isInValidLocation(item) && world.getTotalWorldTime() % 12 == 0) {
			ItemStack stack = item.getEntityItem();

			incrementCompletePercent(stack);

			if (getCompletePercent(stack) >= 100) {
				Block infusedBlock = isDay(world.getWorldTime()) ? ModBlocks.SOLAR_WOOD : ModBlocks.LUNAR_WOOD;

				item.setEntityItemStack(new ItemStack(infusedBlock, stack.getCount()));
			}
			else {
				item.setEntityItemStack(stack);
			}
		}

		return false;
	}

	private boolean isInValidLocation(EntityItem item) {
		return item.world.getBlockState(item.getPosition()).getBlock() == ModBlocks.TIME_WATER && item.world.provider.isSurfaceWorld() && item.world.canBlockSeeSky(item.getPosition());
	}

	private void incrementCompletePercent(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("complete", Constants.NBT.TAG_INT)) {
			stack.getTagCompound().setInteger("complete", getCompletePercent(stack) + 1);
		}
		else {
			NBTTagCompound stackTag;

			if (stack.hasTagCompound()) {
				stackTag = stack.getTagCompound();
			}
			else {
				stackTag = new NBTTagCompound();
			}

			stackTag.setInteger("complete", 1);
			stack.setTagCompound(stackTag);
		}
	}

	private int getCompletePercent(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("complete", Constants.NBT.TAG_INT)) {
			return stack.getTagCompound().getInteger("complete");
		}

		return 0;
	}

	private boolean isDay(long time) {
		return time >= 0 && time < 13000;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("complete", Constants.NBT.TAG_INT)) {
			tooltip.add("Infused: " + stack.getTagCompound().getInteger("complete") + "%");
		}
		else {
			tooltip.add("Infused: 0%");
		}
	}
}
