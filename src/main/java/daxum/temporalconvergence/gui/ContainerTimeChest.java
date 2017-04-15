package daxum.temporalconvergence.gui;

import daxum.temporalconvergence.tileentity.TileTimeChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerTimeChest extends Container {
	private TileTimeChest te;

	public ContainerTimeChest(IInventory playerInventory, TileTimeChest tc) {
		te = tc;

		//Chest inventory
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 9; x++)
				addSlotToContainer(new SlotItemHandler(te.getInventory(), x + y * 9, 8 + x * 18, 18 + y * 18)); //I know what you came here to do. Don't.

		//Player inventory
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 9; x++)
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 85 + y * 18));

		//HotBar
		for (int x = 0; x < 9; x++)
			addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 143));
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return te.getDistanceSq(player.posX, player.posY, player.posZ) <= 16;
	}

	public TileTimeChest getTimeChest() {
		return te;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
		ItemStack previous = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(fromSlot);

		if (slot != null && slot.getHasStack()) {
			ItemStack current = slot.getStack();
			previous = current.copy();

			if (fromSlot < 27) {
				// From Chest Inventory to Player Inventory
				if (!mergeItemStack(current, 27, 63, true))
					return ItemStack.EMPTY;
			} else {
				// From Player Inventory to Chest Inventory
				if (!mergeItemStack(current, 0, 27, false))
					return ItemStack.EMPTY;
			}

			if (current.getCount() == 0)
				slot.putStack(ItemStack.EMPTY);
			else
				slot.onSlotChanged();

			if (current.getCount() == previous.getCount())
				return ItemStack.EMPTY;

			slot.onSlotChanged();
		}

		return previous;
	}
}
