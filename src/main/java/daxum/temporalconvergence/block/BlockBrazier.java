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

import java.util.Random;

import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.tileentity.TileBrazier;
import daxum.temporalconvergence.util.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class BlockBrazier extends BlockBase {
	public static final PropertyBool BURNING = PropertyBool.create("burning");
	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.21875, 0.75);

	public BlockBrazier() {
		super("brazier", BlockPresets.STONE);
		setStateDefaults(BURNING, false);
		setHasTileEntity();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			TileBrazier brazier = WorldHelper.getTileEntity(world, pos, TileBrazier.class);

			if (brazier != null) {
				if (brazier.isBurning()) {
					brazier.pauseBurning();
					world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8f);
				}
				else {
					//Extract item
					if (player.getHeldItem(hand).isEmpty()) {
						//If sneaking, clear all items, including partially burned ones. Partially burned items are lost
						if (player.isSneaking()) {
							brazier.stopBurning();
							ItemHandlerHelper.giveItemToPlayer(player, brazier.getInventory().extractItem(0, brazier.getInventory().getSlotLimit(0), false));
						}
						else {
							ItemHandlerHelper.giveItemToPlayer(player, brazier.getInventory().extractItem(0, 1, false));
						}
					}
					//Light brazier
					else if (player.getHeldItem(hand).getItem() instanceof ItemFlintAndSteel) {
						brazier.StartOrResumeBurning();
						player.getHeldItem(hand).damageItem(1, player);
						world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, Block.RANDOM.nextFloat() * 0.4f + 0.8f);
					}
					//Insert item
					else {
						ItemHandlerHelper.giveItemToPlayer(player, brazier.getInventory().insertItem(0, player.getHeldItem(hand).splitStack(1), false));
					}
				}
			}
		}

		return true;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		IBlockState other = world.getBlockState(pos);

		if (other.getBlock() != this) {
			return other.getLightValue(world, pos);
		}

		if (state.getValue(BURNING)) {
			return 15;
		}

		return 0;
	}

	@Override
	protected boolean isCube() {
		return false;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileBrazier();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {
		if (!world.isRemote && world.getBlockState(pos).getValue(BURNING) && !entity.isImmuneToFire()) {
			entity.setFire(3);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (world.getTileEntity(pos) instanceof TileBrazier) {
			ItemStackHandler invToDrop = ((TileBrazier) world.getTileEntity(pos)).getInventory();

			for (int i = 0; i < invToDrop.getSlots(); i++) {
				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), invToDrop.getStackInSlot(i));
				invToDrop.setStackInSlot(i, ItemStack.EMPTY);
			}
		}

		if (world.getBlockState(pos.down()).getBlock() instanceof BlockBrazierBottom) {
			world.setBlockToAir(pos.down());
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ModItems.BRAZIER;
	}

	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(ModItems.BRAZIER);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB;
	}
}
