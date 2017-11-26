/***************************************************************************
 * Temporal Convergence
 * Copyright (C) 2017
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 **************************************************************************/
package daxum.temporalconvergence.item;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.block.ModBlocks;
import net.minecraft.block.BlockSlab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModItems {
	public static final CreativeTabs TEMPCONVTAB;

	public static final ToolMaterial SOLAR_WOOD;
	public static final ToolMaterial REINFORCED_SOLAR;

	public static final ArmorMaterial PHASE_CLOTH_ARMOR;

	public static final Item TIME_BULB;
	public static final Item TIME_DUST;
	public static final Item TIME_STEEL_INGOT;
	public static final Item SOLAR_WOOD_PICK;
	public static final Item SOLAR_WOOD_SHOVEL;
	public static final Item SOLAR_WOOD_AXE;
	public static final Item TIME_FREEZER;
	public static final Item DIM_LINKER;
	public static final Item EARLY_FUTURE_DOOR;
	public static final Item REWOUND_TIME_SEEDS;
	public static final Item PHASE_CLOTH_CHEST;
	public static final Item PHASE_CLOTH_LEGS;
	public static final Item PHASE_CLOTH_BOOTS;
	public static final Item PHASE_CLOTH_HELMET;
	public static final Item LUNAR_BOOMERANG;
	public static final Item BRAZIER;
	public static final Item ENERGIZED_CHARCOAL;
	public static final Item STABLE_IRON_INGOT;
	public static final Item REINFORCED_SOLAR_PICK;
	public static final Item REINFORCED_SOLAR_SHOVEL;
	public static final Item REINFORCED_SOLAR_AXE;
	public static final Item ANCIENT_DUST;
	public static final Item TIME_GEM;

	static {
		TEMPCONVTAB = new CreativeTabs(TemporalConvergence.MODID) {
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(Items.CLOCK);
			}
		};

		SOLAR_WOOD = EnumHelper.addToolMaterial("TC_SOLAR_WOOD", 1, 28800, 6.0f, 0.0f, 0);
		PHASE_CLOTH_ARMOR = EnumHelper.addArmorMaterial("TC_PHASE_CLOTH_ARMOR", "temporalconvergence:phase_cloth_armor", 3, new int[] {1, 1, 1, 1}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0f);

		TIME_STEEL_INGOT = new ItemBase("time_steel_ingot");
		ENERGIZED_CHARCOAL = new ItemBase("energized_charcoal") {
			@Override
			public int getItemBurnTime(ItemStack fuel) {
				return 2000;
			}
		};
		STABLE_IRON_INGOT = new ItemBase("stable_iron_ingot");
		ANCIENT_DUST = new ItemBase("ancient_dust");
		TIME_GEM = new ItemBase("time_gem");

		REINFORCED_SOLAR = EnumHelper.addToolMaterial("TC_REINF_SOLAR", 2, 57600, 8.0f, 1.0f, 5).setRepairItem(new ItemStack(STABLE_IRON_INGOT));

		TIME_BULB = new ItemTimeBulb();
		TIME_DUST = new ItemTimeDust();
		SOLAR_WOOD_PICK = new ItemSolarWoodPick();
		SOLAR_WOOD_SHOVEL = new ItemSolarWoodShovel();
		SOLAR_WOOD_AXE = new ItemSolarWoodAxe();
		TIME_FREEZER = new ItemTimeFreezer();
		DIM_LINKER = new ItemDimensionalLinker();
		EARLY_FUTURE_DOOR = new ItemEarlyFutureDoor();
		REWOUND_TIME_SEEDS = new ItemRewoundTimeSeeds();
		PHASE_CLOTH_CHEST = new ItemPhaseClothChest();
		PHASE_CLOTH_LEGS = new ItemPhaseClothLegs();
		PHASE_CLOTH_BOOTS = new ItemPhaseClothBoots();
		PHASE_CLOTH_HELMET = new ItemPhaseClothHelmet();
		LUNAR_BOOMERANG = new ItemLunarBoomerang();
		BRAZIER = new ItemBrazier();
		REINFORCED_SOLAR_PICK = new ItemReinforcedSolarPick();
		REINFORCED_SOLAR_SHOVEL = new ItemReinforcedSolarShovel();
		REINFORCED_SOLAR_AXE = new ItemReinforcedSolarAxe();
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry itemRegistry = event.getRegistry();

		itemRegistry.register(DIM_LINKER);
		itemRegistry.register(TIME_DUST);
		itemRegistry.register(TIME_FREEZER);
		itemRegistry.register(TIME_BULB);
		itemRegistry.register(TIME_STEEL_INGOT);
		itemRegistry.register(SOLAR_WOOD_AXE);
		itemRegistry.register(SOLAR_WOOD_PICK);
		itemRegistry.register(SOLAR_WOOD_SHOVEL);
		itemRegistry.register(EARLY_FUTURE_DOOR);
		itemRegistry.register(REWOUND_TIME_SEEDS);
		itemRegistry.register(PHASE_CLOTH_CHEST);
		itemRegistry.register(PHASE_CLOTH_LEGS);
		itemRegistry.register(PHASE_CLOTH_BOOTS);
		itemRegistry.register(PHASE_CLOTH_HELMET);
		itemRegistry.register(LUNAR_BOOMERANG);
		itemRegistry.register(BRAZIER);
		itemRegistry.register(ENERGIZED_CHARCOAL);
		itemRegistry.register(STABLE_IRON_INGOT);
		itemRegistry.register(REINFORCED_SOLAR_PICK);
		itemRegistry.register(REINFORCED_SOLAR_SHOVEL);
		itemRegistry.register(REINFORCED_SOLAR_AXE);
		itemRegistry.register(ANCIENT_DUST);
		itemRegistry.register(TIME_GEM);

		//ItemBlocks
		itemRegistry.register(new ItemBlock(ModBlocks.DIM_CONTR).setRegistryName(ModBlocks.DIM_CONTR.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.ORIGIN_STONE).setRegistryName(ModBlocks.ORIGIN_STONE.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.PEDESTAL).setRegistryName(ModBlocks.PEDESTAL.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TEST_TELEPORTER).setRegistryName(ModBlocks.TEST_TELEPORTER.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_CHEST).setRegistryName(ModBlocks.TIME_CHEST.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_PLANT).setRegistryName(ModBlocks.TIME_PLANT.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIMESTONE_MIX).setRegistryName(ModBlocks.TIMESTONE_MIX.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_STEEL).setRegistryName(ModBlocks.TIME_STEEL.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_STONE).setRegistryName(ModBlocks.TIME_STONE.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_STONE_PILLAR).setRegistryName(ModBlocks.TIME_STONE_PILLAR.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.LUNAR_WOOD).setRegistryName(ModBlocks.LUNAR_WOOD.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.LUNAR_PLANKS).setRegistryName(ModBlocks.LUNAR_PLANKS.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.EARLY_FUTURE_BLOCK) {@Override public int getMetadata(int damage) { return damage; }}.setRegistryName(ModBlocks.EARLY_FUTURE_BLOCK.getRegistryName()).setHasSubtypes(true));
		itemRegistry.register(new ItemBlock(ModBlocks.FANCY_EARLY_FUTURE_STAIRS).setRegistryName(ModBlocks.FANCY_EARLY_FUTURE_STAIRS.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.EARLY_FUTURE_STAIRS).setRegistryName(ModBlocks.EARLY_FUTURE_STAIRS.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.EARLY_FUTURE_FENCE).setRegistryName(ModBlocks.EARLY_FUTURE_FENCE.getRegistryName()));
		itemRegistry.register(new ItemSlab(ModBlocks.EARLY_FUTURE_HALF_SLAB, (BlockSlab) ModBlocks.EARLY_FUTURE_HALF_SLAB, (BlockSlab) ModBlocks.EARLY_FUTURE_DOUBLE_SLAB).setRegistryName(ModBlocks.EARLY_FUTURE_HALF_SLAB.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.REWOUND_SOIL).setRegistryName(ModBlocks.REWOUND_SOIL.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.EARLY_FUTURE_BUTTON).setRegistryName(ModBlocks.EARLY_FUTURE_BUTTON.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.SOLAR_WOOD) {
			@Override
			public int getItemBurnTime(ItemStack fuel) {
				return 600;
			}
		}.setRegistryName(ModBlocks.SOLAR_WOOD.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.SOLAR_PLANKS) {
			@Override
			public int getItemBurnTime(ItemStack fuel) {
				return 600;
			}
		}.setRegistryName(ModBlocks.SOLAR_PLANKS.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_FURNACE).setRegistryName(ModBlocks.TIME_FURNACE.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.EARLY_FUTURE_ROAD_STRIPE).setRegistryName(ModBlocks.EARLY_FUTURE_ROAD_STRIPE.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.EARLY_FUTURE_ROAD).setRegistryName(ModBlocks.EARLY_FUTURE_ROAD.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.EARLY_FUTURE_ROAD_BORDER).setRegistryName(ModBlocks.EARLY_FUTURE_ROAD_BORDER.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.ROAD_BORDER_STAIRS).setRegistryName(ModBlocks.ROAD_BORDER_STAIRS.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.FUTURE_CHEST).setRegistryName(ModBlocks.FUTURE_CHEST.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.SOLAR_LEAVES).setRegistryName(ModBlocks.SOLAR_LEAVES.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.SOLAR_SAPLING).setRegistryName(ModBlocks.SOLAR_SAPLING.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.LUNAR_LEAVES).setRegistryName(ModBlocks.LUNAR_LEAVES.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.LUNAR_SAPLING).setRegistryName(ModBlocks.LUNAR_SAPLING.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.CRAFTER).setRegistryName(ModBlocks.CRAFTER.getRegistryName()));
		itemRegistry.register(new ItemBlock(ModBlocks.TIME_VAULT).setRegistryName(ModBlocks.TIME_VAULT.getRegistryName()));
	}
}
