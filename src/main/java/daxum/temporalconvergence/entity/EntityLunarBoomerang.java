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
package daxum.temporalconvergence.entity;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

public class EntityLunarBoomerang extends Entity implements IProjectile {
	private final Predicate<Entity> VALID_TARGETS = Predicates.and(EntitySelectors.IS_ALIVE, EntitySelectors.CAN_AI_TARGET, new Predicate<Entity>() {
		@Override
		public boolean apply(Entity entity)
		{
			return entity == null ? false : entity.canBeCollidedWith() && !alreadyHit.contains(entity);
		}
	});

	private EntityPlayer thrower = null;
	private ItemStack thrownStack = ItemStack.EMPTY;
	private Entity target = null;
	private List<Entity> alreadyHit = new ArrayList<>();
	private float damage = 3.0f;
	private int ticksInAir = 0;
	private int bounces = 5;

	public EntityLunarBoomerang(World world) {
		super(world);
		setSize(1.0f, 0.5f);
	}

	public EntityLunarBoomerang(World world, EntityPlayer user, ItemStack stack, float damageAmount, int hits) {
		this(world);
		setPosition(user.posX, user.posY + user.getEyeHeight() - 0.1, user.posZ);
		thrower = user;
		thrownStack = stack;
		damage = damageAmount;
		bounces = hits;
	}

