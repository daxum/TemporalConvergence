package daxum.temporalconvergence.block;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.gui.GuiHandler;
import daxum.temporalconvergence.tileentity.TileTimeChest;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.ItemStackHandler;

public class BlockTimeChest extends BlockBase implements ITileEntityProvider {
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375);

	public BlockTimeChest() {
		super(Material.ROCK, "time_chest", 3.5f, 25.0f, "pickaxe", 0, SoundType.STONE);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		GameRegistry.registerTileEntity(TileTimeChest.class, "time_chest");
	}


	@Override
	public boolean onBlockActivated (World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float x, float y, float z) {
		if (world.getTileEntity(pos) instanceof TileTimeChest) {
			player.openGui(TemporalConvergence.instance, GuiHandler.TIME_CHEST_GUI, world, pos.getX(), pos.getY(), pos.getZ());
			((TileTimeChest) world.getTileEntity(pos)).setUsed();
		}

		return true;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		//What, were you expecting something?
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (world.getTileEntity(pos) instanceof TileTimeChest) {
			ItemStackHandler invToDrop = ((TileTimeChest) world.getTileEntity(pos)).getInventory();

			for (int i = 0; i < invToDrop.getSlots(); i++) {
				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), invToDrop.getStackInSlot(i)); //It doesn't count if Minecraft modifies the stack, right?
				invToDrop.setStackInSlot(i, ItemStack.EMPTY);
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileTimeChest();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
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
		EnumFacing enumfacing = EnumFacing.getFront(meta);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
	}

		return getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror) {
		return state.withRotation(mirror.toRotation(state.getValue(FACING)));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {FACING});
	}
}
