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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import daxum.temporalconvergence.network.PacketDodge;
import daxum.temporalconvergence.network.PacketDodgeSuccess;
import daxum.temporalconvergence.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPhaseClothChest extends ItemArmor {
	private static final int MAX_DODGE_TIMER = 20;
	private static final int MAX_DODGE_COOLDOWN = 40;
	private static final Random rand = new Random();
	//TODO: reset dodgeCooldown and map when exit world
	private static final Map<UUID, DodgeInfo> PLAYER_DODGE_MAP = new HashMap<>(); //Only used/set on server
	private static int dodgeCooldown = 0; //Only set on client

	public ItemPhaseClothChest() {
		super(ModItems.PHASE_CLOTH_ARMOR, 0, EntityEquipmentSlot.CHEST);
		setRegistryName("phase_cloth_chest");
		setUnlocalizedName("phase_cloth_chest");
		setCreativeTab(ModItems.TEMPCONVTAB);
	}

	public static void setPlayerDodging(UUID uuid) {
		DodgeInfo info = PLAYER_DODGE_MAP.get(uuid);

		if (info == null) {
			PLAYER_DODGE_MAP.put(uuid, new DodgeInfo());
			info = PLAYER_DODGE_MAP.get(uuid);
		}

		info.setActive();
	}

	public static void updateDodgeState() {
		for (Entry<UUID, DodgeInfo> entry : PLAYER_DODGE_MAP.entrySet()) {
			entry.getValue().onUpdate();
		}
	}

	//Called serverside when a player who is attempting to dodge is hit
	public static boolean onHit(EntityPlayer player, DamageSource source) {
		if (canPlayerDodge(player)) {
			if (source.isDamageAbsolute() || source.canHarmInCreative() || source.isMagicDamage() || source.isUnblockable() || source == DamageSource.WITHER || player.isEntityInvulnerable(source)) {
				return false;
			}

			if (source.isFireDamage() && player.isBurning()) {
				player.extinguish();
			}

			PacketHandler.HANDLER.sendToDimension(new PacketDodgeSuccess().setUuidAndPos(player.getPersistentID(), player.posX, player.posY, player.posZ), player.dimension);

			if (PLAYER_DODGE_MAP.get(player.getPersistentID()) != null) {
				PLAYER_DODGE_MAP.get(player.getPersistentID()).deactivate();
			}

			if (rand.nextBoolean() && !player.capabilities.isCreativeMode) {
				int slot = rand.nextInt(4);
				EntityEquipmentSlot armorSlot = null;

				switch(slot) {
				case 0: armorSlot = EntityEquipmentSlot.FEET; break;
				case 1: armorSlot = EntityEquipmentSlot.LEGS; break;
				case 2: armorSlot = EntityEquipmentSlot.CHEST; break;
				case 3:	armorSlot = EntityEquipmentSlot.HEAD; break;
				default: armorSlot = EntityEquipmentSlot.CHEST; break;
				}

				player.getItemStackFromSlot(armorSlot).damageItem(1, player);
			}

			return true;
		}

		return false;
	}

	@SideOnly(Side.CLIENT)
	public static void onSuccessfulDodge(EntityPlayer player) {
		dodgeCooldown = 0;
		movePlayer(player);
	}

	@SideOnly(Side.CLIENT)
	//Called on the client when a PacketDodgeMove is received
	private static void movePlayer(EntityPlayer player) {
		if (Math.abs(player.motionX) > 0 || Math.abs(player.motionZ) > 0) {
			double origX = player.posX;
			double origZ = player.posZ;
			double newX = origX;
			double newY = player.posY;
			double newZ = origZ;

			while (Math.abs(newX - origX) < player.width * 2 && Math.abs(newZ - origZ) < player.width * 2) {
				if (player.getEntityBoundingBox() != null) {
					if (player.world.collidesWithAnyBlock(player.getEntityBoundingBox().offset(newX + player.motionX - origX, newY - player.posY, newZ + player.motionZ - origZ))) {
						if (!player.world.collidesWithAnyBlock(player.getEntityBoundingBox().offset(newX + player.motionX - origX, newY - player.posY + 1.0, newZ + player.motionZ - origZ))) {
							newY += 1.0;
						}
						else if (!player.world.collidesWithAnyBlock(player.getEntityBoundingBox().offset(newX + player.motionX - origX, newY - player.posY - 1.0, newZ + player.motionZ - origZ))) {
							newY -= 1.0;
						}
						else {
							break;
						}
					}
				}

				newX += player.motionX;
				newZ += player.motionZ;
			}

			player.setPosition(newX, newY, newZ);
		}
	}

	private static boolean canPlayerDodge(EntityPlayer player) {
		if (PLAYER_DODGE_MAP.containsKey(player.getPersistentID())) {
			return PLAYER_DODGE_MAP.get(player.getPersistentID()).isActive;
		}

		return false;
	}

	@SideOnly(Side.CLIENT)
	public static void onDodgeKeyPress() {
		PacketHandler.HANDLER.sendToServer(new PacketDodge());
		dodgeCooldown = MAX_DODGE_COOLDOWN;
	}

	public static void updateDodgeCooldown() {
		if (dodgeCooldown > 0) {
			dodgeCooldown--;
		}
	}

	public static boolean isWearingArmor(EntityPlayer player) {
		return player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemPhaseClothChest
				&& player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() instanceof ItemPhaseClothLegs
				&& player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof ItemPhaseClothBoots
				&& player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemPhaseClothHelmet;
	}

	private static class DodgeInfo {
		private int dodgeTimer = 0;
		private boolean isActive = false;

		public void setActive() {
			isActive = true;
			dodgeTimer = MAX_DODGE_TIMER;
		}

		public void onUpdate() {
			if (isActive) {
				dodgeTimer--;

				if (dodgeTimer <= 0) {
					isActive = false;
				}
			}
		}

		public void deactivate() {
			isActive = false;
			dodgeTimer = 0;
		}

		@Override
		public String toString() {
			return "DodgeInfo: { " + dodgeTimer + ", " + isActive + " }";
		}
	}
}
