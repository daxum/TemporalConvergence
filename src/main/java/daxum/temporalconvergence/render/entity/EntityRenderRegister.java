package daxum.temporalconvergence.render.entity;

import daxum.temporalconvergence.entity.EntityFrozen;
import daxum.temporalconvergence.entity.EntityTimePixie;
import daxum.temporalconvergence.render.tileentity.TileDimContrRenderer;
import daxum.temporalconvergence.render.tileentity.TileDimGenRenderer;
import daxum.temporalconvergence.render.tileentity.TilePedestalRenderer;
import daxum.temporalconvergence.render.tileentity.TileTimeChestRenderer;
import daxum.temporalconvergence.tileentity.TileDimContr;
import daxum.temporalconvergence.tileentity.TileDimGen;
import daxum.temporalconvergence.tileentity.TilePedestal;
import daxum.temporalconvergence.tileentity.TileTimeChest;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public final class EntityRenderRegister {
	public static void init() {
		RenderingRegistry.registerEntityRenderingHandler(EntityTimePixie.class, RenderTimePixie::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityFrozen.class, RenderFrozenEntity::new);

		initTileEntity();
	}

	public static void initTileEntity() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileTimeChest.class, new TileTimeChestRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TilePedestal.class, new TilePedestalRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileDimGen.class, new TileDimGenRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileDimContr.class, new TileDimContrRenderer());
	}
}
