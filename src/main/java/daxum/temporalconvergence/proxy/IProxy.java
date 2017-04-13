package daxum.temporalconvergence.proxy;

import net.minecraft.world.World;

public interface IProxy {
	public void registerItemRenderer();
	public void registerBlockRenderer();
	public void registerFluidRenderer();
	public void registerEntityRenderer();
	public void spawnDimGenParticle(World world, double posX, double posY, double posZ, double targetX, double targetY, double targetZ);
}
