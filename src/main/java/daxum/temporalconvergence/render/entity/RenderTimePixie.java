package daxum.temporalconvergence.render.entity;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.entity.EntityTimePixie;
import daxum.temporalconvergence.model.ModelTimePixie;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderTimePixie extends RenderLiving<EntityTimePixie> {
	private static final ResourceLocation TIME_PIXIE_TEXTURE = new ResourceLocation(TemporalConvergence.MODID + ":textures/time_pixie.png");

	public RenderTimePixie(RenderManager rendermanager) {
		super(rendermanager, new ModelTimePixie(), 0.0f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityTimePixie entity) {
		return TIME_PIXIE_TEXTURE;
	}

	@Override
	protected void preRenderCallback(EntityTimePixie entity, float partialTickTime) {
		GlStateManager.scale(0.3F, 0.3F, 0.3F);
	}
}
