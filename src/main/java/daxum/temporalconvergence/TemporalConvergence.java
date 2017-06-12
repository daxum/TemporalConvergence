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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import daxum.temporalconvergence.entity.ModEntities;
import daxum.temporalconvergence.gui.GuiHandler;
import daxum.temporalconvergence.proxy.IProxy;
import daxum.temporalconvergence.recipes.RecipeHandler;
import daxum.temporalconvergence.tileentity.ModTileEntities;
import daxum.temporalconvergence.world.DimensionHandler;
import daxum.temporalconvergence.world.ModWorldGenerator;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = TemporalConvergence.MODID, name = TemporalConvergence.NAME, version = TemporalConvergence.VERSION, acceptedMinecraftVersions="[1.11.2]")
public class TemporalConvergence {
	public static final String MODID = "temporalconvergence"; //Remember to change creative tab name in ModItems and lang file if changed.
	public static final String NAME = "Temporal Convergence";
	public static final String VERSION = "${version}";

	@Instance(MODID)
	public static TemporalConvergence instance;

	public static final Logger LOGGER = LogManager.getLogger(MODID); //Why on earth did I choose a 19 letter name?

	@SidedProxy(clientSide = "daxum.temporalconvergence.proxy.ClientProxy", serverSide = "daxum.temporalconvergence.proxy.ServerProxy")
	public static IProxy proxy;

	static {
		FluidRegistry.enableUniversalBucket();
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModEntities.init();
		ModTileEntities.init();

		RecipeHandler.initOreDict();
		DimensionHandler.init();

		proxy.registerFluidRenderer();
		proxy.registerEntityRenderer();
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

		GameRegistry.registerWorldGenerator(new ModWorldGenerator(), 0);
		RecipeHandler.init();

		LOGGER.info(InitLogBuilder.get());
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}
}
