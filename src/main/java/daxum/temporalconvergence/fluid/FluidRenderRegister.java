package daxum.temporalconvergence.fluid;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.block.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidClassic;

public final class FluidRenderRegister {
	public static void init() {
		registerFluid((BlockFluidClassic) ModBlocks.TIME_WATER, "time_water");
	}

	private static void registerFluid(BlockFluidClassic block, String name) {
		Item item = Item.getItemFromBlock(block);
		ModelBakery.registerItemVariants(item);
		final ModelResourceLocation loc = new ModelResourceLocation(TemporalConvergence.MODID + ":fluids", name);
		ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				return loc;
			}
		});
		ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return loc;
			}
		});
	}
}
