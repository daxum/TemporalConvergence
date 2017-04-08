package daxum.temporalconvergence.tileentity;

import daxum.temporalconvergence.power.PowerDimension;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileDimContr extends TileEntity {
	protected int linkId = -1;
	protected boolean didThisFreeze = false;

	public void freezeDim() {
		if (world.isRemote || didThisFreeze) return;

		PowerDimension connected = PowerDimension.get(world, linkId);

		if (connected != null)
			connected.addFreezer();

		didThisFreeze = true;
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 8);
	}

	public void unFreezeDim() {
		if (!world.isRemote) {
			PowerDimension connected = PowerDimension.get(world, linkId);

			if (connected != null && didThisFreeze) {
				connected.removeFreezer();
				didThisFreeze = false;
				world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 8);
			}
		}
	}

	public void setId(int i) {
		if (!world.isRemote && i != linkId && i >= 0) {
			if (didThisFreeze) {
				PowerDimension old = PowerDimension.get(world, linkId);

				if (old != null)
					old.removeFreezer();

				linkId = i;

				PowerDimension current = PowerDimension.get(world, linkId);

				if (current != null)
					current.addFreezer();
			}
			else {
				linkId = i;
			}

			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 8);
		}
	}

	public int getId() {
		return linkId;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		if (linkId >= 0) //Negative id's are invalid
			comp.setInteger("linkid", linkId);
		comp.setBoolean("freeze", didThisFreeze);

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("linkid"))
			linkId = comp.getInteger("linkid");
		if (comp.hasKey("freeze"))
			didThisFreeze = comp.getBoolean("freeze");

		super.readFromNBT(comp);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, -42, writeToNBT(new NBTTagCompound()));
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}
}
