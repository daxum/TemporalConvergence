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
package daxum.temporalconvergence.render;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.lwjgl.opengl.GL11;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.entity.EntityAIBoss;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class AIBossBarRenderer {
	private static final ResourceLocation AI_BOSS_BAR = new ResourceLocation(TemporalConvergence.MODID, "textures/gui/boss_bars.png");
	private static final Map<UUID, WeakReference<EntityAIBoss>> AI_BOSS_LIST = new WeakHashMap<>();

	public static void addAIBoss(EntityAIBoss toAdd) {
		AI_BOSS_LIST.put(toAdd.getBossBarUUID(), new WeakReference(toAdd));
	}

	@SubscribeEvent
	public static void renderBossBar(RenderGameOverlayEvent.BossInfo event) {
		BossInfoClient info = event.getBossInfo();
		//get get get get get get get. Haha, now get doesn't sound like a word anymore
		EntityAIBoss boss = AI_BOSS_LIST.get(info.getUniqueId()) == null ? null : AI_BOSS_LIST.get(info.getUniqueId()).get();

		if (boss != null) {
			event.setCanceled(true);

			int increment = renderBossBar(info, boss.getShieldPercent(event.getPartialTicks()), boss.isShieldDepleted(), event.getResolution().getScaledWidth(), event.getX(), event.getY());

			event.setIncrement(increment);
		}
	}

	//returns the offset for the next bar
	private static int renderBossBar(BossInfoClient healthInfo, float shieldPercent, boolean shieldDepleted, int scaledWidth, int x, int y) {
		Minecraft mc = Minecraft.getMinecraft();

		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		int drawY = y - 9;

		//Render name
		String name = healthInfo.getName().getFormattedText();
		int stringX = scaledWidth / 2 - mc.fontRenderer.getStringWidth(name) / 2;

		mc.fontRenderer.drawStringWithShadow(name, stringX, drawY, 16777215);

		drawY += mc.fontRenderer.FONT_HEIGHT + 3;



		//Render health bar
		mc.getTextureManager().bindTexture(AI_BOSS_BAR);

		final int health_bar_width = 182;
		final int health_bar_height = 6;

		drawBar(x, drawY, health_bar_width, health_bar_height, 0, 0, 182, 6, 256.0f);
		drawBar(x, drawY, (int) (health_bar_width * healthInfo.getPercent()), health_bar_height, 0, 6, (int) (182 * healthInfo.getPercent()), 12, 256.0f);

		drawY += health_bar_height;



		//Render shield bar
		final int shield_bar_width = 170;
		final int shield_bar_height = 5;

		drawBar(x + 6, drawY, shield_bar_width, shield_bar_height, 6, 12, 177, 17, 256.0f);

		if (shieldDepleted) {
			drawBar(x + 6, drawY, (int) (shield_bar_width * shieldPercent), shield_bar_height, 6, 22, (int) (177 * shieldPercent), 27, 256.0f);
		}
		else {
			drawBar(x + 6, drawY, (int) (shield_bar_width * shieldPercent), shield_bar_height, 6, 17, (int) (177 * shieldPercent), 22, 256.0f);
		}

		drawY += 10;

		return drawY - y + 12;
	}

	private static void drawBar(int x, int y, int width, int height, int texStartX, int texStartY, int texEndX, int texEndY, float texSize) {
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder vb = tess.getBuffer();

		final float texSX = texStartX / texSize;
		final float texEX = texEndX / texSize;
		final float texSY = texStartY / texSize;
		final float texEY = texEndY / texSize;

		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		vb.pos(x, y, 0).tex(texSX, texSY).endVertex();
		vb.pos(x, y + height, 0).tex(texSX, texEY).endVertex();
		vb.pos(x + width, y + height, 0).tex(texEX, texEY).endVertex();
		vb.pos(x + width, y, 0).tex(texEX, texSY).endVertex();

		tess.draw();
	}
}
