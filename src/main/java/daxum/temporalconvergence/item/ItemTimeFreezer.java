package daxum.temporalconvergence.item;

import java.util.List;

import daxum.temporalconvergence.entity.EntityFrozen;
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
		if (!isActive(stack)) {
			if (stack.getItemDamage() > 0 && world.getTotalWorldTime() % 2 == 0)
				stack.setItemDamage(stack.getItemDamage() - 1);
			return;
		}

		if (stack.getItemDamage() >= stack.getMaxDamage() - 1) {
			setActive(stack, false);
			return;
		}

		if (entity != null && entity.getEntityBoundingBox() != null) {
			AxisAlignedBB entityBB = entity.getEntityBoundingBox().expandXyz(2.0);
			List<Entity> entities = entity.getEntityWorld().getEntitiesInAABBexcluding(entity, entityBB, null);

			if (entities.isEmpty()) {
				if (stack.getItemDamage() > 0 && world.getTotalWorldTime() % 4 == 0)
					stack.setItemDamage(stack.getItemDamage() - 1);
				return;
			}

			boolean used = false;
			boolean usedBoss = false;

			for (int i = 0; i < entities.size(); i++) {
				Entity current = entities.get(i);

				if (current instanceof EntityFrozen) {
					EntityFrozen frozen = (EntityFrozen) current;
					frozen.reFreeze();

					used = true;

					if (isBossProjectile(frozen.getFrozenEntity()))
						usedBoss = true;
				} //TODO: Add additional through config, optional boss projecile, blacklist
				else if (current instanceof IProjectile || current instanceof EntityFireball || current instanceof EntityShulkerBullet) {
					world.spawnEntity(new EntityFrozen(world, current));

					used = true;

					if (isBossProjectile(current)) //All boss projectiles cause 10x durability loss
						usedBoss = true;
				}
			}

			if (used) {
				stack.setItemDamage(stack.getItemDamage() + 1);

				if (usedBoss) {
					if (stack.getItemDamage() >= stack.getMaxDamage() - 10)
						stack.setItemDamage(stack.getMaxDamage() - 1);
					else
						stack.setItemDamage(stack.getItemDamage() + 9);
				}
			}
			else if (stack.getItemDamage() > 0 && world.getTotalWorldTime() % 4 == 0) {
				stack.setItemDamage(stack.getItemDamage() - 1);
			}
		}
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entity) {
		onUpdate(entity.getEntityItem(), entity.world, entity, 0, true);
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
		return 800;
	}

	@Override
	public int getItemStackLimit() {
		return 1;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean changed) {
		return changed || oldStack.getItem() != newStack.getItem() || isActive(oldStack) != isActive(newStack);
	}

	public boolean isActive(ItemStack stack) {
		return !stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().getBoolean("active");
	}

	public void setActive(ItemStack stack, boolean active) {
		if (!stack.isEmpty() && stack.getItem() == this) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());

			NBTTagCompound comp = stack.getTagCompound();

			if (comp.getBoolean("active") != active)
				comp.setBoolean("active", active);
		}
	}

	//To be expanded once config made
	public boolean isBossProjectile(Entity entity) {
		return entity instanceof EntityDragonFireball || entity instanceof EntityWitherSkull;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (isActive(stack))
			tooltip.add("Activated");
		else
			tooltip.add("Deactivated");
	}
}
