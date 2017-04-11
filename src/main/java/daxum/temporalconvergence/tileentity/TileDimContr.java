package daxum.temporalconvergence.tileentity;

import daxum.temporalconvergence.block.BlockDimContr.EnumPowerLevel;
import daxum.temporalconvergence.power.PowerDimension;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileDimContr extends TileEntity implements ITickable {
	protected int linkId = -1;
	protected boolean didThisFreeze = false;
	protected boolean isFrozen = false;
	public float renderScale = 0; //Gets set by the tesr
	public EnumPowerLevel state = EnumPowerLevel.EMPTY;

	@Override
	public void update() {
		if (!world.isRemote) {
			EnumPowerLevel prevState = state;
			boolean prevFrozen = isFrozen;

			if (linkId == -1) {
				state = EnumPowerLevel.EMPTY;
				isFrozen = false;
			}
			else {
				PowerDimension connected = PowerDimension.get(world, linkId);

				if (connected == null) {
					state = EnumPowerLevel.EMPTY;
					isFrozen = false;
				}
				else {
					isFrozen = !connected.isActive();
					double ratio = connected.getPowerRatio();

					if (ratio < 0.15)
						state = EnumPowerLevel.LOW;
					else if (ratio < 0.5)
						state = EnumPowerLevel.MEDIUM;
					else if (ratio <= 1)
						state = EnumPowerLevel.HIGH;
					else if (ratio > 1)
						state = EnumPowerLevel.TOO_HIGH;
				}
			}

			if (state != prevState || isFrozen != prevFrozen)
				sendBlockUpdate();
		}
	}

	public void freezeDim() {
		if (world.isRemote || didThisFreeze) return;

		PowerDimension connected = PowerDimension.get(world, linkId);

		if (connected != null)
			connected.addFreezer();

		didThisFreeze = true;
		sendBlockUpdate();
	}

	public void unFreezeDim() {
		if (!world.isRemote && didThisFreeze) {
			PowerDimension connected = PowerDimension.get(world, linkId);

			if (connected != null) {
				connected.removeFreezer();
				didThisFreeze = false;
				sendBlockUpdate();
			}
		}
	}

	public void unbind() {
		if (!world.isRemote) {
			if (didThisFreeze) {
				PowerDimension connected = PowerDimension.get(world, linkId);

				if (connected != null)
					connected.removeFreezer();
			}

			linkId = -1;
			sendBlockUpdate();
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

			sendBlockUpdate();
		}
	}

	public int getId() {
		return linkId;
	}

	public boolean isDimFrozen() {
		return isFrozen;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setInteger("linkid", linkId);
		comp.setBoolean("freeze", didThisFreeze);
		comp.setInteger("state", state.getIndex());
		comp.setBoolean("isdimf", isFrozen);

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("linkid"))
			linkId = comp.getInteger("linkid");
		if (comp.hasKey("freeze"))
			didThisFreeze = comp.getBoolean("freeze");
		if (comp.hasKey("state"))
			state = EnumPowerLevel.getValue(comp.getInteger("state"));
		if (comp.hasKey("isdimf"))
			isFrozen = comp.getBoolean("isdimf");

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
		EnumPowerLevel prevPL = state;
		readFromNBT(pkt.getNbtCompound());

		if (state != prevPL)
			world.markBlockRangeForRenderUpdate(pos, pos);
	}

	public void sendBlockUpdate() {
		if (!world.isRemote) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 8);
			markDirty();
		}
	}
}
