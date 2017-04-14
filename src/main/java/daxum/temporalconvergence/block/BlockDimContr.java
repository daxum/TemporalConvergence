package daxum.temporalconvergence.block;

import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.tileentity.TileDimContr;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDimContr extends BlockBase implements ITileEntityProvider {
	public static final PropertyEnum POWER_LEVEL = PropertyEnum.create("power_level", EnumPowerLevel.class);

	public BlockDimContr() {
		super("dim_controller", 5.0f, 30.0f, "pickaxe", 2);
		setDefaultState(blockState.getBaseState().withProperty(POWER_LEVEL, EnumPowerLevel.EMPTY));
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileDimContr();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float x, float y, float z) {
		ItemStack stack = player.getHeldItem(hand);

		if (stack.isEmpty() && player.isSneaking()) {
			TileEntity te = world.getTileEntity(pos);

			if (te != null && te instanceof TileDimContr) {
				((TileDimContr) te).unbind();
				te.markDirty();
			}
		}
		else if (stack.getItem() == ModItems.DIM_LINKER) {
			TileEntity te = world.getTileEntity(pos);

			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dimid")) {
				if (te != null && te instanceof TileDimContr) {
					((TileDimContr) te).setId(stack.getTagCompound().getInteger("dimid"));
					te.markDirty();
				}
			}
			else if (te instanceof TileDimContr && ((TileDimContr) te).getId() >= 0){
				if (!stack.hasTagCompound())
					stack.setTagCompound(new NBTTagCompound());

				stack.getTagCompound().setInteger("dimid", ((TileDimContr) te).getId());
			}
		}

		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);

		if (te != null && te instanceof TileDimContr)
			((TileDimContr) te).unFreezeDim();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed, BlockPos changedPos) {
		if (world.getTileEntity(pos) instanceof TileDimContr) {
			if (world.isBlockPowered(pos))
				((TileDimContr)world.getTileEntity(pos)).freezeDim();
			else
				((TileDimContr)world.getTileEntity(pos)).unFreezeDim();
		}
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);

		if (te != null && te instanceof TileDimContr) {
			return getDefaultState().withProperty(POWER_LEVEL, ((TileDimContr) te).state);
		}

		return state;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(POWER_LEVEL, EnumPowerLevel.getValue(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumPowerLevel) state.getValue(POWER_LEVEL)).getIndex();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {POWER_LEVEL});
	}

	public static enum EnumPowerLevel implements IStringSerializable {
		EMPTY,
		LOW,
		MEDIUM,
		HIGH,
		TOO_HIGH;

		public int getIndex() {
			switch(this) {
			case EMPTY: return 0;
			case LOW: return 1;
			case MEDIUM: return 2;
			case HIGH: return 3;
			case TOO_HIGH: return 4;
			default: return 0;
			}
		}

		public static EnumPowerLevel getValue(int i) {
			switch(i) {
			case 0:
			default: return EMPTY;
			case 1: return LOW;
			case 2: return MEDIUM;
			case 3: return HIGH;
			case 4: return TOO_HIGH;
			}
		}

		@Override
		public String getName() {
			switch(this) {
			case EMPTY: return "empty";
			case HIGH: return "high";
			case LOW: return "low";
			case MEDIUM: return "medium";
			case TOO_HIGH: return "too_high";
			default: return "NAV"; //Not a value
			}
		}
	}
}
