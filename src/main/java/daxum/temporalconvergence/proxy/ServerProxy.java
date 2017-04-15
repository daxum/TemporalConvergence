package daxum.temporalconvergence.proxy;

import net.minecraft.world.World;

public class ServerProxy implements IProxy{

	@Override
	public void registerItemRenderer() {}

	@Override
	public void registerFluidRenderer() {}

	@Override
	public void registerEntityRenderer() {}

	@Override
	public void spawnDimGenParticle(World world, double posX, double posY, double posZ, double targetX, double targetY, double targetZ) {}
}
