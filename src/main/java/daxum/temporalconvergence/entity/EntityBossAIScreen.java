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

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityBossAIScreen extends Entity {
	public static final DataParameter<Boolean> ACTIVE = EntityDataManager.createKey(EntityBossAIScreen.class, DataSerializers.BOOLEAN);
	public static final DataParameter<Byte> HEALTH = EntityDataManager.createKey(EntityBossAIScreen.class, DataSerializers.BYTE);
	private EntityBossAI parent = null;

	public EntityBossAIScreen(World world) {
		super(world);
		setSize(3.0f, 2.0f); //TODO: Fix bounding box
		isImmuneToFire = true;
	}

	@Override
	protected void entityInit() {
		dataManager.register(ACTIVE, false);
		dataManager.register(HEALTH, (byte) 3); //TODO: scaling for multiple players
	}

	@Override
	public void onEntityUpdate() {}

	public void setHealth(int amount) {
		if (amount >= 0 && amount <= 3)
			dataManager.set(HEALTH, Byte.valueOf((byte) amount));
	}

	public void damage(int amount) {
		setHealth(dataManager.get(HEALTH) - amount);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (parent != null && !isEntityInvulnerable(source) && dataManager.get(ACTIVE))
			parent.damageScreen(this);

		return false;
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		return getIsInvulnerable() || EntityBossAI.isBossInvulnerableTo(source);
	}

	@Override
	protected boolean canBeRidden(Entity entityIn) {
		return false; //Riding a monitor would be rather strange, no?
	}

	@Override
	public boolean isPushedByWater() {
		return false;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound comp) {
		setHealth(comp.getByte("health"));
		//Active will get set by the parent when it rebinds.
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound comp) {
		comp.setByte("health", dataManager.get(HEALTH));
	}

	public boolean isOccupied() {
		return dataManager.get(ACTIVE);
	}

	public void setParent(EntityBossAI newParent) {
		parent = newParent;
		dataManager.set(ACTIVE, true);
	}
}
