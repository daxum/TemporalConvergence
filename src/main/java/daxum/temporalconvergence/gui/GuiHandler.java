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

import daxum.temporalconvergence.tileentity.TileFutureChest;
import daxum.temporalconvergence.tileentity.TileTimeChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	public static final int TIME_CHEST_GUI = 0;
	public static final int FUTURE_CHEST_GUI = 1;

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch(id) {
		case TIME_CHEST_GUI: return new ContainerTimeChest(player.inventory, (TileTimeChest)world.getTileEntity(new BlockPos(x, y, z)));
		case FUTURE_CHEST_GUI: return new ContainerFutureChest(player.inventory, (TileFutureChest)world.getTileEntity(new BlockPos(x, y, z)));
		default: return null;
		}
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch(id) {
		case TIME_CHEST_GUI: return new GuiTimeChest(player.inventory, (TileTimeChest)world.getTileEntity(new BlockPos(x, y, z)));
		case FUTURE_CHEST_GUI: return new GuiFutureChest(player.inventory, (TileFutureChest)world.getTileEntity(new BlockPos(x, y, z)));
		default: return null;
		}
	}
}
