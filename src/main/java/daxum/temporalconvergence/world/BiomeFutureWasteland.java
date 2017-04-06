package daxum.temporalconvergence.world;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class BiomeFutureWasteland extends Biome {

	public BiomeFutureWasteland(BiomeProperties properties) {
		super(properties);
		spawnableMonsterList.clear();
		spawnableCreatureList.clear();
		spawnableWaterCreatureList.clear();
		spawnableCaveCreatureList.clear();
		topBlock = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, DirtType.COARSE_DIRT); //This was much simpler with getStateFromMeta(1)
		theBiomeDecorator = new BiomeWastelandDecorator();
		registerBiome(217, "future_wasteland", this);
		BiomeDictionary.addTypes(this, Type.SPARSE, Type.DRY/*relatively*/, Type.SAVANNA, Type.DEAD, Type.WASTELAND);
	}

	@Override
	public BiomeDecorator createBiomeDecorator() {
		return new BiomeWastelandDecorator();
	}

	@Override
	public BiomeDecorator getModdedBiomeDecorator(BiomeDecorator original) { //Not sure if I need this, but just in case
		return new BiomeWastelandDecorator();
	}

	@Override
	public List<Biome.SpawnListEntry> getSpawnableList(EnumCreatureType creatureType) {
		return new ArrayList<>();
	}
}
