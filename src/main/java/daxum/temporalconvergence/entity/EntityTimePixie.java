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
import java.util.Random;

import daxum.temporalconvergence.LootTables;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.EntityMoveHelper.Action;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityTimePixie extends EntityFlying {

	public EntityTimePixie(World world) {
		super(world);
		setSize(0.3f, 0.3f);
		moveHelper = new MoveHelper(this);
	}

	@Override
	protected void initEntityAI() {
		tasks.addTask(1, new AIFlyRandomly(this));
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public void collideWithEntity(Entity entity) {}

	@Override
	public void collideWithNearbyEntities() {}

	@Override
	public void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2.0);
	}

	@Override
	public boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean doesEntityNotTriggerPressurePlate() {
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public float getEyeHeight() {
		return height / 2.0f;
	}

	@Override
	public boolean canBeLeashedTo(EntityPlayer player) {
		return false;
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LootTables.TIME_PIXIE;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	private static class AIFlyRandomly extends EntityAIBase {
		private final EntityTimePixie parent;

		public AIFlyRandomly(EntityTimePixie ft) {
			parent = ft;
			setMutexBits(1);
		}

		@Override
		public boolean shouldExecute() {
			return !parent.getMoveHelper().isUpdating() && parent.getRNG().nextInt(7) == 0;
		}

		@Override
		public boolean continueExecuting() {
			return parent.getMoveHelper().isUpdating();
		}

		@Override
		public void startExecuting() {
			Random rand = parent.getRNG();

			double x = parent.posX + (rand.nextDouble() * 8 - 4);
			double z = parent.posZ + (rand.nextDouble() * 8 - 4);
			double y = parent.posY + (rand.nextDouble() * 4 - 2);

			if (y > parent.world.getHeight(MathHelper.floor(x), MathHelper.floor(z)) + 4)
				y = y - 3;

			parent.getMoveHelper().setMoveTo(x, y, z, rand.nextDouble() * 0.025 + 0.005);
		}

		@Override
		public void updateTask() {
			double[] expectedMotion = ((MoveHelper) parent.moveHelper).getNextExpectedMotion();

			AxisAlignedBB nextParentBB = parent.getEntityBoundingBox().offset(expectedMotion[0], expectedMotion[1], expectedMotion[2]);
			List<AxisAlignedBB> collisions = getPossibleCollisions(nextParentBB);

			for (int i = 0; i < collisions.size(); i++) {
				if (collisions.get(i).intersectsWith(nextParentBB)) {
					parent.moveHelper.action = Action.WAIT;
					break;
				}
			}
		}

		public List<AxisAlignedBB> getPossibleCollisions(AxisAlignedBB toCheck) {
			List<AxisAlignedBB> out = new ArrayList();

			boolean xDif = Math.floor(toCheck.maxX) != Math.floor(toCheck.minX);
			boolean yDif = Math.floor(toCheck.maxY) != Math.floor(toCheck.minY);
			boolean zDif = Math.floor(toCheck.maxZ) != Math.floor(toCheck.minZ);

			//The below checks at most 8 blocks around the parent entity
			for (int i = 0; i <= (xDif ? 1 : 0); i++) {
				for (int j = 0; j <= (yDif ? 1 : 0); j++) {
					for (int k = 0; k <= (zDif ? 1 : 0); k++) {
						BlockPos pos = new BlockPos(i == 0 ? toCheck.minX : toCheck.maxX, j == 0 ? toCheck.minY : toCheck.maxY, k == 0 ? toCheck.minZ : toCheck.maxZ);
						AxisAlignedBB candidate = parent.world.getBlockState(pos).getCollisionBoundingBox(parent.world, pos);

						if (candidate != null)
							out.add(candidate.offset(pos));
					}
				}
			}

			return out;
		}
	}

	private static class MoveHelper extends EntityMoveHelper {
		public MoveHelper(EntityLiving entitylivingIn) {
			super(entitylivingIn);
		}

		@Override
		public void onUpdateMoveHelper() {
			if (action != EntityMoveHelper.Action.MOVE_TO) return;

			double x = posX - entity.posX;
			double y = posY - entity.posY;
			double z = posZ - entity.posZ;
			double distance = Math.sqrt(x*x + y*y + z*z);

			if (speed > distance || distance < 0.5) {
				action = Action.WAIT;
			}
			else {
				double ratio = speed / distance;

				entity.motionX += x * ratio;
				entity.motionY += y * ratio;
				entity.motionZ += z * ratio;

				float yaw = (float)(MathHelper.atan2(z, x) * (180.0 / Math.PI)) - 90.0f;
				entity.rotationYaw = limitAngle(entity.rotationYaw, yaw, 90.0f);
			}
		}

		public double[] getNextExpectedMotion() {
			double x = posX - entity.posX;
			double y = posY - entity.posY;
			double z = posZ - entity.posZ;
			double distance = Math.sqrt(x*x + y*y + z*z);

			if (speed > distance || distance < 0.5) {
				double[] out = {entity.motionX, entity.motionY, entity.motionZ};
				return out;
			}
			else {
				double ratio = speed / distance;

				double[] out = {entity.motionX + x * ratio,
						entity.motionY + y * ratio,
						entity.motionZ + z * ratio};

				return out;
			}
		}
	}
}
