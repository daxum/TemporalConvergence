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
import daxum.temporalconvergence.pathfinding.PathNavigateFlyer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.EntityMoveHelper.Action;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class EntityAIBoss extends EntityMob {
	private static final int SCREEN_SEARCH_RADIUS = 9;
	private static final int MAX_SHIELD_AMOUNT = 1; //TODO: put back to 60 when done testing
	private static final int SPAWN_TIME = 100;
	private static final int MAX_HOME_DISTANCE = 8;
	private static final int BOSS_ROOM_RADIUS = 8;
	private static final int BOSS_ROOM_WIDTH = 17;
	private static final int BOSS_ROOM_DEPTH = 17;
	private static final int BOSS_ROOM_HEIGHT = 8;

	private static final DataParameter<Byte> BOSS_STATE = EntityDataManager.createKey(EntityAIBoss.class, DataSerializers.BYTE);
	private static final DataParameter<Integer> SPAWN_TICKS = EntityDataManager.createKey(EntityAIBoss.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> SHIELD_AMOUNT = EntityDataManager.createKey(EntityAIBoss.class, DataSerializers.VARINT);
	private List<BlockPos> screenList = new ArrayList();
	private float movementPitch = 0.0f; //Can't use rotationPitch because lookHelper constantly screws with it. This is in radians.
	private EntityMoveHelper groundMoveHelper; //The movehelper used in the vulnerable state
	private PathNavigate groundNavigator;
	private EntityMoveHelper airMoveHelper; //The movehelper used when flying
	private PathNavigate airNavigator;

	public EntityAIBoss(World world) {
		super(world);
		setSize(0.5f, 0.5f);
		experienceValue = 75;
		isImmuneToFire = true;
		noClip = true;
		groundMoveHelper = moveHelper;
		airMoveHelper = new AIBossMoveHelper(this);
		moveHelper = airMoveHelper;
		groundNavigator = navigator;
		airNavigator = new PathNavigateFlyer(this, world);
		navigator = airNavigator;
	}

	@Override
	public void entityInit() {
		super.entityInit();
		dataManager.register(BOSS_STATE, (byte)0);
		dataManager.register(SPAWN_TICKS, SPAWN_TIME);
		dataManager.register(SHIELD_AMOUNT, MAX_SHIELD_AMOUNT);
	}

	private BossState getState() {
		return BossState.getStateFromByte(dataManager.get(BOSS_STATE));
	}

	private void setState(BossState state) {
		dataManager.set(BOSS_STATE, state.getIndex());
	}

	private void transitionToState(BossState state) {
		TemporalConvergence.LOGGER.info("Switching to state {}", state);
		setState(state);
		navigator.clearPathEntity();
		moveHelper.action = Action.WAIT;

		if (state == BossState.VULNERABLE) {
			noClip = false;
			isImmuneToFire = false;
			movementPitch = 0.0f;
			moveHelper = groundMoveHelper;
			navigator = groundNavigator;
		}
		else {
			noClip = true;
			isImmuneToFire = true;
			moveHelper = airMoveHelper;
			navigator = airNavigator;
			isJumping = false;
		}

		//TODO: Set up states
	}

	private int getShield() {
		return dataManager.get(SHIELD_AMOUNT);
	}

	private void setShield(int amount) {
		dataManager.set(SHIELD_AMOUNT, amount);
	}

	private void damageShield(int amount) {
		if (amount > 0) {
			setShield(Math.max(getShield() - amount, 0));
		}
		else {
			setShield(Math.min(getShield() - amount, MAX_SHIELD_AMOUNT));
		}
	}

	private void healShield(int amount) {
		damageShield(-amount);
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingData) {
		setHomePosAndDistance(new BlockPos(this), MAX_HOME_DISTANCE);
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

		screenPos[0] = getHomePosition().north(8).east(4);
		screenPos[1] = getHomePosition().north(8).west(3);
		screenPos[2] = screenPos[0].up(4);
		screenPos[3] = screenPos[1].up(4);
		screenPos[4] = getHomePosition().east(8).north(3);
		screenPos[5] = getHomePosition().east(8).south(4);
		screenPos[6] = screenPos[4].up(4);
		screenPos[7] = screenPos[5].up(4);
		screenPos[8] = getHomePosition().south(8).east(3);
		screenPos[9] = getHomePosition().south(8).west(4);
		screenPos[10] = screenPos[8].up(4);
		screenPos[11] = screenPos[9].up(4);
		screenPos[12] = getHomePosition().west(8).south(3);
		screenPos[13] = getHomePosition().west(8).north(4);
		screenPos[14] = screenPos[12].up(4);
		screenPos[15] = screenPos[13].up(4);

		return screenPos;
	}

	private void resetScreenCache() {
		screenList.clear();

		for (int x = getHomePosition().getX() - SCREEN_SEARCH_RADIUS; x < getHomePosition().getX() + SCREEN_SEARCH_RADIUS; x++) {
			for (int y = getHomePosition().getY() - SCREEN_SEARCH_RADIUS; y < getHomePosition().getY() + SCREEN_SEARCH_RADIUS; y++) {
				for (int z = getHomePosition().getZ() - SCREEN_SEARCH_RADIUS; z < getHomePosition().getZ() + SCREEN_SEARCH_RADIUS; z++) {
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
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.7);

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
			if (i >= 0 && i < STATES.length) {
				return STATES[i];
			}

			return FLYING;
		}

		public boolean isVulnerable() {
			return this == VULNERABLE;
		}

		public boolean isUsingShield() {
			return this == FLYING || this == IN_SCREEN;
		}

		public boolean isInvulnerable() {
			return this == SPAWNING || this == DYING;
		}

		public boolean isFlying() {
			return this != IN_SCREEN && this != VULNERABLE;
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound comp) {
		super.writeEntityToNBT(comp);

		comp.setByte("state", dataManager.get(BOSS_STATE));
		comp.setInteger("shield", dataManager.get(SHIELD_AMOUNT));
		comp.setLong("initPos", getHomePosition().toLong());
		comp.setFloat("movePitch", movementPitch);

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
			dataManager.set(SHIELD_AMOUNT, comp.getInteger("shield"));
		}

		if (comp.hasKey("initPos", Constants.NBT.TAG_LONG)) {
			setHomePosAndDistance(BlockPos.fromLong(comp.getLong("initPos")), MAX_HOME_DISTANCE);
		}

		if (comp.hasKey("movePitch", Constants.NBT.TAG_FLOAT)) {
			movementPitch = comp.getFloat("movePitch");
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

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (getState().isVulnerable() && source == DamageSource.ANVIL && rand.nextInt(500) == 0) {
			setHealth(0.0f); //You've been Etho'd!
			return false;
		}

		amount = Math.min(amount, 20.0f);

		if (source == DamageSource.LIGHTNING_BOLT) {
			healShield(MathHelper.floor(amount));
			return false;
		}

		if (!isBossInvulnerableTo(source) && getState() == BossState.FLYING || getState() == BossState.IN_SCREEN) {
			damageShield(MathHelper.floor(amount));

			if (getShield() <= 0) {
				if (getShield() < 0) {
					setShield(0);
				}

				transitionToState(BossState.VULNERABLE);
				return true;
			}

			return false;
		}

		return super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean getIsInvulnerable() {
		return super.getIsInvulnerable() || getState().isInvulnerable();
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		return getIsInvulnerable() || isBossInvulnerableTo(source);
	}

	private boolean isBossInvulnerableTo(DamageSource source) {
		return isGloballyImmune(source) || isStateImmune(source);
	}

	private boolean isGloballyImmune(DamageSource source) {
		return source.isMagicDamage() || source == DamageSource.DROWN || source == DamageSource.FALL || source == DamageSource.CRAMMING || source == DamageSource.IN_WALL
				|| source == DamageSource.WITHER;
	}

	private boolean isStateImmune(DamageSource source) {
		if (getState().isUsingShield()) {
			return source.isExplosion() || source.isFireDamage() || source == DamageSource.CACTUS || source == DamageSource.FALLING_BLOCK;
		}

		return false;
	}

	@Override
	protected void despawnEntity() {
		//TODO: reset fight if player gets too far away
	}

	@Override
	protected boolean isMovementBlocked() {
		return super.isMovementBlocked() || getState() == BossState.IN_SCREEN;
	}

	@Override
	protected void initEntityAI() {
		//Spawning
		tasks.addTask(0, new AISpawn());

		//Flying
		tasks.addTask(2, new AIFlyAround());

		//Vulnerable
		//tasks.addTask(2, new AIRunAroundHelplessly(this));
		//tasks.addTask(1, new AIDodge(this));

		//other
		targetTasks.addTask(0, new EntityAINearestAttackableTarget(this, EntityPlayer.class, false, true));
	}

	@Override
	public boolean isPotionApplicable(PotionEffect potioneffect) {
		return false;
	}

	@Override
	public void setInWeb() {
		if (getState().isVulnerable()) {
			super.setInWeb();
		}
	}

	@Override
	public float getBlockPathWeight(BlockPos pos) {
		return 0.0F;
	}

	@Override
	protected boolean isValidLightLevel() {
		return true;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		if (!getState().isFlying()) {
			super.fall(distance, damageMultiplier);
		}
	}

	@Override
	protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
		if (!getState().isFlying()) {
			super.updateFallState(y, onGroundIn, state, pos);
		}
	}

	public class AISpawn extends EntityAIBase {
		public AISpawn() {
			setMutexBits(7);
		}

		@Override
		public void startExecuting() {
			TemporalConvergence.LOGGER.info("Starting spawn AI");
			navigator.tryMoveToXYZ(getHomePosition().getX(), getHomePosition().getY() + 2.0, getHomePosition().getZ(), 0.25);
		}

		@Override
		public boolean shouldExecute() {
			return getState() == BossState.SPAWNING && dataManager.get(SPAWN_TICKS) > 0;
		}

		@Override
		public void updateTask() {
			dataManager.set(SPAWN_TICKS, dataManager.get(SPAWN_TICKS) - 1);

			if (dataManager.get(SPAWN_TICKS) <= 0) {
				transitionToState(BossState.FLYING);
				TemporalConvergence.LOGGER.info("Spawn AI complete");
			}
		}
	}

	public class AIFlyAround extends EntityAIBase {
		public AIFlyAround() {
			setMutexBits(1);
		}

		@Override
		public boolean shouldExecute() {
			return getState() == BossState.FLYING;
		}

		@Override
		public void startExecuting() {
			TemporalConvergence.LOGGER.info("Starting flying AI");
		}

		@Override
		public void resetTask() {
			TemporalConvergence.LOGGER.info("Stopping flying AI");
		}

		@Override
		public void updateTask() {
			if (navigator.noPath()) {
				int x = rand.nextInt(BOSS_ROOM_WIDTH) + getHomePosition().getX() - BOSS_ROOM_RADIUS;
				int y = rand.nextInt(BOSS_ROOM_HEIGHT) + getHomePosition().getY() - 1;
				int z = rand.nextInt(BOSS_ROOM_DEPTH) + getHomePosition().getZ() - BOSS_ROOM_RADIUS;

				navigator.tryMoveToXYZ(x, y, z, 0.5);
			}
		}
	}

	/*public class AIRunAroundHelplessly extends EntityAIBase {

	}*/

	/*public class AIDodge extends EntityAIAvoidEntity {

	}*/

	public static class AIBossMoveHelper extends EntityMoveHelper {
		private final EntityAIBoss entity;

		public AIBossMoveHelper(EntityAIBoss boss) {
			super(boss);
			entity = boss;
		}

		@Override
		public void onUpdateMoveHelper() {
			if (action == EntityMoveHelper.Action.MOVE_TO) {
				action = EntityMoveHelper.Action.WAIT;
				double x = posX - entity.posX;
				double y = posY - entity.posY;
				double z = posZ - entity.posZ;
				double distanceSquared = x * x + y * y + z * z;

				if (distanceSquared < 2.5e-7) {
					entity.setMoveForward(0.0F);
					return;
				}

				entity.rotationYaw = (float)(MathHelper.atan2(z, x) * (180 / Math.PI));
				entity.movementPitch = (float)MathHelper.atan2(y, MathHelper.sqrt(x * x + z * z));
				entity.setAIMoveSpeed((float)(speed * entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
			}
			else {
				entity.setAIMoveSpeed(0.0f);
			}
		}
	}

	@Override
	public void moveEntityWithHeading(float strafe, float forward) {
		if (getState().isFlying()) {
			moveRelative(strafe, forward, 0.2f);
			move(MoverType.SELF, motionX, motionY, motionZ);

			motionX *= 0.75;
			motionY *= 0.75;
			motionZ *= 0.75;
		}
		else {
			super.moveEntityWithHeading(strafe, forward);
		}
	}

	@Override
	public void moveRelative(float strafe, float forward, float friction) {
		float distanceSquared = strafe * strafe + forward * forward;

		if (distanceSquared >= 1.0e-4f) {
			float distance = MathHelper.sqrt(distanceSquared);

			if (distance < 1.0f) {
				distance = 1.0f;
			}

			float adjustedFriction = friction / distance;
			strafe = strafe * adjustedFriction;
			forward = forward * adjustedFriction;
			float movement = MathHelper.sqrt(strafe * strafe + forward * forward);
			float xzMovement = movement * MathHelper.cos(movementPitch);
			float yawRadians = (float) (rotationYaw * (Math.PI / 180.0));

			float yComponent = movement * MathHelper.sin(movementPitch);
			float xComponent = xzMovement * MathHelper.cos(yawRadians);
			float zComponent = xzMovement * MathHelper.sin(yawRadians);

			if (Math.abs(xComponent) < 0.003f) {
				xComponent = 0.0f;
			}
			if (Math.abs(yComponent) < 0.003f) {
				yComponent = 0.0f;
			}
			if (Math.abs(zComponent) < 0.003f) {
				zComponent = 0.0f;
			}

			motionX += xComponent;
			motionY += yComponent;
			motionZ += zComponent;
		}
	}
	/*
	public boolean areWeFriends(EntityBossAI other) {
		return other.getInitialPos().equals(initialPos);
	}

	@Override
	public void addPotionEffect(PotionEffect potioneffectIn) {} //Immune to potion effects (They can probably still be added via map if really necessary)

	 */
}
