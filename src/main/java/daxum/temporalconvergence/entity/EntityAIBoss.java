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

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.block.BlockAIBossScreen;
import daxum.temporalconvergence.block.BlockAIBossScreen.ScreenState;
import daxum.temporalconvergence.block.ModBlocks;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class EntityAIBoss extends EntityLiving implements IMob {
	private static final int SCREEN_SEARCH_RADIUS = 9;

	private static final DataParameter<Byte> BOSS_STATE = EntityDataManager.createKey(EntityAIBoss.class, DataSerializers.BYTE);
	private static final DataParameter<Integer> SPAWN_TICKS = EntityDataManager.createKey(EntityAIBoss.class, DataSerializers.VARINT);
	private BlockPos initialPos = BlockPos.ORIGIN;
	private List<BlockPos> screenList = new ArrayList();
	private int shieldAmount = 50;

	public EntityAIBoss(World world) {
		super(world);
		setSize(0.5f, 0.5f);
		experienceValue = 75;
		isImmuneToFire = true;
		//moveHelper = new MoveHelper(this);
	}

	@Override
	public void entityInit() {
		super.entityInit();
		dataManager.register(BOSS_STATE, (byte)0);
		dataManager.register(SPAWN_TICKS, 0);
	}

	private BossState getState() {
		return BossState.getStateFromByte(dataManager.get(BOSS_STATE));
	}

	private void setState(BossState state) {
		dataManager.set(BOSS_STATE, state.getIndex());
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingData) {
		initialPos = new BlockPos(MathHelper.floor(posX), MathHelper.floor(posY), MathHelper.floor(posZ));
		dataManager.set(SPAWN_TICKS, 300);
		spawnScreens();
		resetScreenCache();

		return livingData;
	}

	private void spawnScreens() {
		BlockPos posList[] = getPosList();

		for (int i = 0; i < posList.length; i++) {
			setScreenState(posList[i], ScreenState.STATIC, getFacingForScreen(i));
		}
	}

	private EnumFacing getFacingForScreen(int index) {
		if (index >= 0 && index < 4) {
			return EnumFacing.SOUTH;
		}
		else if (index >= 4 && index < 8) {
			return EnumFacing.WEST;
		}
		else if (index >= 8 && index < 12) {
			return EnumFacing.NORTH;
		}
		else if (index >= 12 && index < 16) {
			return EnumFacing.EAST;
		}

		TemporalConvergence.LOGGER.error("getFacingForScreen() called for invalid index {}", index);
		return EnumFacing.NORTH;
	}

	private BlockPos[] getPosList() {
		BlockPos screenPos[] = new BlockPos[16];

		screenPos[0] = initialPos.north(8).east(4);
		screenPos[1] = initialPos.north(8).west(3);
		screenPos[2] = screenPos[0].up(4);
		screenPos[3] = screenPos[1].up(4);
		screenPos[4] = initialPos.east(8).north(3);
		screenPos[5] = initialPos.east(8).south(4);
		screenPos[6] = screenPos[4].up(4);
		screenPos[7] = screenPos[5].up(4);
		screenPos[8] = initialPos.south(8).east(3);
		screenPos[9] = initialPos.south(8).west(4);
		screenPos[10] = screenPos[8].up(4);
		screenPos[11] = screenPos[9].up(4);
		screenPos[12] = initialPos.west(8).south(3);
		screenPos[13] = initialPos.west(8).north(4);
		screenPos[14] = screenPos[12].up(4);
		screenPos[15] = screenPos[13].up(4);

		return screenPos;
	}

	private void resetScreenCache() {
		screenList.clear();

		for (int x = initialPos.getX() - SCREEN_SEARCH_RADIUS; x < initialPos.getX() + SCREEN_SEARCH_RADIUS; x++) {
			for (int y = initialPos.getY() - SCREEN_SEARCH_RADIUS; y < initialPos.getY() + SCREEN_SEARCH_RADIUS; y++) {
				for (int z = initialPos.getZ() - SCREEN_SEARCH_RADIUS; z < initialPos.getZ() + SCREEN_SEARCH_RADIUS; z++) {
					if (world.getBlockState(new BlockPos(x, y, z)).getBlock() == ModBlocks.BOSS_SCREEN) {
						screenList.add(new BlockPos(x, y, z));
					}
				}
			}
		}
	}

	private void setScreenState(BlockPos pos, ScreenState state) {
		if (world.getBlockState(pos).getBlock() == ModBlocks.BOSS_SCREEN) {
			world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockAIBossScreen.STATE, state));
		}
		else {
			world.setBlockState(pos, ModBlocks.BOSS_SCREEN.getDefaultState().withProperty(BlockAIBossScreen.STATE, state));
		}
	}

	private void setScreenState(BlockPos pos, ScreenState state, EnumFacing facing) {
		world.setBlockState(pos, ModBlocks.BOSS_SCREEN.getDefaultState().withProperty(BlockAIBossScreen.STATE, state).withProperty(BlockAIBossScreen.FACING, facing));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300.0);
		getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(12.0);
		getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(20.0);
	}

	@Override
	public boolean isNonBoss() {
		return false;
	}

	public enum BossState {
		SPAWNING(0),
		FLYING(1),
		IN_SCREEN(2),
		VULNERABLE(3),
		DYING(4);

		public static final BossState[] STATES = {SPAWNING, FLYING, IN_SCREEN, VULNERABLE, DYING};
		private final byte index;

		private BossState(int b) {
			index = (byte)b;
		}

		public byte getIndex() {
			return index;
		}

		public static BossState getStateFromByte(byte i) {
			if (i > 0 && i < STATES.length) {
				return STATES[i];
			}

			return FLYING;
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound comp) {
		super.writeEntityToNBT(comp);

		comp.setByte("state", dataManager.get(BOSS_STATE));
		comp.setInteger("shield", shieldAmount);
		comp.setLong("initPos", initialPos.toLong());

		if (dataManager.get(SPAWN_TICKS) > 0) {
			comp.setInteger("spawn", dataManager.get(SPAWN_TICKS));
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound comp) {
		super.readEntityFromNBT(comp);

		if (comp.hasKey("state", Constants.NBT.TAG_BYTE)) {
			dataManager.set(BOSS_STATE, comp.getByte("state"));
		}

		if (comp.hasKey("spawn", Constants.NBT.TAG_INT)) {
			dataManager.set(SPAWN_TICKS, comp.getInteger("spawn"));
		}

		if (comp.hasKey("sheild", Constants.NBT.TAG_INT)) {
			shieldAmount = comp.getInteger("shield");
		}

		if (comp.hasKey("initPos", Constants.NBT.TAG_LONG)) {
			initialPos = BlockPos.fromLong(comp.getLong("initPos"));
		}
	}

	@Override
	public void onKillCommand() {
		setDead();
	}

	@Override
	protected void kill() {
		setDead();
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean isPushedByWater() {
		return false;
	}

	@Override
	public boolean isOnLadder() {
		return false;
	}
	/*


	@Override
	protected void initEntityAI() {
		//State 0
		tasks.addTask(0, new AISpawn());

		//State 1
		tasks.addTask(2, new AIFlyAround());

		//State 3
		tasks.addTask(1, new AIRunAroundHelplessly());

		//Other
		targetTasks.addTask(0, new EntityAIFindEntityNearestPlayer(this));
	}

	@Override
	public void onUpdate() {
		if (!world.isRemote && needsLoadRebind) {
			rebindFromReload();
			needsLoadRebind = false;
		}

		if (getState() == 3) {
			noClip = false;

			for (int i = 0; i < 5; i++)
				TemporalConvergence.proxy.spawnWaterParticle(world, posX + width/2, posY + height, posZ + height / 2, -motionX, 1.0, -motionZ);
		}
		else
			noClip = true;

		super.onUpdate();
	}

	public boolean areWeFriends(EntityBossAI other) {
		return other.getInitialPos().equals(initialPos);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source == DamageSource.ANVIL && rand.nextInt(1000) == 0)
			setHealth(0.0f); //You've been Etho'd!
		else
			amount = Math.min(amount, 15.0f);

		if (getState() == 1) {
			shieldAmount -= MathHelper.floor(amount);

			if (shieldAmount <= 0) {
				shieldAmount = 50;
				setState(3);
				moveHelper.action = Action.WAIT;
			}

			return false;
		}

		return super.attackEntityFrom(source, amount);
	}

	public float getAdjustedDamage(float amount) {
		if (getState() == 3) {
			float healthBeforeSwitch = getHealth() % 50;
			amount = Math.min(amount, healthBeforeSwitch);
		}

		return Math.min(amount, 15.0f);
	}

	public boolean shouldSwitchToSpark() {
		return getHealth() % 50 == 0;
	}

	@Override
	public boolean getIsInvulnerable() {
		return super.getIsInvulnerable() || getState() != 3;
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		return getIsInvulnerable() || isBossInvulnerableTo(source);
	}

	public static boolean isBossInvulnerableTo(DamageSource source) {
		return source.isMagicDamage() || source.isExplosion() || source.isFireDamage() || source == DamageSource.DROWN || source == DamageSource.CACTUS
				|| source == DamageSource.FALL || source == DamageSource.CRAMMING || source == DamageSource.FALLING_BLOCK || source == DamageSource.IN_WALL
				|| source == DamageSource.WITHER;
	}

	@Override
	public void addPotionEffect(PotionEffect potioneffectIn) {} //Immune to potion effects (They can probably still be added via map if really necessary)

	@Override
	public boolean isPotionApplicable(PotionEffect potioneffectIn) { return false; }

	@Override
	protected void despawnEntity() {}

	@Override
	public void fall(float distance, float damageMultiplier) {
		if (getState() == 3)
			super.fall(distance, damageMultiplier);
	}

	@Override
	protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
		if (getState() == 3)
			super.updateFallState(y, onGroundIn, state, pos);
	}

	//Mostly copied from EntityFlying - couldn't extend because it's only flying some of the time
	@Override
	public void moveEntityWithHeading(float strafe, float forward) {
		if (getState() == 3) {
			super.moveEntityWithHeading(strafe, forward);
			return;
		}

		float f = 0.91f;

		if (onGround) {
			f *= world.getBlockState(new BlockPos(MathHelper.floor(posX), MathHelper.floor(getEntityBoundingBox().minY) - 1, MathHelper.floor(posZ))).getBlock().slipperiness;
		}

		float f1 = 0.16277136f / (f * f * f);
		moveRelative(strafe, forward, onGround ? 0.1f * f1 : 0.02f);
		move(MoverType.SELF, motionX, motionY, motionZ);
		motionX *= f;
		motionY *= f;
		motionZ *= f;
	}

	//An interesting strategy
	@Override
	public void setInWeb() {
		if (getState() == 3) {
			isInWeb = true;
			fallDistance = 0.0f;
		}
	}

	public class AISpawn extends EntityAIBase {
		public AISpawn() {
			setMutexBits(7);
		}

		@Override
		public void startExecuting() {
			moveHelper.setMoveTo(posX, posY + 2, posZ, 0.05);
		}

		@Override
		public boolean shouldExecute() {
			return getState() == 0 && dataManager.get(SPAWN_TICKS) > 0;
		}

		@Override
		public void updateTask() {
			dataManager.set(SPAWN_TICKS, dataManager.get(SPAWN_TICKS) - 1);

			if (dataManager.get(SPAWN_TICKS) <= 0) {
				setState(1);
			}
		}
	}

	public class AIFlyAround extends EntityAIBase {
		public AIFlyAround() {
			setMutexBits(1);
		}

		@Override
		public boolean shouldExecute() {
			return getState() == 1;
		}

		@Override
		public void updateTask() {
			if (!moveHelper.isUpdating()) {
				int x = rand.nextInt(MathHelper.floor(searchAABB.maxX - searchAABB.minX)) + MathHelper.floor(searchAABB.minX);
				int y = rand.nextInt(MathHelper.floor(searchAABB.maxY - searchAABB.minY)) + MathHelper.floor(searchAABB.minY);
				int z = rand.nextInt(MathHelper.floor(searchAABB.maxZ - searchAABB.minZ)) + MathHelper.floor(searchAABB.minZ);

				moveHelper.setMoveTo(x, y, z, 0.45);
			}
		}
	}

	public class AIRunAroundHelplessly extends EntityAIBase {
		private int ticksUntilRecovery = 0;

		public AIRunAroundHelplessly() {
			setMutexBits(1);
		}

		@Override
		public boolean shouldExecute() {
			return getState() == 3;
		}

		@Override
		public void resetTask() {
			ticksUntilRecovery = 0;
			moveHelper.action = Action.WAIT;
		}

		@Override
		public void startExecuting() {
			ticksUntilRecovery = rand.nextInt(600) + 600;
		}

		@Override
		public void updateTask() {
			ticksUntilRecovery--;

			if (ticksUntilRecovery <= 0) {
				setState(1);
			}

			if (recentlyHit > 58 || !moveHelper.isUpdating() && rand.nextInt(5) == 0) {
				int x = rand.nextInt(MathHelper.floor(searchAABB.maxX - searchAABB.minX)) + MathHelper.floor(searchAABB.minX);
				int z = rand.nextInt(MathHelper.floor(searchAABB.maxZ - searchAABB.minZ)) + MathHelper.floor(searchAABB.minZ);

				moveHelper.setMoveTo(x, posY, z, 0.5);
			}
		}
	}

	public static class MoveHelper extends EntityMoveHelper {
		private final EntityBossAI entity;
		private int moveTicks = 0;

		public MoveHelper(EntityBossAI ebai) {
			super(ebai);
			entity = ebai;
		}

		@Override
		public void onUpdateMoveHelper() {
			if (!isUpdating()) return;

			moveTicks++;

			double x = posX - entity.posX;
			double y = posY - entity.posY;
			double z = posZ - entity.posZ;
			double distance = Math.sqrt(x*x + entity.getState() == 3 ? 0 : y*y + z*z);

			if (moveTicks > 100 || speed > distance || distance < 0.5) {
				action = Action.WAIT;
				moveTicks = 0;
			}
			else {
				double ratio = speed / distance;

				if (entity.getState() != 3) {
					entity.motionX = x * ratio;
					entity.motionY = y * ratio;
					entity.motionZ = z * ratio;
				}
				else {
					ratio /= 2.0;
					entity.motionX += x * ratio;
					entity.motionZ += z * ratio;
				}

				float yaw = (float)(MathHelper.atan2(z, x) * (180.0 / Math.PI)) - 90.0f;
				entity.rotationYaw = limitAngle(entity.rotationYaw, yaw, 90.0f);
			}
		}
	}*/
}
