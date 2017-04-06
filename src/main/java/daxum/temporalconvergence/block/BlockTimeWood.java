package daxum.temporalconvergence.block;

import daxum.temporalconvergence.item.ModItems;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockTimeWood extends BlockRotatedPillar {
	public BlockTimeWood() {
		super(Material.WOOD);
		setCreativeTab(ModItems.TEMPCONVTAB);
		setUnlocalizedName("time_wood");
		setRegistryName("time_wood");
		setHardness(2.0f);
		setHarvestLevel("axe", 0);
		setSoundType(SoundType.WOOD);
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this).setRegistryName(this.getRegistryName()));
	}
}
