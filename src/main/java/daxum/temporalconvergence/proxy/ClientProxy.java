package daxum.temporalconvergence.proxy;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.block.ModBlocks;
import daxum.temporalconvergence.fluid.FluidRenderRegister;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.particle.ParticleDimGenCraft;
import daxum.temporalconvergence.render.entity.EntityRenderRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class ClientProxy implements IProxy {

	@Override
	public void registerItemRenderer() {
		registerRender(ModItems.timePearl);
		registerRender(ModItems.timeSteelIngot);
		registerRender(ModItems.timeDust);
		registerRender(ModItems.timeWoodPick);
		registerRender(ModItems.timeWoodShovel);
		registerRender(ModItems.timeWoodAxe);
		registerRender(ModItems.timeFreezer);
		registerRender(ModItems.dimLinker);
	}

	@Override
	public void registerBlockRenderer() {
		registerRender(Item.getItemFromBlock(ModBlocks.originStone));
		registerRender(Item.getItemFromBlock(ModBlocks.timeStone));
		registerRender(Item.getItemFromBlock(ModBlocks.timeSteel));
		registerRender(Item.getItemFromBlock(ModBlocks.timePlant));
		registerRender(Item.getItemFromBlock(ModBlocks.timeWood));
		registerRender(Item.getItemFromBlock(ModBlocks.timeWoodPlanks));
		registerRender(Item.getItemFromBlock(ModBlocks.timeSand));
		registerRender(Item.getItemFromBlock(ModBlocks.timeChest));
		registerRender(Item.getItemFromBlock(ModBlocks.timePedestal));
		registerRender(Item.getItemFromBlock(ModBlocks.dimGen));
		registerRender(Item.getItemFromBlock(ModBlocks.timeStonePillar));
		registerRender(Item.getItemFromBlock(ModBlocks.testTeleporter)); //Why?
		registerRender(Item.getItemFromBlock(ModBlocks.dimController));
	}

	@Override
	public void registerFluidRenderer() {
		FluidRenderRegister.init();
	}

	@Override
	public void registerEntityRenderer() {
		EntityRenderRegister.init();
	}

	public void registerRender(Item item) {
		ModelResourceLocation mrl = new ModelResourceLocation(TemporalConvergence.MODID + ":" + item.getUnlocalizedName().substring(5), "inventory");
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, mrl);
	}

	@Override
	public void spawnDimGenParticle(World world, double posX, double posY, double posZ, double targetX, double targetY, double targetZ) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDimGenCraft(world, posX, posY, posZ, targetX, targetY, targetZ));

	}
}
