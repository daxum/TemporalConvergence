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
import daxum.temporalconvergence.fluid.FluidRenderRegister;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.particle.ParticleDimGenCraft;
import daxum.temporalconvergence.render.entity.EntityRenderRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

public class ClientProxy implements IProxy {

	@Override
	public void registerItemRenderer() {
		//Items
		registerRender(ModItems.TIME_PEARL);
		registerRender(ModItems.TIME_STEEL_INGOT);
		registerRender(ModItems.TIME_DUST);
		registerRender(ModItems.TIME_WOOD_PICK);
		registerRender(ModItems.TIME_WOOD_SHOVEL);
		registerRender(ModItems.TIME_WOOD_AXE);
		registerRender(ModItems.TIME_FREEZER);
		registerRender(ModItems.DIM_LINKER);
		registerRender(ModItems.EARLY_FUTURE_DOOR);

		//Blocks
		registerRender(Item.getItemFromBlock(ModBlocks.ORIGIN_STONE));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_STONE));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_STEEL));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_PLANT));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_WOOD));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_WOOD_PLANKS));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_SAND));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_CHEST));
		registerRender(Item.getItemFromBlock(ModBlocks.PEDESTAL));
		registerRender(Item.getItemFromBlock(ModBlocks.DIM_GEN));
		registerRender(Item.getItemFromBlock(ModBlocks.TIME_STONE_PILLAR));
		registerRender(Item.getItemFromBlock(ModBlocks.TEST_TELEPORTER)); //Why?
		registerRender(Item.getItemFromBlock(ModBlocks.DIM_CONTR));
		registerRender(Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_BLOCK));
		registerRender(Item.getItemFromBlock(ModBlocks.FANCY_EARLY_FUTURE_STAIRS));
		registerRender(Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_STAIRS));
		registerRender(Item.getItemFromBlock(ModBlocks.EARLY_FUTURE_FENCE));
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

	@Override
	public void spawnDimGenParticle(World world, double posX, double posY, double posZ, double targetX, double targetY, double targetZ) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDimGenCraft(world, posX, posY, posZ, targetX, targetY, targetZ));
	}
}