	@Override
	protected void entityInit() {}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!world.isRemote) {
			ticksInAir++;

			Vec3d start = new Vec3d(posX, posY, posZ);
			Vec3d end = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
			RayTraceResult rtr = world.rayTraceBlocks(start, end, false, true, false);

			if (rtr != null) {
				end = rtr.hitVec;
			}

			Entity entity = findEntityOnPath(start, end);

			if (entity != null) {
				rtr = new RayTraceResult(entity);
			}

			if (rtr != null && rtr.entityHit instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer)rtr.entityHit;

				if (thrower != null && !thrower.canAttackPlayer(player)) {
					rtr = null;
				}
			}

			if (rtr != null) {
				onHit(rtr);
			}

			if (target != null && target.isEntityAlive()) {
				setThrowableHeading(target.posX - posX, adjustTargetHeight(target.posY, target.height) - posY, target.posZ - posZ, (float)getVelocity(), 0.0f);
			}
		}

		posX += motionX;
		posY += motionY;
		posZ += motionZ;

		double drag = 0.99;

		if (isInWater()) {
			for (int i = 0; i < 4; i++) {
				world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * 0.25, posY - motionY * 0.25, posZ - motionZ * 0.25, motionX, motionY, motionZ);
			}

			drag = 0.6;
		}

		if (isWet()) {
			extinguish();
		}

		motionX *= drag;
		motionY *= drag;
		motionZ *= drag;

		if (!hasNoGravity()) {
			motionY -= 0.01;
		}

		setPosition(posX, posY, posZ);
		doBlockCollisions();
	}

	public void setAim(Entity shooter, double pitch, double yaw, float velocity) {
		double x = -Math.sin(yaw) * Math.cos(pitch);
		double y = -Math.sin(pitch);
		double z = Math.cos(yaw) * Math.cos(pitch);

		setThrowableHeading(x, y, z, velocity, 0.0f);

		motionX += shooter.motionX;
		motionZ += shooter.motionZ;

		if (!shooter.onGround) {
			motionY += shooter.motionY;
		}
	}

	@Override
	public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
		double distance = Math.sqrt(x * x + y * y + z * z);
		x = x / distance * velocity;
		y = y / distance * velocity;
		z = z / distance * velocity;

		motionX = x;
		motionY = y;
		motionZ = z;
	}

	private Entity findEntityOnPath(Vec3d start, Vec3d end) {
		Entity collidingEntity = null;
		List<Entity> list = world.getEntitiesInAABBexcluding(this, getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(1.0), VALID_TARGETS);

		double minDistance = Double.POSITIVE_INFINITY;
		for (int i = 0; i < list.size(); i++) {
			Entity entity = list.get(i);

			if (entity != thrower || ticksInAir >= 5) {
				AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(0.3);
				RayTraceResult raytraceresult = aabb.calculateIntercept(start, end);

				if (raytraceresult != null) {
					double entityDistance = start.squareDistanceTo(raytraceresult.hitVec);

					if (entityDistance < minDistance) {
						collidingEntity = entity;
						minDistance = entityDistance;
					}
				}
			}
		}

		return collidingEntity;
	}

	private void onHit(RayTraceResult rayTrace) {
		if (rayTrace.typeOfHit == RayTraceResult.Type.ENTITY) {
			final Entity entity = rayTrace.entityHit;

			if (entity != thrower) {
				bounces--;

				if (isBurning() && !(entity instanceof EntityEnderman)) {
					entity.setFire(5);
				}

				final double velocity = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);

				float modifier = 0.0f;

				if (entity instanceof EntityLivingBase) {
					modifier = EnchantmentHelper.getModifierForCreature(thrownStack, ((EntityLivingBase)entity).getCreatureAttribute());
				}
				else {
					modifier = EnchantmentHelper.getModifierForCreature(thrownStack, EnumCreatureAttribute.UNDEFINED);
				}

				double adjustedDamage = velocity * damage + modifier;
				if (entity.attackEntityFrom(new EntityDamageSourceIndirect("boomerang", this, thrower), MathHelper.ceil(adjustedDamage))) {
					if (thrownStack.attemptDamageItem(1, rand, null)) {
						//TODO: Spawn breaking particles?
						setDead();
					}
				}

				alreadyHit.add(entity);

				if (bounces > 0) {
					target = findNextTarget();

					if (target != null) {
						setThrowableHeading(target.posX - posX, adjustTargetHeight(target.posY, target.height) - posY, target.posZ - posZ, (float)getVelocity(), 0.0f);
					}
					else {
						returnToThrower();
					}
				}
				else {
					returnToThrower();
				}
			}
			else {
				returnToThrower();
			}
		}
		else if (rayTrace.typeOfHit == RayTraceResult.Type.BLOCK) {
			returnToThrower();
		}
	}

	private void returnToThrower() {
		setDead();

		if (!thrownStack.isEmpty()) {
			if (thrower != null) {
				ItemHandlerHelper.giveItemToPlayer(thrower, thrownStack);
			}
			else {
				world.spawnEntity(new EntityItem(world, posX, posY, posZ, thrownStack));
			}
		}

		//TODO: particles
	}

	private Entity findNextTarget() {
		List<Entity> entityList = world.getEntitiesInAABBexcluding(this, getEntityBoundingBox().grow(10.0), VALID_TARGETS);
		Entity nextTarget = null;
		Vec3d posVec = new Vec3d(posX, posY, posZ);

		double minDistance = Double.POSITIVE_INFINITY;
		for (int i = 0; i < entityList.size(); i++) {
			Entity entity = entityList.get(i);

			if (entity != thrower) {
				Vec3d entityVec = new Vec3d(entity.posX, adjustTargetHeight(entity.posY, entity.height), entity.posZ);

				RayTraceResult rtr = world.rayTraceBlocks(posVec, entityVec, false, true, false);

				if (rtr == null) {
					double entityDistance = posVec.squareDistanceTo(entity.posX, entity.posY, entity.posZ);

					if (entityDistance < minDistance) {
						nextTarget = entity;
						minDistance = entityDistance;
					}
				}
			}
		}

		return nextTarget;
	}

	private double getVelocity() {
		return Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
	}

	private double adjustTargetHeight(double original, double height) {
		return Math.max(Math.min(posY, original + height), original);
	}

	private static final String THROWER_TAG = "thrower";
	private static final String STACK_TAG = "stack";
	private static final String DAMAGE_TAG = "damage";
	private static final String HITS_TAG = "bounces";

	@Override
	protected void readEntityFromNBT(NBTTagCompound comp) {
		if (comp.hasUniqueId(THROWER_TAG)) {
			thrower = world.getPlayerEntityByUUID(comp.getUniqueId(THROWER_TAG));
		}

		if (comp.hasKey(STACK_TAG, Constants.NBT.TAG_COMPOUND)) {
			thrownStack = new ItemStack(comp.getCompoundTag(STACK_TAG));
		}

		if (comp.hasKey(DAMAGE_TAG, Constants.NBT.TAG_FLOAT)) {
			damage = comp.getFloat(DAMAGE_TAG);
		}

		if (comp.hasKey(HITS_TAG, Constants.NBT.TAG_INT)) {
			bounces = comp.getInteger(HITS_TAG);
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound comp) {
		if (thrower != null) {
			comp.setUniqueId(THROWER_TAG, thrower.getPersistentID());
		}

		comp.setTag(STACK_TAG, thrownStack.writeToNBT(new NBTTagCompound()));
		comp.setFloat(DAMAGE_TAG, damage);
		comp.setInteger(HITS_TAG, bounces);
	}

	@Override
	public boolean canBeAttackedWithItem() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		double edgeSize = getEntityBoundingBox().getAverageEdgeLength() * 10.0;

		if (Double.isNaN(edgeSize)) {
			edgeSize = 1.0;
		}

		edgeSize = edgeSize * 64.0 * getRenderDistanceWeight();
		return distance < edgeSize * edgeSize;
	}
}
