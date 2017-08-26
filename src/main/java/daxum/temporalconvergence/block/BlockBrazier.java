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
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockBrazier extends BlockBase {
	public static final PropertyEnum<FilledState> FILL_STATE = PropertyEnum.create("filled", FilledState.class);
	public static final PropertyBool BURNING = PropertyBool.create("burning");
	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.21875, 0.75);

	public BlockBrazier() {
		super("brazier", BlockPresets.STONE);
		setStateDefaults(FILL_STATE, FilledState.EMPTY, BURNING, false);
		setHasTileEntity();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!state.getValue(BURNING)) {
			FilledState filledState = state.getValue(FILL_STATE);
			ItemStack stack = player.getHeldItem(hand);

			if (stack.getItem() == ModItems.TIME_DUST && canFillWithDust(state)) {
				if (!world.isRemote) {
					world.setBlockState(pos, state.withProperty(FILL_STATE, filledState.getNextDustState()));
					stack.shrink(1);
				}

				return true;
			}
			else if (stack.getItem() == Item.getItemFromBlock(Blocks.NETHERRACK) && canFillWithNetherrack(state)) {
				if (!world.isRemote) {
					world.setBlockState(pos, state.withProperty(FILL_STATE, FilledState.NETHERRACK));
					stack.shrink(1);
				}

				return true;
			}
			else if (stack.getItem() instanceof ItemFlintAndSteel) {
				if (filledState != FilledState.EMPTY) {
					lightBrazier(world, pos, state);
				}

				world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, Block.RANDOM.nextFloat() * 0.4f + 0.8f);
				return true;
			}
			else if (hand == EnumHand.MAIN_HAND && stack.isEmpty()) {
				ItemStack newStack = filledState.getItem();

				if (!newStack.isEmpty()) {
					ItemHandlerHelper.giveItemToPlayer(player, newStack);

					if (!world.isRemote) {
						world.setBlockState(pos, state.withProperty(FILL_STATE, filledState.getPreviousState()));
					}

					return true;
				}
			}
		}
		else if (player.getHeldItemMainhand().isEmpty()) {
			putOutBrazier(world, pos, state);
			world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8f);
			return true;
		}

		return false;
	}

	private void lightBrazier(World world, BlockPos pos, IBlockState state) {
		world.setBlockState(pos, state.withProperty(BURNING, true));

		if (world.getTileEntity(pos) instanceof TileBrazier) {
			((TileBrazier)world.getTileEntity(pos)).startBurning();
		}
	}

	public static void putOutBrazier(World world, BlockPos pos, IBlockState state) {
		if (state.getValue(FILL_STATE) != FilledState.NETHERRACK) {
			world.setBlockState(pos, state.withProperty(BURNING, false).withProperty(FILL_STATE, state.getValue(FILL_STATE).getPreviousState()));
		}
		else {
			world.setBlockState(pos, state.withProperty(BURNING, false));
		}
	}

	private boolean canFillWithDust(IBlockState state) {
		FilledState fillState = state.getValue(FILL_STATE);

		return fillState != FilledState.LEVEL_4 && fillState != FilledState.NETHERRACK && !state.getValue(BURNING);
	}

	private boolean canFillWithNetherrack(IBlockState state) {
		return state.getValue(FILL_STATE) == FilledState.EMPTY && !state.getValue(BURNING);
	}

	public static boolean hasDust(IBlockState state) {
		return state.getValue(FILL_STATE).isDust();
	}

	public static IBlockState getLowerDustState(IBlockState state) {
		return state.withProperty(FILL_STATE, state.getValue(FILL_STATE).getPreviousState());
	}

	public static boolean isEmpty(IBlockState state) {
		return state.getValue(FILL_STATE) == FilledState.EMPTY;
	}

	public enum FilledState implements IStringSerializable {
		EMPTY("empty"),
		LEVEL_1("level1"),
		LEVEL_2("level2"),
		LEVEL_3("level3"),
		LEVEL_4("level4"),
		NETHERRACK("netherrack");

		private String name;

		private FilledState(String n) {
			name = n;
		}

		@Override
		public String getName() {
			return name;
		}

		public ItemStack getItem() {
			if (this == EMPTY) {
				return ItemStack.EMPTY;
			}
			else if (this == NETHERRACK) {
				return new ItemStack(Blocks.NETHERRACK);
			}
			else {
				return new ItemStack(ModItems.TIME_DUST);
			}
		}

		public FilledState getNextDustState() {
			switch(this) {
			case EMPTY: return LEVEL_1;
			case LEVEL_1: return LEVEL_2;
			case LEVEL_2: return LEVEL_3;
			case LEVEL_3: return LEVEL_4;
			case LEVEL_4: return LEVEL_4;
			case NETHERRACK: return NETHERRACK;
			default: return EMPTY;
			}
		}

		public FilledState getPreviousState() {
			switch(this) {
			case EMPTY: return EMPTY;
			case LEVEL_1: return EMPTY;
			case LEVEL_2: return LEVEL_1;
			case LEVEL_3: return LEVEL_2;
			case LEVEL_4: return LEVEL_3;
			case NETHERRACK: return EMPTY;
			default: return EMPTY;
			}
		}

		public boolean isDust() {
			return this != EMPTY && this != NETHERRACK;
		}
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		IBlockState other = world.getBlockState(pos);

		if (other.getBlock() != this) {
			return other.getLightValue(world, pos);
		}

		if (state.getValue(BURNING)) {
			FilledState fillState = state.getValue(FILL_STATE);

			if (fillState == FilledState.LEVEL_1) {
				return 6;
			}
			else if (fillState == FilledState.LEVEL_2) {
				return 9;
			}
			else if (fillState == FilledState.LEVEL_3) {
				return 12;
			}
			else if (fillState == FilledState.LEVEL_4 || fillState == FilledState.NETHERRACK) {
				return 15;
			}
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
		ItemStack stackToDrop = ItemStack.EMPTY;

		switch(state.getValue(FILL_STATE)) {
		case EMPTY:
		default: break;
		case LEVEL_1: stackToDrop = new ItemStack(ModItems.TIME_DUST); break;
		case LEVEL_2: stackToDrop = new ItemStack(ModItems.TIME_DUST, 2); break;
		case LEVEL_3: stackToDrop = new ItemStack(ModItems.TIME_DUST, 3); break;
		case LEVEL_4: stackToDrop = new ItemStack(ModItems.TIME_DUST, 4); break;
		case NETHERRACK: stackToDrop = new ItemStack(Blocks.NETHERRACK); break;
		}

		InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stackToDrop);

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
