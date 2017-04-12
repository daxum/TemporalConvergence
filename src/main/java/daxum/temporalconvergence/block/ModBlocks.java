package daxum.temporalconvergence.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public final class ModBlocks {
	public static Block originStone;
	public static Block timeStone;
	public static Block timeSteel;
	public static Block timeWood;
	public static Block timeWoodPlanks;
	public static Block timeSand;
	public static Block timePlant;
	public static Block timeChest;
	public static Block timeWater;
	public static Block timePedestal;
	public static Block dimGen;
	public static Block timeStonePillar;
	public static Block testTeleporter;
	public static Block dimController;

	public static void init() {
		originStone = new BlockBase("origin_stone");
		timeStone = new BlockBase("time_stone");
		timeSteel = new BlockBase(Material.IRON, "time_steel", 5.0f, 30.0f, "pickaxe", 1, SoundType.METAL);
		timeWoodPlanks = new BlockBase(Material.WOOD, "time_wood_planks", 2.0f, 15.0f, "axe", 0, SoundType.WOOD) {
			@Override
			public boolean isWood(IBlockAccess world, BlockPos pos) {
				return world.getBlockState(pos).getBlock() == this;
			}
		};

		timeWood = new BlockTimeWood();
		timeSand = new BlockTimeSand();
		timePlant = new BlockTimePlant();
		timeChest = new BlockTimeChest();
		timeWater = new BlockFluidTimeWater();
		timePedestal = new BlockPedestal();
		dimGen = new BlockDimGen();
		timeStonePillar = new BlockTimeStonePillar();
		testTeleporter = new BlockTestTeleporter();
		dimController = new BlockDimContr();
	}
}
