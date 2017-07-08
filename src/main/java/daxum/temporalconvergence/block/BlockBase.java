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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockBase extends Block {
	public BlockBase(Material material, String registryName, float hardness, float resistance, Tool tool, MiningLevel level, SoundType sound) {
		super(material);
		setUnlocalizedName(registryName);
		setRegistryName(registryName);
		setCreativeTab(ModItems.TEMPCONVTAB);
		setHardness(hardness);
		setResistance(resistance);
		setHarvestLevel(tool.name, level.level);
		setSoundType(sound);
	}

	public BlockBase(String registryName, BlockPresets preset) {
		this(preset.material, registryName, preset.hardness, preset.resistance, preset.tool, preset.level, preset.sound);
	}

	public BlockBase(String registryName, float hardness, float resistance, Tool tool, MiningLevel level) {
		this(Material.ROCK, registryName, hardness, resistance, tool, level, SoundType.STONE);
	}

	public BlockBase(String registryName) {
		this(registryName, 2.0f, 10.0f, Tool.PICKAXE, MiningLevel.WOOD);
	}

	protected void setHarvestTool(Tool tool) {
		setHarvestLevel(tool.name, getHarvestLevel(getDefaultState()));
	}

	protected void setMiningLevel(MiningLevel level) {
		setHarvestLevel(getHarvestTool(getDefaultState()), level.level);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, getProperties());
	}

	//This is one of the stupidest things I've ever done. Also probably really inefficient.
	protected IProperty[] getProperties() {
		List<IProperty> properties = new ArrayList<>();

		for (Field field : getClass().getFields()) {
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
				try {
					Object o = field.get(null);

					if (o instanceof IProperty) {
						properties.add((IProperty) o);
					}
				}
				catch (IllegalArgumentException | IllegalAccessException e) {
					TemporalConvergence.LOGGER.fatal("Reflective BlockState wizardry failed for class {}!", getClass());

					throw new ReportedException(new CrashReport("Reflective BlockState wizardry failed for class " + getClass() + "!", e));
				}
			}
		}

		return properties.toArray(new IProperty[0]);
	}

	//TODO: find way to remove Default class
	protected void setStateDefaults(Default... defaults) {
		IBlockState defaultState = blockState.getBaseState();

		for (Default def : defaults) {
			defaultState = defaultState.withProperty(def.property, def.value);
		}
	}

	protected class Default<T extends Comparable<T>> {
		public IProperty<T> property;
		public T value;

		public Default(IProperty<T> p, T v) {
			property = p;
			value = v;
		}
	}

	protected boolean isCube() {
		return true;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return isCube();
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return isCube();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return isCube();
	}

	public enum BlockPresets {
		STONE(Material.ROCK, 2.0f, 10.0f, Tool.PICKAXE, MiningLevel.WOOD, SoundType.STONE),
		PLANT(Material.PLANTS, 0.0f, 0.0f, Tool.NONE, MiningLevel.HAND, SoundType.PLANT),
		UNBREAKABLE(Material.BARRIER, -1.0f, Float.MAX_VALUE, Tool.NONE, MiningLevel.HAND, SoundType.STONE),
		IRON(Material.IRON, 5.0f, 30.0f, Tool.PICKAXE, MiningLevel.STONE, SoundType.METAL),
		WOOD(Material.WOOD, 2.0f, 15.0f, Tool.AXE, MiningLevel.HAND, SoundType.WOOD),
		STONE_MACHINE(Material.ROCK, 5.0f, 30.0f, Tool.PICKAXE, MiningLevel.IRON, SoundType.STONE),
		WEAK_IRON(Material.IRON, 2.0f, 10.0f, Tool.PICKAXE, MiningLevel.WOOD, SoundType.METAL);

		private final Material material;
		private final float hardness;
		private final float resistance;
		private final Tool tool;
		private final MiningLevel level;
		private final SoundType sound;

		private BlockPresets(Material m, float h, float r, Tool t, MiningLevel l, SoundType s) {
			material = m;
			hardness = h;
			resistance = r;
			tool = t;
			level = l;
			sound = s;
		}
	}

	public enum Tool {
		NONE(""),
		PICKAXE("pickaxe"),
		AXE("axe"),
		SHOVEL("shovel");

		private final String name;

		private Tool(String n) {
			name = n;
		}
	}

	public enum MiningLevel {
		HAND(-1),
		WOOD(0),
		GOLD(0),
		STONE(1),
		IRON(2),
		DIAMOND(3);

		private final int level;

		private MiningLevel(int l) {
			level = l;
		}
	}
}
