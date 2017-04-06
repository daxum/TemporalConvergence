package daxum.temporalconvergence.item;

import daxum.temporalconvergence.TemporalConvergence;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

public final class ModItems {
	public static final CreativeTabs TEMPCONVTAB = new CreativeTabs(TemporalConvergence.MODID) {
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(Items.CLOCK);
		}
	};

	public static ToolMaterial TIMEWOOD = EnumHelper.addToolMaterial("TIMEWOOD", 1, 28800, 6.0f, 0.0f, 5);

	public static Item timePearl;
	public static Item timeDust;
	public static Item timeSteelIngot;
	public static Item timeWoodPick;
	public static Item timeWoodShovel;
	public static Item timeWoodAxe;
	public static Item timeFreezer;
	public static Item dimLinker;

	public static void init() {
		timePearl = new ItemBase("time_pearl");
		timeSteelIngot = new ItemBase("time_steel_ingot");

		timeDust = new ItemTimeDust();
		timeWoodPick = new ItemTimeWoodPick();
		timeWoodShovel = new ItemTimeWoodShovel();
		timeWoodAxe = new ItemTimeWoodAxe();
		timeFreezer = new ItemTimeFreezer();
		dimLinker = new ItemDimensionalLinker();
	}
}
