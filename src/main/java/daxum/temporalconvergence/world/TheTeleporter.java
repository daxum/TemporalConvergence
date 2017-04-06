package daxum.temporalconvergence.world;

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class TheTeleporter extends Teleporter {
	protected final WorldServer worldServer;

	public TheTeleporter(World world) {
		super((WorldServer) world);
		worldServer = (WorldServer) world;
	}

	@Override
	public void placeInPortal(Entity entity, float rotationYaw) {
		BlockPos pos = entity.getPosition();
		WorldServer world = entity.getServer().worldServerForDimension(entity.dimension); //I have a weird feeling that there's a better way to do this

		int i = 0;
		while (!isValid(world, pos)) {
			if (world.isAirBlock(pos.down()))
				pos = pos.down();
			else
				pos = pos.up();
			i++;

			if (i > 256) {
				TemporalConvergence.LOGGER.warn("Could not find proper teleport location, aborting");
				pos = entity.getPosition();
				break;
			}
		}

		entity.setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public boolean isValid(World world, BlockPos pos) {
		return !world.isAirBlock(pos.down()) && world.isAirBlock(pos) && world.isAirBlock(pos.up());
	}

	//These aren't used yet
	@Override
	public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) { return false; }

	@Override
	public boolean makePortal(Entity entityIn) { return false; }

	@Override
	public void removeStalePortalLocations(long worldTime) {}
}
