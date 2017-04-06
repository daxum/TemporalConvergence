package daxum.temporalconvergence.block;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTimeStonePillar extends BlockBase {
	private static final PropertyBool TOP = PropertyBool.create("top");
	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 1.0, 0.6875);

	BlockTimeStonePillar() {
		super("time_stone_pillar");
		setDefaultState(blockState.getBaseState().withProperty(TOP, true));
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return world.getBlockState(pos.up()).getBlock() == this ? getDefaultState().withProperty(TOP, false) : getDefaultState();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed, BlockPos changedPos) {
		if (changedPos.equals(pos.up())) {
			if (state.getValue(TOP)) {
				if (world.getBlockState(changedPos).getBlock() == this)
					world.setBlockState(pos, getDefaultState().withProperty(TOP, false));
			}
			else {
				if (world.getBlockState(changedPos).getBlock() != this)
					world.setBlockState(pos, getDefaultState());
			}
		}
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return meta == 0 ? getDefaultState() : getDefaultState().withProperty(TOP, false);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TOP) ? 0 : 1;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {TOP});
	}
}
