package daxum.temporalconvergence.block;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTimeStonePillar extends BlockBase {
	private static final PropertyEnum ENDS = PropertyEnum.create("location", EnumEnds.class);
	private static final AxisAlignedBB MIDDLE_AABB = new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 1.0, 0.6875);
	private static final AxisAlignedBB BOTTOM_AABB = FULL_BLOCK_AABB;
	private static final AxisAlignedBB TOP_AABB = new AxisAlignedBB( 0.1875, 0.0, 0.1875, 0.8125, 1.0, 0.8125);

	BlockTimeStonePillar() {
		super("time_stone_pillar");
		setDefaultState(blockState.getBaseState().withProperty(ENDS, EnumEnds.BOTH));
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		boolean topBlock = world.getBlockState(pos.up()).getBlock() == this;
		boolean bottomBlock = world.getBlockState(pos.down()).getBlock() == this;

		if (topBlock && bottomBlock)
			return getDefaultState().withProperty(ENDS, EnumEnds.NEITHER);
		if (topBlock)
			return getDefaultState().withProperty(ENDS, EnumEnds.BOTTOM);
		if (bottomBlock)
			return getDefaultState().withProperty(ENDS, EnumEnds.TOP);
		return getDefaultState();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed, BlockPos changedPos) {
		boolean topBlock = world.getBlockState(pos.up()).getBlock() == this;
		boolean bottomBlock = world.getBlockState(pos.down()).getBlock() == this;

		IBlockState newState = getDefaultState();

		if (topBlock && bottomBlock)
			newState = getDefaultState().withProperty(ENDS, EnumEnds.NEITHER);
		else if (topBlock)
			newState = getDefaultState().withProperty(ENDS, EnumEnds.BOTTOM);
		else if (bottomBlock)
			newState = getDefaultState().withProperty(ENDS, EnumEnds.TOP);

		if (newState.getValue(ENDS) != state.getValue(ENDS))
			world.setBlockState(pos, newState);
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
		switch((EnumEnds) state.getValue(ENDS)) {
		case BOTH:
		case BOTTOM: return BOTTOM_AABB;
		default:
		case NEITHER: return MIDDLE_AABB;
		case TOP: return TOP_AABB;
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return MIDDLE_AABB;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		switch(meta) {
		default:
		case 0: return getDefaultState();
		case 1: return getDefaultState().withProperty(ENDS, EnumEnds.NEITHER);
		case 2: return getDefaultState().withProperty(ENDS, EnumEnds.TOP);
		case 3: return getDefaultState().withProperty(ENDS, EnumEnds.BOTTOM);
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		switch((EnumEnds) state.getValue(ENDS)) {
		default:
		case BOTH: return 0;
		case NEITHER: return 1;
		case TOP: return 2;
		case BOTTOM: return 3;
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {ENDS});
	}

	public static enum EnumEnds implements IStringSerializable {
		NEITHER,
		TOP,
		BOTTOM,
		BOTH;

		@Override
		public String getName() {
			switch(this) {
			case BOTH: return "single";
			case BOTTOM: return "bottom";
			case NEITHER: return "middle";
			case TOP: return "top";
			default: return "Abort! Abort! EVERYONE PANIC!!1!!11!";
			}
		}
	}
}
