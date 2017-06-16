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
package daxum.temporalconvergence.block;

import daxum.temporalconvergence.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockBase extends Block {
	public BlockBase (Material material, String registryName, float hardness, float resistance, String tool, int level, SoundType sound) {
		super(material);
		setUnlocalizedName(registryName);
		setRegistryName(registryName);
		setCreativeTab(ModItems.TEMPCONVTAB);
		setHardness(hardness);
		setResistance(resistance);
		setHarvestLevel(tool, level);
		setSoundType(sound);
	}

	public BlockBase (String unlocalizedName, float hardness, float resistance, String tool, int level) {
		this(Material.ROCK, unlocalizedName, hardness, resistance, tool, level, SoundType.STONE);
	}

	public BlockBase (String unlocalizedName) {
		this(unlocalizedName, 2.0f, 10.0f, "pickaxe", 0);
	}

	public BlockBase (Material material, String unlocalizedName, float hardness, float resistance) {
		this(material, unlocalizedName, hardness, resistance, "", 0, SoundType.STONE);
	}
}
