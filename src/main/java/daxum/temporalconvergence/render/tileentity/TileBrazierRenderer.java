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
package daxum.temporalconvergence.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.tileentity.TileBrazier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileBrazierRenderer extends FastTESR<TileBrazier> {
	private static final Map<Item, ResourceLocation> FILL_TEXTURES = new HashMap<>();
	private static boolean registrationComplete = false;

	@Override
	public void renderTileEntityFast(TileBrazier te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, BufferBuilder buffer) {
		if (te.shouldRenderContents()) {
			ResourceLocation location = FILL_TEXTURES.get(te.getItem());

			TextureAtlasSprite texture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite((location == null ? TextureMap.LOCATION_MISSING_TEXTURE : location).toString());

			final float bottom = 0.0625f;
			final float top = 0.1875f;
			final float minX = 0.3125f;
			final float minZ = 0.3125f;
			final float maxX = 0.6875f;
			final float maxZ = 0.6875f;

			final float fillHeight = (top - bottom) * te.getFillPercent(partialTicks) + bottom;

			final int light = te.getWorld().isBlockLoaded(te.getPos()) ? te.getWorld().getCombinedLight(te.getPos(), 0) : 0;
			final int light1 = light >> 16 & 65535;
			final int light2 = light & 65535;

			buffer.setTranslation(x, y, z);
			buffer.pos(minX, fillHeight, minZ).color(255, 255, 255, 255).tex(texture.getMinU(), texture.getMinV()).lightmap(light1, light2).endVertex();
			buffer.pos(minX, fillHeight, maxZ).color(255, 255, 255, 255).tex(texture.getMinU(), texture.getMaxV()).lightmap(light1, light2).endVertex();
			buffer.pos(maxX, fillHeight, maxZ).color(255, 255, 255, 255).tex(texture.getMaxU(), texture.getMaxV()).lightmap(light1, light2).endVertex();
			buffer.pos(maxX, fillHeight, minZ).color(255, 255, 255, 255).tex(texture.getMaxU(), texture.getMinV()).lightmap(light1, light2).endVertex();
		}
	}

	public static void addFillTexture(Item item, ResourceLocation location) {
		if (!registrationComplete) {
			if (item != null && FILL_TEXTURES.get(item) == null) {
				FILL_TEXTURES.put(item, location);
			}
			else {
				TemporalConvergence.LOGGER.error("Invalid call to TileBrazierRenderer.addFillTexture(): item is null or already registered");
			}
		}
		else {
			TemporalConvergence.LOGGER.error("Attempted to register item {} to texture {} after textureStitchEvent was fired", item, location);
		}
	}

	@SubscribeEvent
	public static void textureStitch(TextureStitchEvent.Pre event) {
		TextureMap map = event.getMap();

		for (Map.Entry<Item, ResourceLocation> entry : FILL_TEXTURES.entrySet()) {
			map.registerSprite(entry.getValue());
		}

		registrationComplete = true;
	}

	static {
		addFillTexture(ModItems.TIME_DUST, new ResourceLocation(TemporalConvergence.MODID, "blocks/brazier_dust_fill"));
		addFillTexture(ModItems.ANCIENT_DUST, new ResourceLocation(TemporalConvergence.MODID, "blocks/brazier_ancient_dust_fill"));
		addFillTexture(ModItems.ENERGIZED_CHARCOAL, new ResourceLocation(TemporalConvergence.MODID, "blocks/brazier_energized_charcoal_fill"));
	}
}
