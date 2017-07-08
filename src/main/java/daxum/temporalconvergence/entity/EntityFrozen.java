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

import daxum.temporalconvergence.network.PacketFrozenEntity;
import daxum.temporalconvergence.network.PacketHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class EntityFrozen extends Entity {
	private static final DataParameter<Integer> UNFREEZE_COUNT = EntityDataManager.createKey(EntityFrozen.class, DataSerializers.VARINT);
	private Entity frozen;

	//If this isn't here it'll crash
	public EntityFrozen(World world) {
		super(world);
		setNoGravity(true);
		setEntityInvulnerable(true);
		frozen = this;
	}

	public EntityFrozen(World world, Entity toFreeze) {
		super(world);
		setNoGravity(true);
		setEntityInvulnerable(true);
		actualSetPosition(toFreeze.posX, toFreeze.posY, toFreeze.posZ);

		NBTTagCompound comp = new NBTTagCompound();

		if (toFreeze.writeToNBTAtomically(comp)) {
			setFrozenEntity(comp);
			frozen.setPosition(toFreeze.posX, toFreeze.posY, toFreeze.posZ);
			frozen.ticksExisted = toFreeze.ticksExisted;
			//Mostly fixes a bug with shulker bullets (sometimes they still flicker)
			setPrevious(frozen);

			world.removeEntity(toFreeze);
		}
		else {
			//This will only happen if the entity is null or dead
			frozen = this;
		}
	}

	@Override
	protected void entityInit() {
		dataManager.register(UNFREEZE_COUNT, 0);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("frozen", Constants.NBT.TAG_COMPOUND)) {
			setFrozenEntity(comp.getCompoundTag("frozen"));
		}
		else {
			frozen = this;
		}

		if (comp.hasKey("unfreeze", Constants.NBT.TAG_INT)) {
			dataManager.set(UNFREEZE_COUNT, comp.getInteger("unfreeze"));
		}

		actualSetPosition(frozen.posX, frozen.posY, frozen.posZ);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound comp) {
		NBTTagCompound frozenComp = new NBTTagCompound();

		if (frozen != this && frozen.writeToNBTAtomically(frozenComp)) {
			comp.setTag("frozen", frozenComp);
		}

		comp.setInteger("unfreeze", dataManager.get(UNFREEZE_COUNT));
	}

	public void reFreeze() {
		dataManager.set(UNFREEZE_COUNT, 0);
	}

	@Override
	public void onUpdate() {
		if (firstUpdate && !world.isRemote) {
			NBTTagCompound comp = new NBTTagCompound();
			frozen.writeToNBTAtomically(comp);
			PacketHandler.HANDLER.sendToAll(new PacketFrozenEntity().setTag(getPersistentID(), comp));
			firstUpdate = false;
		}

		if (dataManager.get(UNFREEZE_COUNT) > 2) {
			if (!world.isRemote && !(frozen instanceof EntityFrozen)) {
				//Arrows come out with potion particles because of MC-107941
				world.spawnEntity(frozen);
			}

			setDead();
		}

		if (!world.isRemote) {
			dataManager.set(UNFREEZE_COUNT, dataManager.get(UNFREEZE_COUNT) + 1);
		}
	}

	private void actualSetPosition(double x, double y, double z) {
		posX = x;
		prevPosX = x;
		lastTickPosX = x;
		posY = y;
		prevPosY = y;
		lastTickPosY = y;
		posZ = z;
		prevPosZ = z;
		lastTickPosZ = z;
	}

	public Entity getFrozenEntity() {
		return frozen;
	}

	public void setFrozenEntity(NBTTagCompound frozenTag) {
		frozen = EntityList.createEntityFromNBT(frozenTag, world);
	}

	private void setPrevious(Entity entity) {
		entity.prevDistanceWalkedModified = entity.distanceWalkedModified;
		entity.prevPosX = entity.posX;
		entity.prevPosY = entity.posY;
		entity.prevPosZ = entity.posZ;
		entity.prevRotationPitch = entity.rotationPitch;
		entity.prevRotationYaw = entity.rotationYaw;
	}

	@Override
	public boolean doesEntityNotTriggerPressurePlate() { return true; }

	@Override
	public void onEntityUpdate() {}

	@Override
	public boolean getIsInvulnerable() { return true; }

	@Override
	protected boolean canTriggerWalking() { return false; }

	@Override
	protected void updateFallState(double y, boolean onGround, IBlockState state, BlockPos pos) {}

	@Override
	public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {}

	@Override
	public void moveToBlockPosAndAngles(BlockPos pos, float rotationYawIn, float rotationPitchIn) {}

	@Override
	public void onCollideWithPlayer(EntityPlayer entityIn) {}

	@Override
	public void applyEntityCollision(Entity entityIn) {}

	@Override
	public void addVelocity(double x, double y, double z) {}

	@Override
	public void setWorld(World world) {}

	@Override
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {}

	@Override
	public void setPortal(BlockPos pos) {}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) { return true; }

	@Override
	public void setPositionAndUpdate(double x, double y, double z) {}

	public static final AxisAlignedBB ZERO_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		if (!(frozen instanceof EntityFrozen))
			return frozen.getEntityBoundingBox();

		return ZERO_AABB;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (!(frozen instanceof EntityFrozen))
			return frozen.getRenderBoundingBox();

		return super.getRenderBoundingBox();
	}

	@Override
	public boolean isInRangeToRender3d(double x, double y, double z) {
		if (!(frozen instanceof EntityFrozen))
			return frozen.isInRangeToRender3d(x, y, z);

		return false;
	}

	@Override
	public void setPosition(double x, double y, double z) {}
}
