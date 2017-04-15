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
package daxum.temporalconvergence.world;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

public final class DimensionHandler {
	//Dimensions
	public static final DimensionType EARLY_FUTURE;

	//Biomes
	public static final Biome FUTURE_WASTELAND;

	static {
		EARLY_FUTURE = DimensionType.register("The Early Future", "_early_future", -39, WorldProviderEarlyFuture.class, false);

		FUTURE_WASTELAND = new BiomeFutureWasteland(new Biome.BiomeProperties("Future Wasteland").setBaseHeight(0.125f).setHeightVariation(0.05f).setTemperature(0.9f).setRainfall(0.2f).setWaterColor(0x887037));
	}

	public static void init() {
		DimensionManager.registerDimension(EARLY_FUTURE.getId(), EARLY_FUTURE);
	}

	public static void changePlayerDim(EntityPlayer player, DimensionType newDim) {
		if (player != null && player instanceof EntityPlayerMP)
			((EntityPlayerMP)player).mcServer.getPlayerList().transferPlayerToDimension((EntityPlayerMP) player, newDim.getId(), new TheTeleporter(player.world));
	}
}
