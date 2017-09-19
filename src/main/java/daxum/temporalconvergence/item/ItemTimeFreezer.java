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

import daxum.temporalconvergence.entity.EntityFrozen;
import daxum.temporalconvergence.power.PowerHandler;
import daxum.temporalconvergence.power.PowerTypeList;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTimeFreezer extends ItemBase {
	public ItemTimeFreezer() {
		super("time_freezer");
		addPropertyOverride(new ResourceLocation("active"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, World world, EntityLivingBase entity) {
				return isActive(stack) ? 1.0F : 0.0F;
			}
		});
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (world.isRemote) return;

		if (!isActive(stack)) {
			if (stack.getItemDamage() > 0) {
				rechargeFreezer(world, entity, stack);
			}

			return;
		}

		if (stack.getItemDamage() >= stack.getMaxDamage() - 1) {
			setActive(stack, false);
			return;
		}

		if (entity != null && entity.getEntityBoundingBox() != null) {
			AxisAlignedBB entityBB = entity.getEntityBoundingBox().grow(2.0);
			List<Entity> entities = entity.getEntityWorld().getEntitiesInAABBexcluding(entity, entityBB, null);

			if (entities.isEmpty()) {
				if (stack.getItemDamage() > 0 && world.getTotalWorldTime() % 4 == 0) {
					rechargeFreezer(world, entity, stack);
				}

				return;
			}

			int damage = 0;
			//All boss projectiles cause extra durability loss
			final int bossDamageIncrease = 10;

			for (int i = 0; i < entities.size(); i++) {
				Entity current = entities.get(i);

				if (current instanceof EntityFrozen) {
					EntityFrozen frozen = (EntityFrozen) current;
					frozen.reFreeze();

					if (isBossProjectile(frozen.getFrozenEntity())) {
						damage += bossDamageIncrease;
					}
					else {
						damage++;
					}
				}
				//TODO: Add additional through config, optional boss projectile, blacklist
				else if (current instanceof IProjectile || current instanceof EntityFireball || current instanceof EntityShulkerBullet) {
					world.spawnEntity(new EntityFrozen(world, current));

					if (isBossProjectile(current)) {
						damage += bossDamageIncrease;
					}
					else {
						damage++;
					}
				}
			}

			if (damage > 0) {
				if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isCreative()) {
					damageStack(stack, damage);
				}
			}
			else if (stack.getItemDamage() > 0 && world.getTotalWorldTime() % 4 == 0) {
				rechargeFreezer(world, entity, stack);
			}
		}
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entity) {
		boolean active = isActive(entity.getItem());

		onUpdate(entity.getItem(), entity.world, entity, 0, true);

		if (isActive(entity.getItem()) != active) {
			//Refresh the item so the texture changes
			entity.setItem(entity.getItem());
		}

		return false;
	}

	@Override
	public int getEntityLifespan(ItemStack stack, World world) {
		return Integer.MAX_VALUE;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		setActive(stack, !isActive(stack));
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public int getMaxDamage() {
		return 1500;
	}

	@Override
	public int getItemStackLimit() {
		return 1;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean changed) {
		return changed || oldStack.getItem() != newStack.getItem() || isActive(oldStack) != isActive(newStack);
	}

	private boolean isActive(ItemStack stack) {
		return !stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().getBoolean("active");
	}

	private void setActive(ItemStack stack, boolean active) {
		if (!stack.isEmpty() && stack.getItem() == this) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());

			NBTTagCompound comp = stack.getTagCompound();

			if (comp.getBoolean("active") != active)
				comp.setBoolean("active", active);
		}
	}

	//To be expanded once config made
	private boolean isBossProjectile(Entity entity) {
		return entity instanceof EntityDragonFireball || entity instanceof EntityWitherSkull;
	}

	private void rechargeFreezer(World world, Entity entity, ItemStack stack) {
		if (entity != null && entity.getEntityBoundingBox() != null) {
			stack.setItemDamage(stack.getItemDamage() - PowerHandler.requestPower(world, entity.getEntityBoundingBox(), PowerTypeList.TIME, 5));
		}
	}

	private void damageStack(ItemStack stack, int amount) {
		if (amount + stack.getItemDamage() >= stack.getMaxDamage() - 1) {
			stack.setItemDamage(stack.getMaxDamage() - 1);
		}
		else {
			stack.setItemDamage(stack.getItemDamage() + amount);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		if (isActive(stack))
			tooltip.add("Activated");
		else
			tooltip.add("Deactivated");
	}
}
