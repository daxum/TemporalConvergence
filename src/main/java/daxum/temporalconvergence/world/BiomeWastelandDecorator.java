package daxum.temporalconvergence.world;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomeWastelandDecorator extends BiomeDecorator {
	private WorldGenerator localIronGen = new WorldGenMinable(Blocks.IRON_ORE.getDefaultState(), 4);

	@Override
	public void decorate(World world, Random rand, Biome biome, BlockPos pos) {
		localIronGen.generate(world, rand, pos.add(rand.nextInt(16), rand.nextInt(40) + 8, rand.nextInt(16)));
	}

	@Override
	protected void genDecorations(Biome biomeIn, World worldIn, Random random) {

	}
}
