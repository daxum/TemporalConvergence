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
package daxum.temporalconvergence.block;

import daxum.temporalconvergence.power.DirectPowerHelper;
import daxum.temporalconvergence.tileentity.TilePedestal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

public class BlockPedestal extends BlockBase {
	public static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(0.1875, 0.0, 0.1875, 0.8125, 0.1875, 0.8125);
	public static final AxisAlignedBB MIDDLE_AABB = new AxisAlignedBB(0.3125, 0.1875, 0.3125, 0.6875, 0.8125, 0.6875);
	public static final AxisAlignedBB TOP_AABB = new AxisAlignedBB(0.0625, 0.8125, 0.0625, 0.9375, 0.875, 0.9375);

	public BlockPedestal() {
		super("pedestal", BlockPresets.STONE);
		setHasTileEntity();
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TilePedestal();
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		DirectPowerHelper.signalProviderAdd(world, pos, TilePedestal.PROVIDER_RANGE);
	}

	@Override
	//Once again, x, y, and z ARE NOT WORLD COORDINATES!!!!
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float x, float y, float z) {
		if (!world.isRemote && world.getTileEntity(pos) instanceof TilePedestal) {
			ItemStackHandler inventory = ((TilePedestal)world.getTileEntity(pos)).getInventory();
			ItemStack playerStack = player.getHeldItemMainhand();
			ItemStack invStack = inventory.getStackInSlot(0);

			if (!invStack.isEmpty()) {
				world.spawnEntity(new EntityItem(world, pos.getX() + 0.5f, pos.getY() + 1.0f, pos.getZ() + 0.5f, invStack));
				inventory.setStackInSlot(0, ItemStack.EMPTY);
			}
			else if (!playerStack.isEmpty()) {
				inventory.setStackInSlot(0, playerStack.splitStack(1));
			}
		}

		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (world.getTileEntity(pos) instanceof TilePedestal) {
			ItemStackHandler inventory = ((TilePedestal)world.getTileEntity(pos)).getInventory();

			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(0));
			inventory.setStackInSlot(0, ItemStack.EMPTY);
		}

		DirectPowerHelper.signalProviderRemove(world, pos, TilePedestal.PROVIDER_RANGE);
	}

	@Override
	protected boolean isCube() {
		return false;
	}

	@Override
	public boolean hasMultipleBoundingBoxes() {
		return true;
	}

	@Override
	protected AxisAlignedBB[] getNewBoundingBoxList(World world, BlockPos pos, IBlockState state) {
		return new AxisAlignedBB[] {BASE_AABB, MIDDLE_AABB, TOP_AABB};
	}
}
