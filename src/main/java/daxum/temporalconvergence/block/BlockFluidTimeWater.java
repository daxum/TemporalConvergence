package daxum.temporalconvergence.block;

import daxum.temporalconvergence.fluid.ModFluids;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class BlockFluidTimeWater extends BlockFluidClassic {
	public BlockFluidTimeWater() {
		super(ModFluids.timeWater, Material.WATER);
		setUnlocalizedName("time_water");
		setRegistryName("time_water");
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this).setRegistryName(this.getRegistryName()));
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos neighborPos) {
		super.neighborChanged(state, world, pos, neighbor, neighborPos);

		Block newNeighbor = world.getBlockState(neighborPos).getBlock();
		if (!neighborPos.equals(pos.up())) {
			convert(world, neighborPos, newNeighbor);
		}
	}

	@Override
	public int place(World world, BlockPos pos, FluidStack stack, boolean doPlace) {
		int superVal = super.place(world, pos, stack, doPlace);

		if (superVal != 0 && doPlace) {
			checkAllButUp(world, pos);
		}

		return superVal;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		checkAllButUp(world, pos);
	}

	@Override
	public void flowIntoBlock(World world, BlockPos pos, int meta) {
		if (meta >= 0 && displaceIfPossible(world, pos)) {
			world.setBlockState(pos, getBlockState().getBaseState().withProperty(LEVEL, meta), 3);
			checkAllButUp(world, pos);
		}
	}

	public void checkAllButUp(World world, BlockPos pos) {
		checkAndReplace(world, pos.down());
		checkAndReplace(world, pos.north());
		checkAndReplace(world, pos.east());
		checkAndReplace(world, pos.south());
		checkAndReplace(world, pos.west());
	}

	public void checkAndReplace(World world, BlockPos pos) {
		Block checked = world.getBlockState(pos).getBlock();
		convert(world, pos, checked);
	}

	private void convert(World world, BlockPos pos, Block toConvert) {
		if (toConvert == ModBlocks.timeWood || toConvert == ModBlocks.timeSteel)
			return;

		if (OreDictionary.containsMatch(false, OreDictionary.getOres("blockIron"), new ItemStack(toConvert, 1, OreDictionary.WILDCARD_VALUE))) {
			world.setBlockState(pos, ModBlocks.timeSteel.getDefaultState());
			world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 3.4f);
		}
		else if (OreDictionary.containsMatch(false, OreDictionary.getOres("logWood"), new ItemStack(toConvert, 1, OreDictionary.WILDCARD_VALUE))) {
			world.setBlockState(pos, ModBlocks.timeWood.getDefaultState());
			world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 3.4f);
		}
	}
}
