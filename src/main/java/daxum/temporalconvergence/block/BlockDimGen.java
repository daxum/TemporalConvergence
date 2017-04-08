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
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.ItemStackHandler;

public class BlockDimGen extends BlockBase implements ITileEntityProvider {
	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375);

	public BlockDimGen() {
		super("dimensional_generator", 5.0f, 30.0f, "pickaxe", 2);
		GameRegistry.registerTileEntity(TileDimGen.class, "dimensional_generator");
	}

	@Override
	public boolean onBlockActivated (World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float notWorldx, float notWorldy, float notWorldz) {
		if (!world.isRemote && world.getTileEntity(pos) instanceof TileDimGen) {
			if (player.isSneaking()) {
				((TileDimGen) world.getTileEntity(pos)).setCrafting();
			}
			else {
				ItemStackHandler inventory = ((TileDimGen)world.getTileEntity(pos)).getInventory();
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
		}

		return true;
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
			((TileDimGen)world.getTileEntity(pos)).setCrafting();
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileDimGen();
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
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}
}
