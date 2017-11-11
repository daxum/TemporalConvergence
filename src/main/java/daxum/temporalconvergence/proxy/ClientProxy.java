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
import daxum.temporalconvergence.render.AIBossBarRenderer;
import daxum.temporalconvergence.render.entity.EntityRenderRegister;
import daxum.temporalconvergence.tileentity.TileBrazier;
import daxum.temporalconvergence.util.WorldHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.model.ModelLoader;

public class ClientProxy implements IProxy {

	@Override
	public void registerItemRenderer() {
		//Items
		registerRender(ModItems.TIME_BULB);
		registerRender(ModItems.TIME_STEEL_INGOT);
		registerRender(ModItems.TIME_DUST);
		registerRender(ModItems.SOLAR_WOOD_PICK);
		registerRender(ModItems.SOLAR_WOOD_SHOVEL);
		registerRender(ModItems.SOLAR_WOOD_AXE);
		registerRender(ModItems.TIME_FREEZER);
		registerRender(ModItems.DIM_LINKER);
		registerRender(ModItems.EARLY_FUTURE_DOOR);
		registerRender(ModItems.REWOUND_TIME_SEEDS);
		registerRender(ModItems.PHASE_CLOTH_CHEST);
		registerRender(ModItems.PHASE_CLOTH_LEGS);
		registerRender(ModItems.PHASE_CLOTH_BOOTS);
		registerRender(ModItems.PHASE_CLOTH_HELMET);
		registerRender(ModItems.LUNAR_BOOMERANG);
		registerRender(ModItems.BRAZIER);
		registerRender(ModItems.ENERGIZED_CHARCOAL);
		registerRender(ModItems.STABLE_IRON_INGOT);
		registerRender(ModItems.REINFORCED_SOLAR_PICK);
		registerRender(ModItems.REINFORCED_SOLAR_SHOVEL);
		registerRender(ModItems.REINFORCED_SOLAR_AXE);
		registerRender(ModItems.ANCIENT_DUST);
		registerRender(ModItems.TIME_GEM);

		//Blocks
		registerRender(Item.getItemFromBlock(ModBlocks.ORIGIN_STONE));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_STONE));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_STEEL));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_PLANT));
		registerRender(Item.getItemFromBlock(ModBlocks.LUNAR_WOOD));
		registerRender(Item.getItemFromBlock(ModBlocks.LUNAR_PLANKS));
		registerRender(Item.getItemFromBlock(ModBlocks.TIMESTONE_MIX));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_CHEST));
		registerRender(Item.getItemFromBlock(ModBlocks.PEDESTAL));
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
		registerRender(Item.getItemFromBlock(ModBlocks.SOLAR_WOOD));
		registerRender(Item.getItemFromBlock(ModBlocks.SOLAR_PLANKS));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_FURNACE));
		registerRender(Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_ROAD_STRIPE));
		registerRender(Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_ROAD));
		registerRender(Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_ROAD_BORDER));
		registerRender(Item.getItemFromBlock(ModBlocks.ROAD_BORDER_STAIRS));
		registerRender(Item.getItemFromBlock(ModBlocks.FUTURE_CHEST));
		registerRender(Item.getItemFromBlock(ModBlocks.SOLAR_LEAVES));
		registerRender(Item.getItemFromBlock(ModBlocks.SOLAR_SAPLING));
		registerRender(Item.getItemFromBlock(ModBlocks.LUNAR_LEAVES));
		registerRender(Item.getItemFromBlock(ModBlocks.LUNAR_SAPLING));
		registerRender(Item.getItemFromBlock(ModBlocks.CRAFTER));
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
	public void registerColors() {
		BlockColors colors = Minecraft.getMinecraft().getBlockColors();
		colors.registerBlockColorHandler((IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) -> { return getTimePlantColor(world, pos); }, ModBlocks.TIME_PLANT);
		colors.registerBlockColorHandler((IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) -> { return getBrazierColor(world, pos); }, ModBlocks.BRAZIER);

	}

	private int getTimePlantColor(IBlockAccess world, BlockPos pos) {
		if (world != null && pos != null) {
			return BiomeColorHelper.getGrassColorAtPos(world, pos);
		}

		return ColorizerGrass.getGrassColor(0.5, 1.0);
	}

	private int getBrazierColor(IBlockAccess world, BlockPos pos) {
		if (world != null && pos != null) {
			TileBrazier brazier = WorldHelper.getTileEntity(world, pos, TileBrazier.class);

			if (brazier != null) {
				return brazier.getColor();
			}
		}

		return 15961837;
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
