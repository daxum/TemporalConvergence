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
