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

import daxum.temporalconvergence.tileentity.TileDimGen;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

public class BlockDimGen extends BlockBase implements ITileEntityProvider {
	public static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(0.1875, 0.0, 0.1875, 0.8125, 0.1875, 0.8125);
	public static final AxisAlignedBB MIDDLE_AABB = new AxisAlignedBB(0.3125, 0.1875, 0.3125, 0.6875, 0.6875, 0.6875);
	public static final AxisAlignedBB TOP_AABB = new AxisAlignedBB(0.0625, 0.6875, 0.0625, 0.9375, 0.875, 0.9375);

	public BlockDimGen() {
		super("dimensional_generator", BlockPresets.STONE_MACHINE);
	}

	@Override
	public boolean onBlockActivated (World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float notWorldx, float notWorldy, float notWorldz) {
		if (!world.isRemote && player != null && world.getTileEntity(pos) instanceof TileDimGen) {
			if (player.isSneaking()) {
				//Sneak-right click to craft
				((TileDimGen) world.getTileEntity(pos)).tryStartCrafting();
			}
			else {
				ItemStackHandler inventory = ((TileDimGen) world.getTileEntity(pos)).getInventory();
				ItemStack inventoryStack = inventory.extractItem(0, 1, true);
				ItemStack playerStack = player.getHeldItem(hand);

				//If an item can be inserted, insert it.
				if (inventoryStack.isEmpty() && !playerStack.isEmpty()) {
					ItemStack remainder = inventory.insertItem(0, playerStack, false);
					player.setHeldItem(hand, remainder);
				}
				//Otherwise, try to take an item out
				else {
					inventoryStack = inventory.extractItem(0, 1, false);
					world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, inventoryStack));
				}
			}
		}

		return true; //What is this return value for?
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (world.getTileEntity(pos) instanceof TileDimGen) {
			//If (crafting) { explosions; }
			ItemStackHandler inventory = ((TileDimGen)world.getTileEntity(pos)).getInventory();

			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(0));
			inventory.setStackInSlot(0, ItemStack.EMPTY);
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos changedPos) {
		if (!world.isRemote && world.isBlockPowered(pos) && world.getTileEntity(pos) instanceof TileDimGen)
			((TileDimGen)world.getTileEntity(pos)).tryStartCrafting();
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileDimGen();
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

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}
}
