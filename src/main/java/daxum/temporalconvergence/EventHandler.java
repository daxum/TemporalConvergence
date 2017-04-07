package daxum.temporalconvergence;

import daxum.temporalconvergence.power.PowerDimension;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
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
}
