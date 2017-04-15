package daxum.temporalconvergence.block;

import daxum.temporalconvergence.item.ModItems;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockTimeWood extends BlockRotatedPillar {
	public BlockTimeWood() {
		super(Material.WOOD);
		setCreativeTab(ModItems.TEMPCONVTAB);
		setUnlocalizedName("time_wood");
		setRegistryName("time_wood");
		setHardness(2.0f);
		setHarvestLevel("axe", 0);
		setSoundType(SoundType.WOOD);
	}

	@Override
	public boolean isWood(IBlockAccess world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() == this;
	}
}
