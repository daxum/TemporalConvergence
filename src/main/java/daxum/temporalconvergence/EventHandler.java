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
package daxum.temporalconvergence;

import daxum.temporalconvergence.block.ModBlocks;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.power.PowerDimension;
import daxum.temporalconvergence.render.AIBossBarRenderer;
import daxum.temporalconvergence.world.DimensionHandler;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public final class EventHandler {
	public static void init() {
		MinecraftForge.EVENT_BUS.register(ModBlocks.class);
		MinecraftForge.EVENT_BUS.register(ModItems.class);
	}

	//This doesn't seem like a good way to handle updating...
	@SubscribeEvent
	public static void worldTick(WorldTickEvent event) {
		if (event.side == Side.SERVER && event.phase == Phase.END && event.world.provider.getDimension() == 0) {
			PowerDimension.updateDimensions(event.world);
		}
	}

	@SubscribeEvent
	public static void registerBiomes(RegistryEvent.Register<Biome> event) {
		IForgeRegistry biomeRegistry = event.getRegistry();

		biomeRegistry.register(DimensionHandler.FUTURE_WASTELAND);
		BiomeDictionary.addTypes(DimensionHandler.FUTURE_WASTELAND, Type.SPARSE, Type.DRY/*relatively*/, Type.SAVANNA, Type.DEAD, Type.WASTELAND);
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		TemporalConvergence.proxy.registerItemRenderer();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void renderBossBar(RenderGameOverlayEvent.BossInfo event) {
		AIBossBarRenderer.renderBossBar(event);
	}
}
