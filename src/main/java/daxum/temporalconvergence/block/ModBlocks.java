package daxum.temporalconvergence.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.IForgeRegistry;

public final class ModBlocks {
	public static final Block ORIGIN_STONE;
	public static final Block TIME_STONE;
	public static final Block TIME_STEEL;
	public static final Block TIME_WOOD;
	public static final Block TIME_WOOD_PLANKS;
	public static final Block TIME_SAND;
	public static final Block TIME_PLANT;
	public static final Block TIME_CHEST;
	public static final Block TIME_WATER;
	public static final Block PEDESTAL;
	public static final Block DIM_GEN;
	public static final Block TIME_STONE_PILLAR;
	public static final Block TEST_TELEPORTER;
	public static final Block DIM_CONTR;

	static {
		ORIGIN_STONE = new BlockBase("origin_stone");
		TIME_STONE = new BlockBase("time_stone");
		TIME_STEEL = new BlockBase(Material.IRON, "time_steel", 5.0f, 30.0f, "pickaxe", 1, SoundType.METAL);
		TIME_WOOD_PLANKS = new BlockBase(Material.WOOD, "time_wood_planks", 2.0f, 15.0f, "axe", 0, SoundType.WOOD) {
			@Override
			public boolean isWood(IBlockAccess world, BlockPos pos) {
				return world.getBlockState(pos).getBlock() == this;
			}
		};

		TIME_WOOD = new BlockTimeWood();
		TIME_SAND = new BlockTimeSand();
		TIME_PLANT = new BlockTimePlant();
		TIME_CHEST = new BlockTimeChest();
		TIME_WATER = new BlockFluidTimeWater();
		PEDESTAL = new BlockPedestal();
		DIM_GEN = new BlockDimGen();
		TIME_STONE_PILLAR = new BlockTimeStonePillar();
		TEST_TELEPORTER = new BlockTestTeleporter();
		DIM_CONTR = new BlockDimContr();
	}

	public static void registerBlocks(IForgeRegistry blockRegistry) {
		blockRegistry.register(ModBlocks.DIM_CONTR);
		blockRegistry.register(ModBlocks.DIM_GEN);
		blockRegistry.register(ModBlocks.ORIGIN_STONE);
		blockRegistry.register(ModBlocks.PEDESTAL);
		blockRegistry.register(ModBlocks.TEST_TELEPORTER);
		blockRegistry.register(ModBlocks.TIME_CHEST);
		blockRegistry.register(ModBlocks.TIME_PLANT);
		blockRegistry.register(ModBlocks.TIME_SAND);
		blockRegistry.register(ModBlocks.TIME_STEEL);
		blockRegistry.register(ModBlocks.TIME_STONE);
		blockRegistry.register(ModBlocks.TIME_STONE_PILLAR);
		blockRegistry.register(ModBlocks.TIME_WATER);
		blockRegistry.register(ModBlocks.TIME_WOOD);
		blockRegistry.register(ModBlocks.TIME_WOOD_PLANKS);
	}
}
