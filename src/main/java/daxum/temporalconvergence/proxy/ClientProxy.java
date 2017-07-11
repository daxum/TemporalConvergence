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
package daxum.temporalconvergence.proxy;

import daxum.temporalconvergence.block.ModBlocks;
import daxum.temporalconvergence.entity.EntityAIBoss;
import daxum.temporalconvergence.fluid.FluidRenderRegister;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.particle.ParticleDimGenCraft;
import daxum.temporalconvergence.render.AIBossBarRenderer;
import daxum.temporalconvergence.render.entity.EntityRenderRegister;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.model.ModelLoader;

public class ClientProxy implements IProxy {

	@Override
	public void registerItemRenderer() {
		//Items
		registerRender(ModItems.TIME_BULB, 0, "_empty");
		registerRender(ModItems.TIME_BULB, 1);
		registerRender(ModItems.TIME_STEEL_INGOT);
		registerRender(ModItems.TIME_DUST);
		registerRender(ModItems.SOLAR_WOOD_PICK);
		registerRender(ModItems.SOLAR_WOOD_SHOVEL);
		registerRender(ModItems.SOLAR_WOOD_AXE);
		registerRender(ModItems.TIME_FREEZER);
		registerRender(ModItems.DIM_LINKER);
		registerRender(ModItems.EARLY_FUTURE_DOOR);
		registerRender(ModItems.REWOUND_TIME_SEEDS);
		registerRender(ModItems.INFUSED_WOOD);
		registerRender(ModItems.STABLE_CHARCOAL);
		registerRender(ModItems.PHASE_CLOTH_CHEST);
		registerRender(ModItems.PHASE_CLOTH_LEGS);
		registerRender(ModItems.PHASE_CLOTH_BOOTS);
		registerRender(ModItems.PHASE_CLOTH_HELMET);

		//Blocks
		registerRender(Item.getItemFromBlock(ModBlocks.ORIGIN_STONE));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_STONE));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_STEEL));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_PLANT));
		registerRender(Item.getItemFromBlock(ModBlocks.LUNAR_WOOD));
		registerRender(Item.getItemFromBlock(ModBlocks.LUNAR_PLANKS));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_SAND));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_CHEST));
		registerRender(Item.getItemFromBlock(ModBlocks.PEDESTAL));
		registerRender(Item.getItemFromBlock(ModBlocks.DIM_GEN));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_STONE_PILLAR));
		registerRender(Item.getItemFromBlock(ModBlocks.TEST_TELEPORTER)); //Why?
		registerRender(Item.getItemFromBlock(ModBlocks.DIM_CONTR));
		registerRender(Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_BLOCK));
		registerRender(Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_BLOCK), 1, "_floor");
		registerRender(Item.getItemFromBlock(ModBlocks.FANCY_EARLY_FUTURE_STAIRS));
		registerRender(Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_STAIRS));
		registerRender(Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_FENCE));
		registerRender(Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_HALF_SLAB));
		registerRender(Item.getItemFromBlock(ModBlocks.REWOUND_SOIL));
		registerRender(Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_BUTTON));
		registerRender(Item.getItemFromBlock(ModBlocks.BRAZIER));
		registerRender(Item.getItemFromBlock(ModBlocks.SOLAR_WOOD));
		registerRender(Item.getItemFromBlock(ModBlocks.SOLAR_PLANKS));
	}

	@Override
	public void registerColors() {
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				return world != null && pos != null ? BiomeColorHelper.getGrassColorAtPos(world, pos) :  ColorizerGrass.getGrassColor(0.5, 1.0);
			}
		}, ModBlocks.TIME_PLANT);
	}

	@Override
	public void registerFluidRenderer() {
		FluidRenderRegister.init();
	}

	@Override
	public void registerEntityRenderer() {
		EntityRenderRegister.init();
	}

	public void registerRender(Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

	public void registerRender(Item item, int meta) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

	public void registerRender(Item item, int meta, String suffix) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName().toString() + suffix, "inventory"));
	}

	@Override
	public void spawnDimGenParticle(World world, double posX, double posY, double posZ, double targetX, double targetY, double targetZ) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDimGenCraft(world, posX, posY, posZ, targetX, targetY, targetZ));
	}

	@Override
	public void spawnWaterParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
		world.spawnParticle(EnumParticleTypes.WATER_SPLASH, x, y, z, vx, vy, vz, 0);
	}

	@Override
	public void addAIBoss(EntityAIBoss toAdd) {
		AIBossBarRenderer.addAIBoss(toAdd);
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public boolean isDedicatedServer() {
		return false;
	}
}
