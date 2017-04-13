package daxum.temporalconvergence.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleDimGenCraft extends Particle {
	private double tx;
	private double ty;
	private double tz;
	private double speed;

	public ParticleDimGenCraft(World world, double posX, double posY, double posZ, double targetX, double targetY, double targetZ) {
		super(world, posX, posY, posZ);
		setRBGColorF(0.1961f, 0.6627f, 0.7176f); //Actually RGB, appears to be typo
		canCollide = false;
		particleMaxAge = 150;

		tx = targetX;
		ty = targetY;
		tz = targetZ;
		speed = Math.random() * 0.06 + 0.09;
	}

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (posX == tx && posY == ty && posZ == tz || particleAge++ >= particleMaxAge) {
			setExpired();
		}

		Vec3d vec = new Vec3d(tx - posX, ty - posY, tz - posZ);
		Vec3d travelVec = vec.normalize().scale(speed);

		motionX = travelVec.xCoord;
		motionY = travelVec.yCoord;
		motionZ = travelVec.zCoord;

		if (Math.abs(motionX) > Math.abs(tx - posX))
			motionX = tx - posX;
		if (Math.abs(motionY) > Math.abs(ty - posY))
			motionY = ty - posY;
		if (Math.abs(motionZ) > Math.abs(tz - posZ))
			motionZ = tz - posZ;

		move(motionX, motionY, motionZ);
	}

	@Override
	public void move(double x, double y, double z) {
		setBoundingBox(getBoundingBox().offset(x, y, z));
		resetPositionToBB();
	}
}
