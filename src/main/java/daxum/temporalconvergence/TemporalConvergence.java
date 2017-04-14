package daxum.temporalconvergence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import daxum.temporalconvergence.entity.ModEntities;
import daxum.temporalconvergence.gui.GuiHandler;
import daxum.temporalconvergence.proxy.IProxy;
import daxum.temporalconvergence.recipes.RecipeHandler;
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
	public static final String VERSION = "9001.8";

	@Instance(MODID)
	public static TemporalConvergence instance;

	public static final Logger LOGGER = LogManager.getLogger("tempConv"); //Why on earth did I choose a 19 letter name?

	@SidedProxy(clientSide = "daxum.temporalconvergence.proxy.ClientProxy", serverSide = "daxum.temporalconvergence.proxy.ServerProxy")
	public static IProxy proxy;

	static {
		FluidRegistry.enableUniversalBucket();
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModEntities.init();

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
