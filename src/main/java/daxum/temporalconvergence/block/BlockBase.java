package daxum.temporalconvergence.block;

import daxum.temporalconvergence.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockBase extends Block {
	public BlockBase (Material material, String unlocalizedName, float hardness, float resistance, String tool, int level, SoundType sound) {
		super(material);
		setUnlocalizedName(unlocalizedName);
		setRegistryName(unlocalizedName);
		setCreativeTab(ModItems.TEMPCONVTAB);
		setHardness(hardness);
		setResistance(resistance);
		setHarvestLevel(tool, level);
		setSoundType(sound);
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this).setRegistryName(this.getRegistryName()));
	}

	public BlockBase (String unlocalizedName, float hardness, float resistance, String tool, int level) {
		this(Material.ROCK, unlocalizedName, hardness, resistance, tool, level, SoundType.STONE);
	}

	public BlockBase (String unlocalizedName) {
		this(unlocalizedName, 2.0f, 10.0f, "pickaxe", 0);
	}

	public BlockBase (Material material, String unlocalizedName, float hardness, float resistance) {
		this(material, unlocalizedName, hardness, resistance, "", 0, SoundType.STONE);
	}
}
