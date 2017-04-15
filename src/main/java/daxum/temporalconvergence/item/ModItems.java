package daxum.temporalconvergence.item;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.block.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.IForgeRegistry;

public final class ModItems {
	public static final CreativeTabs TEMPCONVTAB = new CreativeTabs(TemporalConvergence.MODID) {
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(Items.CLOCK);
		}
	};

	public static final ToolMaterial TIMEWOOD = EnumHelper.addToolMaterial("TIMEWOOD", 1, 28800, 6.0f, 0.0f, 5);

	public static final Item TIME_PEARL;
	public static final Item TIME_DUST;
	public static final Item TIME_STEEL_INGOT;
	public static final Item TIME_WOOD_PICK;
	public static final Item TIME_WOOD_SHOVEL;
	public static final Item TIME_WOOD_AXE;
	public static final Item TIME_FREEZER;
	public static final Item DIM_LINKER;

	static {
		TIME_PEARL = new ItemBase("time_pearl");
		TIME_STEEL_INGOT = new ItemBase("time_steel_ingot");

		TIME_DUST = new ItemTimeDust();
		TIME_WOOD_PICK = new ItemTimeWoodPick();
		TIME_WOOD_SHOVEL = new ItemTimeWoodShovel();
		TIME_WOOD_AXE = new ItemTimeWoodAxe();
		TIME_FREEZER = new ItemTimeFreezer();
		DIM_LINKER = new ItemDimensionalLinker();
	}

	public static void registerItems(IForgeRegistry itemRegistry) {
		itemRegistry.register(ModItems.DIM_LINKER);
		itemRegistry.register(ModItems.TIME_DUST);
		itemRegistry.register(ModItems.TIME_FREEZER);
		itemRegistry.register(ModItems.TIME_PEARL);
		itemRegistry.register(ModItems.TIME_STEEL_INGOT);
		itemRegistry.register(ModItems.TIME_WOOD_AXE);
		itemRegistry.register(ModItems.TIME_WOOD_PICK);
		itemRegistry.register(ModItems.TIME_WOOD_SHOVEL);

		//ItemBlocks
		itemRegistry.register(new ItemBlock(ModBlocks.DIM_CONTR).setRegistryName(ModBlocks.DIM_CONTR.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.DIM_GEN).setRegistryName(ModBlocks.DIM_GEN.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.ORIGIN_STONE).setRegistryName(ModBlocks.ORIGIN_STONE.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.PEDESTAL).setRegistryName(ModBlocks.PEDESTAL.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TEST_TELEPORTER).setRegistryName(ModBlocks.TEST_TELEPORTER.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_CHEST).setRegistryName(ModBlocks.TIME_CHEST.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_PLANT).setRegistryName(ModBlocks.TIME_PLANT.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_SAND).setRegistryName(ModBlocks.TIME_SAND.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_STEEL).setRegistryName(ModBlocks.TIME_STEEL.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_STONE).setRegistryName(ModBlocks.TIME_STONE.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_STONE_PILLAR).setRegistryName(ModBlocks.TIME_STONE_PILLAR.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_WATER).setRegistryName(ModBlocks.TIME_WATER.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_WOOD).setRegistryName(ModBlocks.TIME_WOOD.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_WOOD_PLANKS).setRegistryName(ModBlocks.TIME_WOOD_PLANKS.getRegistryName()));
	}
}
