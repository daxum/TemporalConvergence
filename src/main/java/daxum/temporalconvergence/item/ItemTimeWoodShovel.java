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

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTimeWoodShovel extends ItemSpade {
	public ItemTimeWoodShovel() {
		super(ModItems.TIMEWOOD);
		setCreativeTab(ModItems.TEMPCONVTAB);
		setUnlocalizedName("time_wood_shovel");
		setRegistryName("time_wood_shovel");
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		if (!world.isRemote && stack.getItem() == this && world.getTotalWorldTime() % 5 == 0)
			stack.damageItem(1, (EntityLivingBase) entity);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase user) {
		return true;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean changed) {
		return changed || !(oldStack.getItem() == newStack.getItem());
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!player.canPlayerEdit(pos.offset(facing), facing, player.getHeldItem(hand)))
			return EnumActionResult.FAIL;

		if (facing != EnumFacing.DOWN && world.getBlockState(pos.up()).getMaterial() == Material.AIR && world.getBlockState(pos).getBlock() == Blocks.GRASS) {
			world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);

			if (!world.isRemote)
				world.setBlockState(pos, Blocks.GRASS_PATH.getDefaultState(), 11);

			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
}
