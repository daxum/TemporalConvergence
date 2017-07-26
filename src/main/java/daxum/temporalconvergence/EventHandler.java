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
package daxum.temporalconvergence;

import daxum.temporalconvergence.block.BlockBase;
import daxum.temporalconvergence.block.ModBlocks;
import daxum.temporalconvergence.item.ItemPhaseClothChest;
import daxum.temporalconvergence.item.ModItems;
import daxum.temporalconvergence.particle.ParticleHandler;
import daxum.temporalconvergence.power.PowerDimension;
import daxum.temporalconvergence.render.AIBossBarRenderer;
import daxum.temporalconvergence.util.RenderHelper;
import daxum.temporalconvergence.world.DimensionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public final class EventHandler {
	public static void init() {
		MinecraftForge.EVENT_BUS.register(ModBlocks.class);
		MinecraftForge.EVENT_BUS.register(ModItems.class);

		if (!TemporalConvergence.proxy.isDedicatedServer()) {
			MinecraftForge.EVENT_BUS.register(ParticleHandler.class);
			MinecraftForge.EVENT_BUS.register(AIBossBarRenderer.class);
		}
	}

	//This doesn't seem like a good way to handle updating...
	@SubscribeEvent
	public static void worldTick(WorldTickEvent event) {
		if (event.side == Side.SERVER && event.phase == Phase.END && event.world.provider.getDimension() == 0) {
			PowerDimension.updateDimensions(event.world);
			ItemPhaseClothChest.updateDodgeState();
		}
	}

	@SubscribeEvent
	public static void registerBiomes(RegistryEvent.Register<Biome> event) {
		IForgeRegistry biomeRegistry = event.getRegistry();

		biomeRegistry.register(DimensionHandler.FUTURE_WASTELAND);
		BiomeDictionary.addTypes(DimensionHandler.FUTURE_WASTELAND, Type.SPARSE, Type.DRY/*relatively*/, Type.SAVANNA, Type.DEAD, Type.WASTELAND);
	}

	@SubscribeEvent
	public static void LivingAttacked(LivingAttackEvent event) {
		if (!event.getEntity().world.isRemote && event.getEntity() instanceof EntityPlayer && ItemPhaseClothChest.isWearingArmor((EntityPlayer) event.getEntity())) {
			event.setCanceled(ItemPhaseClothChest.onHit((EntityPlayer) event.getEntityLiving(), event.getSource()));
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		TemporalConvergence.proxy.registerItemRenderer();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void drawBlockHighlight(DrawBlockHighlightEvent event) {
		World world = Minecraft.getMinecraft().world;
		BlockPos pos = event.getTarget().getBlockPos();

		if (pos != null) {
			IBlockState state = world.getBlockState(pos);

			if (state.getBlock() instanceof BlockBase && ((BlockBase)state.getBlock()).hasMultipleBoundingBoxes()) {
				RenderHelper.drawSelectionBoxes(world, event.getPlayer(), state, pos, event.getPartialTicks());
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("unused")
	public static void keyPressEvent(KeyInputEvent event) {
		if (ItemPhaseClothChest.isWearingArmor(Minecraft.getMinecraft().player) && Minecraft.getMinecraft().gameSettings.keyBindSprint.isPressed()) {
			ItemPhaseClothChest.onDodgeKeyPress();
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void clientTick(ClientTickEvent event) {
		if (event.phase == Phase.END) {
			ItemPhaseClothChest.updateDodgeCooldown();
		}
	}
}
