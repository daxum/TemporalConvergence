package daxum.temporalconvergence.tileentity;

import java.util.ArrayList;
import java.util.List;

import daxum.temporalconvergence.recipes.DimGenRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileDimGen extends TileEntity implements ITickable {
	protected ItemStackHandler inventory = new SingleInventory(this);
	protected BlockPos[] pedLocs = new BlockPos[12];
	protected BlockPos prevPos = BlockPos.ORIGIN;
	protected AxisAlignedBB fullClock;
	protected AxisAlignedBB smallClock;
	protected boolean crafting = false;
	protected boolean done =  false;
	protected boolean successful = false;
	protected boolean isLinkRecipe = false;
	protected int craftTicks = 0;
	protected List<ItemStack> currentRecipe = new ArrayList<>();
	public float scale = 1.0f;
	public float prevScale = 1.0f;
	public float[] rotations = {0.0f, 0.0f, 0.0f, 0.0f}; //Face, hour, minute, second
	public float[] prevRotations = {0.0f, 0.0f, 0.0f, 0.0f};

	@Override
	public void update() {
		if (!prevPos.equals(pos))
			onLoad(); //Just in case

		prevScale = scale;
		for (int i = 0; i < rotations.length; i++) {
			prevRotations[i] = rotations[i];
		}

		if (crafting) {
			if (scale < 15.0f && !done) {
				if (!world.isRemote) {
					List<ItemStack> stacks = getInputs();

					if (!shouldCraft(stacks, false)) {
						done = true;
						sendBlockUpdate();
					}
				}

				scale += 0.2f;

				if (scale > 15.0f) {
					scale = 15.0f;
					craftTicks = 400;
					clearPedestals();
				}

				setTime(1);
			}
			else if (scale >= 15.0f && !done) {
				if (!world.isRemote && !ItemStack.areItemStacksEqual(inventory.getStackInSlot(0), currentRecipe.get(0))) {
					done = true;
					sendBlockUpdate();
				}
				else {
					setTime(2);
					craftTicks--;
					if (craftTicks <= 0) {
						done = true;
						successful = true;
						if (!world.isRemote)
							sendBlockUpdate();
					}
				}
			}
			else if (done) {
				scale -= 0.2f;

				if (scale < 1.0f)
					scale = 1.0f;

				setTime(3);

				if (scale == 1.0f) {
					crafting = false;
					done = false;
					if (currentRecipe.size() > 1 && successful && !world.isRemote) {
						if (isLinkRecipe) {
							inventory.setStackInSlot(0, DimGenRecipes.getNewDimLink(world, currentRecipe));
							isLinkRecipe = false;
						}
						else
							inventory.setStackInSlot(0, DimGenRecipes.getOutput(currentRecipe.get(0), currentRecipe.subList(1, currentRecipe.size())));
						successful = false;
					}

					currentRecipe.clear();
				}
			}
		}
		else {
			setTime(0);
		}
	}

	//0-not crafting, 1-spinup, 2-crafting, 3-spindown. Only executed on client.
	protected void setTime(int stage) {
		if (world.isRemote) {
			if (stage == 0) {
				rotations[0] = 0.0f; //Not sure why this sometimes isn't set right
				rotations[1] = (world.getWorldTime() + 6000) % 12000 / 12000.0f * 360.0f;
				rotations[2] = rotations[1] % 30.0f / 30.0f * 360.0f;
				rotations[3] = rotations[2] % 30.0f / 30.0f * 360.0f;

				for (int i = 1; i < rotations.length; i++) {
					if (rotations[i] < prevRotations[i])
						prevRotations[i] -= 360.0;
				}
			}
			else if (stage == 1) {
				rotations[0] = 720.0f * ((scale - 1) / 14.0f);

				if (rotations[0] >= 720.0f)
					rotations[0] = 720.0f;

				for (int i = 1; i < rotations.length; i++)
					rotations[i] = 360.0f * (i + 3) * (rotations[0] / 720.0f);
			}
			else if (stage == 2) {
				rotations[0] = 720.0f;
				rotations[1] = (400.0f - craftTicks) / 400.0f * 360.0f + 1440;
				rotations[2] = 1800.0f + (400.0f - craftTicks) / 400.0f * 5040.0f;
				rotations[3] = 7200.0f - (400.0f - craftTicks) / 400.0f * 5040.0f;

				if (rotations[3] == 7200.0f)
					prevRotations[3] += 5040.0f;

				if (rotations[1] >= 1800.0f) {
					rotations[1] = 1440.0f;
					rotations[2] = 1800.0f;
					rotations[3] = 2160.0f;
					prevRotations[1] -= 360.0f;
					prevRotations[3] -= 5040.0f;
				}
			}
			else if (stage == 3) {
				rotations[0] = 720.0f * ((scale - 1) / 14.0f);

				if (rotations[0] <= 0.0f)
					rotations[0] = 0.0f;

				for (int i = 1; i < rotations.length; i++)
					rotations[i] = 360.0f * (i + 3) * (rotations[0] / 720.0f);
			}
		}
	}

	public void setCrafting() {
		if (!world.isRemote && !crafting) {
			List<ItemStack> stacks = getInputs();

			if (shouldCraft(stacks, true)) {
				crafting = true;
				currentRecipe = stacks;
				sendBlockUpdate();
			}
		}
	}

	protected boolean shouldCraft(List<ItemStack> stacks, boolean setLinkRecipe) {
		if (validatePedestals()) {
			if (!isLinkRecipe && stacks.size() >= 2 && DimGenRecipes.isValid(stacks.get(0), stacks.subList(1, stacks.size())) && checkAmount(stacks.size() - 1)) {
				return true;
			}
			else if (stacks.size() > 1 && DimGenRecipes.isValidDimLink(stacks)) {
				if (setLinkRecipe) //I know what you want to do here. Don't do it. Move along.
					isLinkRecipe = true;
				return true;
			}
		}

		return false;
	}

	public boolean validatePedestals() {
		for (int i = 0; i < pedLocs.length; i++)
			if (!(world.getTileEntity(pedLocs[i]) instanceof TilePedestal)) //Should I check if the chunk is loaded?
				return false;
		return true;
	}

	public List<ItemStack> getInputs() {
		List<ItemStack> inputs = new ArrayList<>();

		inputs.add(inventory.getStackInSlot(0));

		for (int i = 0; i < pedLocs.length; i++) {
			ItemStack stack = getSpot(pedLocs[i]);

			if (!stack.isEmpty())
				inputs.add(stack);
		}

		return inputs;
	}

	public boolean checkAmount(int amount) {
		switch (amount) {
		case 2: if (getSpot(pedLocs[1]) != ItemStack.EMPTY && getSpot(pedLocs[3]) != ItemStack.EMPTY						  //East and west
				|| getSpot(pedLocs[0]) != ItemStack.EMPTY && getSpot(pedLocs[2]) != ItemStack.EMPTY) return true; else break; //North and south

		case 4: if (getSpot(pedLocs[0]) != ItemStack.EMPTY && getSpot(pedLocs[1]) != ItemStack.EMPTY
				&& getSpot(pedLocs[2]) != ItemStack.EMPTY && getSpot(pedLocs[3]) != ItemStack.EMPTY) return true; else break; //North, south, east, and west

		case 8: if (getSpot(pedLocs[0]).isEmpty() && getSpot(pedLocs[1]).isEmpty()
				&& getSpot(pedLocs[2]).isEmpty() && getSpot(pedLocs[3]).isEmpty()) return true; else break; //Not four

		case 12: return true;
		}

		return false;
	}

	protected ItemStack getSpot(BlockPos position) {
		if (world.getTileEntity(position) instanceof TilePedestal)
			return ((TilePedestal)world.getTileEntity(position)).getInventory().getStackInSlot(0).copy();
		return ItemStack.EMPTY;
	}

	protected void clearPedestals() {
		if (world.isRemote) return;

		for (int i = 0; i < pedLocs.length; i++)
			clearPedestal(pedLocs[i]);
	}

	protected void clearPedestal(BlockPos position) {
		if (world.getTileEntity(position) instanceof TilePedestal)
			((TilePedestal)world.getTileEntity(position)).getInventory().setStackInSlot(0, ItemStack.EMPTY);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp) {
		comp.setTag("inv", inventory.serializeNBT());
		comp.setBoolean("craft", crafting);
		comp.setBoolean("done", done);
		comp.setBoolean("success", successful);
		comp.setBoolean("linkrecipe", isLinkRecipe);
		comp.setFloat("scale", scale);
		comp.setInteger("ctick", craftTicks);

		if (crafting)
			for (int i = 0; i < currentRecipe.size(); i++)
				comp.setTag("cur" + i, currentRecipe.get(i).serializeNBT());

		return super.writeToNBT(comp);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		if (comp.hasKey("inv"))
			inventory.deserializeNBT(comp.getCompoundTag("inv"));
		if (comp.hasKey("craft"))
			crafting = comp.getBoolean("craft");
		if (comp.hasKey("done"))
			done = comp.getBoolean("done");
		if (comp.hasKey("scale"))
			scale = comp.getFloat("scale");
		if (comp.hasKey("success"))
			successful = comp.getBoolean("success");
		if (comp.hasKey("linkrecipe"))
			isLinkRecipe = comp.getBoolean("linkrecipe");
		if (comp.hasKey("ctick"))
			craftTicks = comp.getInteger("ctick");

		currentRecipe.clear();
		for (int i = 0; i < 13; i++) {
			if (!comp.hasKey("cur" + i))
				break;
			currentRecipe.add(new ItemStack((NBTTagCompound) comp.getTag("cur" + i)));
		}

		super.readFromNBT(comp);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound comp = new NBTTagCompound();
		comp = writeToNBT(comp);

		return new SPacketUpdateTileEntity(pos, -42, comp);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing face) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(cap, face);
	}

	@Override
	public <T> T getCapability (Capability<T> cap, EnumFacing face) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T)inventory : super.getCapability(cap, face);
	}

	public ItemStackHandler getInventory() {
		return inventory;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (scale > 1.0f)
			return fullClock;
		return smallClock;
	}

	@Override
	public void onLoad() {
		//Why didn't this work before, and why did it randomly start working?
		fullClock = new AxisAlignedBB(pos.add(-6, 0, -6), pos.add(6, 1, 6));
		smallClock = new AxisAlignedBB(pos, pos.add(1, 1, 1));

		//Cache pedestal locations
		pedLocs[0] = pos.north(6);
		pedLocs[1] = pos.east(6);
		pedLocs[2] = pos.south(6);
		pedLocs[3] = pos.west(6);
		pedLocs[4] = pos.add(5, 0, 3);
		pedLocs[5] = pos.add(5, 0, -3);
		pedLocs[6] = pos.add(-5, 0, 3);
		pedLocs[7] = pos.add(-5, 0, -3);
		pedLocs[8] = pos.add(3, 0, 5);
		pedLocs[9] = pos.add(3, 0, -5);
		pedLocs[10] = pos.add(-3, 0, 5);
		pedLocs[11] = pos.add(-3, 0, -5);

		prevPos = pos;
	}

	public static class SingleInventory extends ItemStackHandler {
		private final TileDimGen parent;

		public SingleInventory(TileDimGen tp) {
			super();
			parent = tp;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		protected void onContentsChanged(int slot) {
			parent.sendBlockUpdate();
		}
	}

	public void sendBlockUpdate() {
		if (!world.isRemote) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 8);
			markDirty();
		}
	}
}
