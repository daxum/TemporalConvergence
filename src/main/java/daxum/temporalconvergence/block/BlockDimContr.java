package daxum.temporalconvergence.block;

import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.tileentity.TileDimContr;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockDimContr extends BlockBase implements ITileEntityProvider {

	public BlockDimContr() {
		super("dim_controller", 5.0f, 30.0f, "pickaxe", 2);
		GameRegistry.registerTileEntity(TileDimContr.class, "dim_controller");
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileDimContr();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float x, float y, float z) {
		ItemStack stack = player.getHeldItem(hand);

		if (stack.getItem() == ModItems.dimLinker) {
			TileEntity te = world.getTileEntity(pos);

			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dimid")) {
				if (te != null && te instanceof TileDimContr) {
					((TileDimContr) te).setId(stack.getTagCompound().getInteger("dimid"));
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
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

}
