package daxum.temporalconvergence.block;

import daxum.temporalconvergence.item.ModItems;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockTimeSand extends BlockFalling {
	public BlockTimeSand () {
		super(Material.SAND);
		setUnlocalizedName("time_sand");
		setRegistryName("time_sand");
		setCreativeTab(ModItems.TEMPCONVTAB);
		setHardness(0.5f);
		setHarvestLevel("shovel", 0);
		setSoundType(SoundType.SAND);
	}
}
