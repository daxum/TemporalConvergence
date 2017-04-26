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

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//Stopped working on this because it'll be removed when I stop being lazy and update my forge
public class EntityFrozen extends Entity {
	private Entity frozen;
	private int unFreezeCount = 0;

	public EntityFrozen(World world) { //If this isn't here it'll crash
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
			frozen = EntityList.createEntityFromNBT(comp, world);
			frozen.setPosition(toFreeze.posX, toFreeze.posY, toFreeze.posZ);
			frozen.ticksExisted = toFreeze.ticksExisted;
			setPrevious(frozen); //Mostly fixes a bug with shulker bullets (sometimes they still flicker)

			toFreeze.setDropItemsWhenDead(false);
			toFreeze.onKillCommand();
			toFreeze.setDead();
			world.removeEntity(toFreeze); //Have to be sure. Very, very, sure. Redundantly sure.
		}
		else {
			frozen = this; //This will only happen if the entity is null or dead
		}
	}

	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("frozen"))
			frozen = EntityList.createEntityFromNBT(comp.getCompoundTag("frozen"), world);
		else
			frozen = this;

		if (comp.hasKey("unfreeze"))
			unFreezeCount = comp.getInteger("unfreeze");

		actualSetPosition(frozen.posX, frozen.posY, frozen.posZ);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound comp) {
		NBTTagCompound frozenComp = new NBTTagCompound();

		if (frozen != this && frozen.writeToNBTAtomically(frozenComp))
			comp.setTag("frozen", frozenComp);

		comp.setInteger("unfreeze", unFreezeCount);
	}

	public void reFreeze() {
		unFreezeCount = 0;
	}

	@Override
	public void onUpdate() {
		if (unFreezeCount > 2) {
			if (!world.isRemote && !(frozen instanceof EntityFrozen)) //If !world.isRemote isn't there, it spawns a ghost entity on the client.
				world.spawnEntity(frozen);							  //Also, arrows get a potion effect because of (from what I can tell) a vanilla bug
			setDead();
		}

		unFreezeCount++;
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

	protected void setPrevious(Entity entity) {
		entity.prevDistanceWalkedModified = entity.distanceWalkedModified;
		entity.prevPosX = entity.posX;
		entity.prevPosY = entity.posY;
		entity.prevPosZ = entity.posZ;
		entity.prevRotationPitch = entity.rotationPitch;
		entity.prevRotationYaw = entity.rotationYaw;
	}

	//Resist all attempts to move. It shall stay static forever! ...Unless someone directly modifies the position, of course. Please don't do that.
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
	public void setWorld(World world) {} //Please don't mess with my world... :(

	@Override
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {}

	@Override
	public void setPortal(BlockPos pos) {}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) { return true; }

	@Override
	public void setPositionAndUpdate(double x, double y, double z) {}

	public static final AxisAlignedBB ZERO_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0); //Why would this be private in entity?!?!?!?

	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		if (!(frozen instanceof EntityFrozen))
			return frozen.getEntityBoundingBox();

		return ZERO_AABB; //I don't know why this keeps getting called, but it's really annoying
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
