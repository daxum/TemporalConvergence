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

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDimensionalLinker extends ItemBase {
	public ItemDimensionalLinker() {
		super("dimensional_linker");
		addPropertyOverride(new ResourceLocation("bound"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, World world, EntityLivingBase entity) {
				return isBound(stack) && !isEmpty(stack) ? 1.0f : 0.0f;
			}
		});
		addPropertyOverride(new ResourceLocation("empty"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, World world, EntityLivingBase entity) {
				return isEmpty(stack) ? 1.0f : 0.0f;
			}
		});
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		if (!world.isRemote && isBound(stack)) {
			/*PowerDimension pd = PowerDimension.get(world, stack.getTagCompound().getInteger("dimid"));

			if (pd != null) {
				stack.getTagCompound().setString("debug", pd.toString());
			}
			else {
				stack.getTagCompound().setString("debug", "Empty dimension");
			}

			stack.getTagCompound().setBoolean("empty", pd == null);*/
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		if (stack.isEmpty() || stack.getItem() != this || world.isRemote)
			return ActionResult.newResult(EnumActionResult.PASS, stack);

		//Clear if sneaking
		if (player.isSneaking() && isBound(stack)) {
			stack.getTagCompound().removeTag("dimid");
			stack.getTagCompound().removeTag("empty");
			return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
		}

		//Bind to other
		ItemStack other = hand == EnumHand.MAIN_HAND ? player.getHeldItem(EnumHand.OFF_HAND) : player.getHeldItem(EnumHand.MAIN_HAND);
		if (!other.isEmpty() && isBound(other)) {
			int id = other.getTagCompound().getInteger("dimid");

			if (stack.hasTagCompound()) {
				if (!stack.getTagCompound().hasKey("dimid")) {
					stack.getTagCompound().setInteger("dimid", id);
					return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
				}
			} else {
				NBTTagCompound comp = new NBTTagCompound();
				comp.setInteger("dimid", id);
				stack.setTagCompound(comp);
				return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
			}
		}

		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}

	public boolean isBound(ItemStack stack) {
		return stack.getItem() == this && stack.hasTagCompound() && stack.getTagCompound().hasKey("dimid");
	}

	public boolean isEmpty(ItemStack stack) {
		return isBound(stack) && stack.getTagCompound().getBoolean("empty");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dimid")) {
			tooltip.add("Set to dimension #" + stack.getTagCompound().getInteger("dimid"));
			if (stack.getTagCompound().hasKey("debug"))
				tooltip.add(stack.getTagCompound().getString("debug"));
		}
		else {
			tooltip.add("No dimension set");
		}
	}
}
