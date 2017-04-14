package daxum.temporalconvergence;

import daxum.temporalconvergence.block.ModBlocks;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.power.PowerDimension;
import daxum.temporalconvergence.world.DimensionHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber
public final class EventHandler {

	//This doesn't seem like a good way to handle updating...
	@SubscribeEvent
	public static void worldTick(WorldTickEvent event) {
		if (event.side == Side.SERVER && event.phase == Phase.END && event.world.provider.getDimension() == 0) {
			PowerDimension.updateDimensions(event.world);
		}
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		ModBlocks.registerBlocks(event.getRegistry());
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		ModItems.registerItems(event.getRegistry());
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
}
