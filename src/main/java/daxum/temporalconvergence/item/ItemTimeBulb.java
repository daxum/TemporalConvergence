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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemTimeBulb extends ItemBase {
	public ItemTimeBulb() {
		super("time_bulb");
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		if (stack.getItem() == this && canDropItem(stack)) {
			player.setHeldItem(hand, ItemStack.EMPTY);
			ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(ModItems.TIME_DUST, getAmountToDrop(stack)));

			return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
		}

		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}

	private boolean canDropItem(ItemStack stack) {
		return stack.getMetadata() == 1;
	}

	private int getAmountToDrop(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("amount", Constants.NBT.TAG_INT)) {
			return Math.max(stack.getTagCompound().getInteger("amount"), 1);
		}

		return 1;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (canDropItem(stack)) {
			return getUnlocalizedName();
		}
		else {
			return getUnlocalizedName() + "_empty";
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		if (canDropItem(stack)) {
			tooltip.add("Strength: " + getAmountToDrop(stack));
		}
	}
}
