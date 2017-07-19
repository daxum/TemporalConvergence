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

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.tileentity.TileTimeFurnace;
import daxum.temporalconvergence.tileentity.TileTimeFurnaceBase;
import daxum.temporalconvergence.tileentity.TileTimeFurnaceController;
import daxum.temporalconvergence.util.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

public class BlockTimeFurnace extends BlockBase {
	public static final PropertyBool ACTIVE = PropertyBool.create("active");
	public static final PropertyBool CONTROLLER = PropertyBool.create("controller");

	public BlockTimeFurnace() {
		super("time_furnace", BlockPresets.STONE_MACHINE);
		setStateDefaults(new Default(ACTIVE, false), new Default(CONTROLLER, false));
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return state.getValue(ACTIVE) || state.getValue(CONTROLLER);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		if (state.getValue(ACTIVE)) {
			if (state.getValue(CONTROLLER)) {
				return new TileTimeFurnaceController();
			}

			return new TileTimeFurnace();
		}

		return null;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!state.getValue(ACTIVE) && player.getHeldItem(hand).getItem() instanceof ItemFlintAndSteel && isValidStructure(world, pos, facing)) {
			world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, Block.RANDOM.nextFloat() * 0.4f + 0.8f);
			createStructure(world, pos, facing);

			return true;
		}
		else if (state.getValue(ACTIVE)) {
			TileTimeFurnaceBase timeFurnace = WorldHelper.getTileEntity(world, pos, TileTimeFurnaceBase.class);

			if (timeFurnace != null) {
				BlockPos controllerPos = timeFurnace.getControllerPos();
				TileTimeFurnaceController controller = timeFurnace.getController();

				if (controller != null && controllerPos != null) {
					//player.openGui(TemporalConvergence.instance, GuiHandler.TIME_FURNACE_GUI, world, controllerPos.getX(), controllerPos.getY(), controllerPos.getZ());
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (state.getValue(ACTIVE)) {
			BlockPos bottomCorner = null;

			TileTimeFurnaceBase timeFurnace = WorldHelper.getTileEntity(world, pos, TileTimeFurnaceBase.class);

			if (timeFurnace != null) {
				bottomCorner = timeFurnace.getBottomCorner();
			}

			if (bottomCorner != null) {
				deactivate(world, bottomCorner);
			}
		}
	}

	private void deactivate(World world, BlockPos bottomCorner) {
		for (BlockPos pos : getStructurePosList(bottomCorner)) {
			IBlockState state = world.getBlockState(pos);

			if (state.getBlock() == ModBlocks.TIME_FURNACE) {
				if (state.getValue(CONTROLLER)) {
					TileTimeFurnaceController furnaceController = WorldHelper.getTileEntity(world, pos, TileTimeFurnaceController.class);

					if (furnaceController != null) {
						ItemStackHandler contrInv = furnaceController.getInventory();

						for (int i = 0; i < contrInv.getSlots(); i++) {
							InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), contrInv.getStackInSlot(i));
							contrInv.setStackInSlot(i, ItemStack.EMPTY);
						}
					}
				}

				world.setBlockState(pos, getDefaultState());
			}
		}
	}

	private boolean isValidStructure(World world, BlockPos pos, EnumFacing facing) {
		if (facing.getAxis() != Axis.Y) {
			BlockPos bottomCorner = pos.offset(facing.getOpposite()).down().north().west();

			for (BlockPos currentPos : getStructurePosList(bottomCorner)) {
				IBlockState state = world.getBlockState(currentPos);

				if (state.getBlock() != ModBlocks.TIME_FURNACE || state.getValue(ACTIVE)) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	private void createStructure(World world, BlockPos pos, EnumFacing facing) {
		if (!world.isRemote && facing.getAxis() != Axis.Y) {
			final int sideLength = 3;
			BlockPos bottomCorner = pos.offset(facing.getOpposite()).down().north().west();

			for (BlockPos currentPos : getStructurePosList(bottomCorner)) {
				IBlockState state = world.getBlockState(currentPos);

				if (state.getBlock() == ModBlocks.TIME_FURNACE && !state.getValue(ACTIVE)) {
					if (currentPos.equals(pos)) {
						world.setBlockState(currentPos, getDefaultState().withProperty(ACTIVE, true).withProperty(CONTROLLER, true));
					}
					else {
						world.setBlockState(currentPos, getDefaultState().withProperty(ACTIVE, true));

						TileTimeFurnace furnace = WorldHelper.getTileEntity(world, currentPos, TileTimeFurnace.class);

						if (furnace != null) {
							furnace.setControllerPos(pos);
						}
					}

					TileTimeFurnaceBase furnaceBase = WorldHelper.getTileEntity(world, currentPos, TileTimeFurnaceBase.class);

					if (furnaceBase != null) {
						furnaceBase.setBottomCorner(bottomCorner);
					}
				}
				else {
					TemporalConvergence.LOGGER.warn("Invalid structure block at {}", currentPos);
				}
			}
		}
	}

	private BlockPos[] getStructurePosList(BlockPos bottomCorner) {
		final int sideLength = 3;
		BlockPos[] posList = new BlockPos[sideLength * sideLength * sideLength];

		int index = 0;
		for (int x = bottomCorner.getX(); x < bottomCorner.getX() + sideLength; x++) {
			for (int y = bottomCorner.getY(); y < bottomCorner.getY() + sideLength; y++) {
				for (int z = bottomCorner.getZ(); z < bottomCorner.getZ() + sideLength; z++) {
					posList[index++] = new BlockPos(x, y, z);
				}
			}
		}

		return posList;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state.getValue(CONTROLLER)) {
			return 15;
		}

		return 0;
	}
}
