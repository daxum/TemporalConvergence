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
package daxum.temporalconvergence.item;

import net.minecraft.item.ItemStack;

public class HashableStack {
	private ItemStack stack;

	public HashableStack(ItemStack hashStack) {
		stack = hashStack;
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof HashableStack) && !(o instanceof ItemStack)) {
			return false;
		}

		ItemStack compareStack = null;

		if (o instanceof HashableStack) {
			compareStack = ((HashableStack)o).stack;
		}
		else if (o instanceof ItemStack){
			compareStack = (ItemStack)o;
		}

		return ItemStack.areItemStacksEqual(stack, compareStack);
	}

	@Override
	public int hashCode() {
		if (stack.isEmpty()) {
			return 0;
		}

		int code = stack.getItem().hashCode() ^ stack.getCount() ^ stack.getMetadata();

		if (stack.getTagCompound() != null) {
			code ^= stack.getTagCompound().hashCode();
		}

		return code;
	}
}
