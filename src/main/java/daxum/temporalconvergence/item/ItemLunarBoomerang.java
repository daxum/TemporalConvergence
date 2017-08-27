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

import daxum.temporalconvergence.entity.EntityLunarBoomerang;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemLunarBoomerang extends ItemBase {
	private static final int MAX_USAGE_DURATION = 76000;

	public ItemLunarBoomerang() {
		super("lunar_boomerang");
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {
		float velocity = ItemBow.getArrowVelocity(timeLeft);

		if (!world.isRemote && velocity > 0.1f) {
			EntityPlayer user = null;

			if (entity instanceof EntityPlayer) {
				user = (EntityPlayer)entity;
			}

			int additionalHits = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);

			EntityLunarBoomerang boomerang = new EntityLunarBoomerang(world, user, stack.copy(), 3.0f, 5 + additionalHits);
			boomerang.setAim(user, user.rotationPitch * (Math.PI / 180.0), user.rotationYaw * (Math.PI / 180.0), velocity * 2.0f);

			world.spawnEntity(boomerang);
			stack.setCount(0);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		player.setActiveHand(hand);
		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return MAX_USAGE_DURATION;
	}

	@Override
	public int getMaxDamage() {
		return 1000;
	}

	@Override
	public int getItemStackLimit() {
		return 1;
	}

	@Override
	public int getItemEnchantability() {
		return 2;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment == Enchantment.getEnchantmentByLocation("sharpness")
				|| enchantment == Enchantment.getEnchantmentByLocation("smite")
				|| enchantment == Enchantment.getEnchantmentByLocation("bane_of_arthropods")
				|| enchantment == Enchantment.getEnchantmentByLocation("unbreaking")
				|| enchantment == Enchantment.getEnchantmentByLocation("mending")
				|| enchantment == Enchantment.getEnchantmentByLocation("power");
	}
}
