package daxum.temporalconvergence.block;

import daxum.temporalconvergence.tileentity.TilePedestal;
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

public class BlockPedestal extends BlockBase implements ITileEntityProvider {
	public static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375);

	public BlockPedestal() {
		super("time_pedestal", 2.0f, 10.0f, "pickaxe", 1);
		GameRegistry.registerTileEntity(TilePedestal.class, "time_pedestal");
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TilePedestal();
	}

	@Override
	//Once again, x, y, and z ARE NOT WORLD COORDINATES!!!!
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float x, float y, float z) {
		if (!world.isRemote && world.getTileEntity(pos) instanceof TilePedestal) {
			ItemStackHandler inventory = ((TilePedestal)world.getTileEntity(pos)).getInventory();
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

		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (world.getTileEntity(pos) instanceof TilePedestal) {
			ItemStackHandler inventory = ((TilePedestal)world.getTileEntity(pos)).getInventory();

			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(0));
			inventory.setStackInSlot(0, ItemStack.EMPTY);
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
}
