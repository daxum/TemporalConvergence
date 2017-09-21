/***************************************************************************
 * Temporal Convergence
 * Copyright (C) 2017
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 **************************************************************************/
package daxum.temporalconvergence.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

//Screw it, good enough. Try not updating my inventory NOW, forge!
//Seriously though, make sure to remove this once forge fixes it
public class SlotItemHandlerFix extends SlotItemHandler {
	private final ItemStackHandler inventory;
	private final Method inventoryChanged;
	private final int index;

	public SlotItemHandlerFix(ItemStackHandler itemHandler, int i, int xPosition, int yPosition) {
		super(itemHandler, i, xPosition, yPosition);

		inventory = itemHandler;

		try {
			inventoryChanged = ItemStackHandler.class.getDeclaredMethod("onContentsChanged", int.class);
			inventoryChanged.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			TemporalConvergence.LOGGER.error("Inventory notification hack failed!");
			throw new ReportedException(new CrashReport("Inventory notification hack failed!", e));
		}

		index = i;
	}

	@Override
	public void onSlotChanged() {
		super.onSlotChanged();

		try {
			inventoryChanged.invoke(inventory, index);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			TemporalConvergence.LOGGER.error("Inventory notification hack failed!");
			throw new ReportedException(new CrashReport("Inventory notification hack failed!", e));
		}
	}
}
