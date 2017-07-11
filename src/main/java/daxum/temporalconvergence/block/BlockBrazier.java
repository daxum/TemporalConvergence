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

import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.tileentity.TileBrazier;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockBrazier extends BlockBase implements ITileEntityProvider {
	public static final PropertyEnum<FilledState> FILL_STATE = PropertyEnum.create("filled", FilledState.class);
	public static final PropertyBool BURNING = PropertyBool.create("burning");
	private static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.125, 0.75);
	private static final AxisAlignedBB MIDDLE_AABB = new AxisAlignedBB(0.4375, 0.125, 0.4375, 0.5625, 0.9375, 0.5625);
	private static final AxisAlignedBB TOP_AABB = new AxisAlignedBB(0.25, 0.9375, 0.25, 0.75, 1.15625, 0.75);

	public BlockBrazier() {
		super("brazier", BlockPresets.STONE);
		setMiningLevel(MiningLevel.STONE);
		setStateDefaults(new Default(FILL_STATE, FilledState.EMPTY), new Default(BURNING, false));
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
			else if (stack.getItem() == Items.DYE && world.getTileEntity(pos) instanceof TileBrazier) {
				((TileBrazier)world.getTileEntity(pos)).addColor(stack);

				if (!world.isRemote) {
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

		if (world.getTileEntity(pos) instanceof TileBrazier) {
			((TileBrazier)world.getTileEntity(pos)).resetBurnTime();
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

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FILL_STATE, FilledState.getFromMeta(meta & 7)).withProperty(BURNING, (meta & 8) == 8);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FILL_STATE).getMeta() | (state.getValue(BURNING) ? 8 : 0);
	}

	public enum FilledState implements IStringSerializable {
		EMPTY("empty", 0),
		LEVEL_1("level1", 1),
		LEVEL_2("level2", 2),
		LEVEL_3("level3", 3),
		LEVEL_4("level4", 4),
		NETHERRACK("netherrack", 5);

		private String name;
		private int meta;

		private FilledState(String n, int m) {
			name = n;
			meta = m;
		}

		@Override
		public String getName() {
			return name;
		}

		public int getMeta() {
			return meta;
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

		public static FilledState getFromMeta(int meta) {
			switch (meta) {
			default:
			case 0: return EMPTY;
			case 1: return LEVEL_1;
			case 2: return LEVEL_2;
			case 3: return LEVEL_3;
			case 4: return LEVEL_4;
			case 5: return NETHERRACK;
			}
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
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileBrazier();
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
